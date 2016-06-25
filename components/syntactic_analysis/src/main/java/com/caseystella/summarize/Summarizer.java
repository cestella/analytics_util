package com.caseystella.summarize;

import com.caseystella.type.TypeInference;
import com.caseystella.type.ValueSummary;
import com.google.common.collect.Ordering;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import java.util.*;

public class Summarizer {


  public static Map<String, Summary> summarize( DataFrame df
                          , int numericSampleSize
                          , final int nonNumericSampleSize
                          , final List<Double> percentiles
                          )
  {
    Map<String, Summary> columnSummaries = new LinkedHashMap<>();
    final List<String> columns = new ArrayList<>();
    for(String s : df.columns()) {
      columns.add(s);
      columnSummaries.put(s, new Summary());
    }

    JavaPairRDD<TypedColumnWithModifier, ValueSummary> summarize =
    df.javaRDD().flatMapToPair(new PairFlatMapFunction<Row, TypedColumnWithModifier, ValueSummary>() {
      @Override
      public Iterable<Tuple2<TypedColumnWithModifier, ValueSummary>> call(Row row) throws Exception {
        List<Tuple2<TypedColumnWithModifier, ValueSummary>> ret = new ArrayList<>();
        for (int i = 0; i < row.size(); ++i) {
          String column = columns.get(i);
          Object o = row.get(i);
          ValueSummary summary = TypeInference.Type.infer(o);
          ret.add(new Tuple2(new TypedColumnWithModifier(column, summary.getType(), summary.getModifier()), summary));
          if (summary.getType() == TypeInference.Type.INTEGRAL) {
            //we want to treat integers independently as well as part of floats because Z \subset R
            ValueSummary valueSummaryFloat = ValueSummary.of(((Number) summary.getValue()).doubleValue()
                    , TypeInference.Type.FLOAT
                    , summary.getModifier()
            );
            ret.add(new Tuple2(new TypedColumnWithModifier(column, valueSummaryFloat.getType(), valueSummaryFloat.getModifier())
                            , valueSummaryFloat
                    )
            );
          }
        }
        return ret;
      }
    }).cache();

    //count by type
    Map<TypedColumnWithModifier, Long> countByType = null;
    {
      JavaPairRDD<TypedColumnWithModifier, Long> pair = summarize.mapToPair(new PairFunction<Tuple2<TypedColumnWithModifier, ValueSummary>, TypedColumnWithModifier, Long>() {
        @Override
        public Tuple2<TypedColumnWithModifier, Long> call(Tuple2<TypedColumnWithModifier, ValueSummary> t) throws Exception {
          return new Tuple2(t._1, 1L);
        }
      });
      countByType = pair.reduceByKey(new Function2<Long, Long, Long>() {
        @Override
        public Long call(Long x, Long y) throws Exception {
          return x + y;
        }
      }).collectAsMap();
      for(Map.Entry<String, List<Map<String, Object>>> kv : Summary.countByColumn(countByType).entrySet()) {
        columnSummaries.get(kv.getKey()).getCountByType().addAll(kv.getValue());
      }
    }
    //count approximate distinct values by type
    Map<TypedColumnWithModifier, Long> countDistinctByType = new HashMap<>();
    {
      Map<TypedColumnWithModifier, Object> tmp = summarize.mapToPair(new PairFunction<Tuple2<TypedColumnWithModifier, ValueSummary>,TypedColumnWithModifier, Long>() {
        @Override
        public Tuple2<TypedColumnWithModifier, Long> call(Tuple2<TypedColumnWithModifier, ValueSummary> t) throws Exception {
          return new Tuple2(t._1, t._2.getValue());
        }
      })
                                                          .countApproxDistinctByKey(0.001).collectAsMap();
      for(Map.Entry<TypedColumnWithModifier, Object> kv : tmp.entrySet()) {
        countDistinctByType.put(kv.getKey(), Long.parseLong(kv.getValue() + ""));
      }

      for(Map.Entry<String, List<Map<String, Object>>> kv : Summary.countByColumn(countDistinctByType).entrySet()) {
        columnSummaries.get(kv.getKey()).getCountDistinctByType().addAll(kv.getValue());
      }
    }

    //numeric summarize
    Map<TypedColumnWithModifier, Map<String, Double>> numericValueSummary =  null;
    {
      JavaPairRDD<TypedColumnWithModifier, Iterable<Double>> groupedSample =
      summarize.filter(new Function<Tuple2<TypedColumnWithModifier, ValueSummary>, Boolean>() {
        @Override
        public Boolean call(Tuple2<TypedColumnWithModifier, ValueSummary> t) throws Exception {
          return doSampling(t._1.type, t._1.modifier);
        }
      })
              .mapToPair(new PairFunction<Tuple2<TypedColumnWithModifier, ValueSummary>, TypedColumnWithModifier, Double>() {
                @Override
                public Tuple2<TypedColumnWithModifier, Double> call(Tuple2<TypedColumnWithModifier, ValueSummary> t) throws Exception {
                  return new Tuple2<>(t._1, ((Number) t._2.getValue()).doubleValue());
                }
              })
              .sampleByKeyExact(false, getSamplePct(countByType, numericSampleSize))
              .groupByKey()
      ;
      numericValueSummary = groupedSample.mapToPair(new PairFunction<Tuple2<TypedColumnWithModifier, Iterable<Double>>, TypedColumnWithModifier, Map<String, Double>>() {
        @Override
        public Tuple2<TypedColumnWithModifier, Map<String, Double>> call(Tuple2<TypedColumnWithModifier, Iterable<Double>> t) throws Exception {
          return new Tuple2(t._1, statisticallySummarize(t._2, percentiles));
        }
      })
                                  .collectAsMap();

      for(Map.Entry<String, List<Map<String, Object>>> kv : Summary.summaryToList(numericValueSummary).entrySet()) {
        columnSummaries.get(kv.getKey()).getNumericValueSummary().addAll(kv.getValue());
      }
    }

    //non-numeric summarize
    Map<TypedColumnWithModifier, Map<String, Double>> nonNumericValueSummary = null;
    {
      List<Tuple2<TypedColumnWithModifier, Map<String, Double>>> sampleAndCount =
      summarize.filter(new Function<Tuple2<TypedColumnWithModifier, ValueSummary>, Boolean>() {
        @Override
        public Boolean call(Tuple2<TypedColumnWithModifier, ValueSummary> t) throws Exception {
          return !doSampling(t._1.type, t._1.modifier);
        }
      })
               .mapToPair(new PairFunction<Tuple2<TypedColumnWithModifier, ValueSummary>, TypedColumnWithModifierAndValue, Long>() {
                 @Override
                 public Tuple2<TypedColumnWithModifierAndValue, Long> call(Tuple2<TypedColumnWithModifier, ValueSummary> t) throws Exception {
                   return new Tuple2<>(t._1.withValue(t._2.getValue()), 1L);
                 }
               })
               .reduceByKey(new Function2<Long, Long, Long>() {
                 @Override
                 public Long call(Long x, Long y) throws Exception {
                   return x + y;
                 }
               })
               .mapToPair(new PairFunction<Tuple2<TypedColumnWithModifierAndValue, Long>, TypedColumnWithModifier, Tuple2<String, Long>>() {
                            @Override
                            public Tuple2<TypedColumnWithModifier, Tuple2<String, Long>> call(Tuple2<TypedColumnWithModifierAndValue, Long> t) throws Exception {
                              return new Tuple2<>(new TypedColumnWithModifier(t._1.column, t._1.type, t._1.modifier)
                                      , new Tuple2<>(t._1.value, t._2)
                              );
                            }
                          }
                         )
               .groupByKey()
               .mapToPair(new PairFunction<Tuple2<TypedColumnWithModifier,Iterable<Tuple2<String,Long>>>, TypedColumnWithModifier,  Map<String, Double>>() {

                 @Override
                 public Tuple2<TypedColumnWithModifier, Map<String, Double>> call(Tuple2<TypedColumnWithModifier, Iterable<Tuple2<String, Long>>> t) throws Exception {
                   return new Tuple2<>(t._1, getTopK(t._2, nonNumericSampleSize));
                 }
               }).collect();
      nonNumericValueSummary= new HashMap<>();
      for(Tuple2<TypedColumnWithModifier, Map<String, Double>> t : sampleAndCount)  {
        nonNumericValueSummary.put(t._1, t._2);
      }

      for(Map.Entry<String, List<Map<String, Object>>> kv : Summary.summaryToList(nonNumericValueSummary).entrySet()) {
        columnSummaries.get(kv.getKey()).getNonNumericValueSummary().addAll(kv.getValue());
      }
    }
    return columnSummaries;
  }

  public static Map<String, Double> getTopK(Iterable<Tuple2<String, Long>> values, int k) {
    Map<String, Double> ret = new LinkedHashMap<>();
    Iterable<Tuple2<String, Long>> topK =
    Ordering.from(new Comparator<Tuple2<String, Long>>() {
      @Override
      public int compare(Tuple2<String, Long> o1, Tuple2<String, Long> o2) {
        return Long.compare(o1._2, o2._2);
      }
    }).greatestOf(values, k);
    for(Tuple2<String, Long> kv : topK) {
      ret.put(kv._1, kv._2.doubleValue());
    }
    return ret;
  }

  public static Map<String, Double> statisticallySummarize(Iterable<Double> values, List<Double> percentiles) {
    DescriptiveStatistics statistics = new DescriptiveStatistics();
    Map<String, Double> ret = new HashMap<>();
    for(Double d : values) {
      statistics.addValue(d);
    }
    for(Double pct : percentiles) {
      ret.put(pct + " %ile", statistics.getPercentile(pct));
    }
    ret.put("kurtosis", statistics.getKurtosis());
    return ret;
  }

  public static boolean doSampling(TypeInference.Type type, TypeInference.Modifier modifier) {
    return type.isNumeric() && modifier != TypeInference.Modifier.MISSING;
  }
  public static Map<TypedColumnWithModifier, Object> getSamplePct(Map<TypedColumnWithModifier, Long> countByType
                                                                 , int targetSize
                                                                 )
  {
    Map<TypedColumnWithModifier, Object> ret = new HashMap<>();
    for(Map.Entry<TypedColumnWithModifier, Long> kv : countByType.entrySet()) {
      if(doSampling(kv.getKey().type, kv.getKey().modifier)) {
        double pct = 1.0;
        if(targetSize < kv.getValue()) {
          pct = targetSize / kv.getValue().doubleValue();
        }
        ret.put(kv.getKey(), pct);
      }
    }
    return ret;
  }
}
