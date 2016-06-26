package com.caseystella.summarize;

import com.caseystella.type.TypeInference;
import com.caseystella.type.ValueSummary;
import com.caseystella.util.LogLikelihood;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

public class Summarizer implements Serializable {


  public static TotalSummary summarize( DataFrame df
                          , int numericSampleSize
                          , final int nonNumericSampleSize
                          , final List<Double> percentiles
                          , final int numDistinctValuesCutoff
                          , final double similarityCutoff
                          , final int similarityMinOccurrances
                          , final int vectorSize
                          )
  {
    TotalSummary totalSummary = new TotalSummary();
    Map<String, Summary> columnSummaries = totalSummary.getColumnSummaries();
    final List<String> columns = new ArrayList<>();
    for(String s : df.columns()) {
      columns.add(s);
      columnSummaries.put(s, new Summary());
    }
    SynonymHandler handler = new SynonymHandler(df, columns, similarityMinOccurrances, vectorSize, similarityCutoff);
    Map<String, Map<String, String>> synonyms = handler.findSynonymsByColumn();
    for(Map.Entry<String, Map<String, String>> kv : synonyms.entrySet()) {
      columnSummaries.get(kv.getKey()).setSynonyms(kv.getValue());
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
    final long totalCount = summarize.count();
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
    final Map<TypedColumnWithModifier, Long> countDistinctByType = new HashMap<>();
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

    {

      final Map<TypedColumnWithModifierAndValue, Long> categoricalCounts =
      df.javaRDD().flatMapToPair(new PairFlatMapFunction<Row, TypedColumnWithModifierAndValue, Long>() {
      @Override
      public Iterable<Tuple2<TypedColumnWithModifierAndValue, Long>> call(Row row) throws Exception {
        List<Tuple2<TypedColumnWithModifierAndValue, Long>> categoricalVariables = new ArrayList<>();
        for (int i = 0; i < row.size(); ++i) {
          String column = columns.get(i);
          Object o = row.get(i);
          ValueSummary summary = TypeInference.Type.infer(o);
          TypedColumnWithModifier columnWithModifier = new TypedColumnWithModifier(column, summary.getType(), summary.getModifier());
          Long l = countDistinctByType.get(columnWithModifier) ;
          if(l != null && l < numDistinctValuesCutoff) {
            categoricalVariables.add(new Tuple2<>(columnWithModifier.withValue(summary.getValue(), false), 1L));
          }
        }
        return categoricalVariables;
      }
    }).reduceByKey(
              new Function2<Long, Long, Long>() {
                @Override
                public Long call(Long x, Long y) throws Exception {
                  return x + y;
                }
              }
      ).collectAsMap();

      final List<Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double>> g_score_outliers=
      df.javaRDD().flatMapToPair(new PairFlatMapFunction<Row, Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Long>() {
      @Override
      public Iterable<Tuple2<Tuple2<TypedColumnWithModifierAndValue,TypedColumnWithModifierAndValue>, Long>> call(Row row) throws Exception {
        List<TypedColumnWithModifierAndValue> categoricalVariables = new ArrayList<>();
        for (int i = 0; i < row.size(); ++i) {
          String column = columns.get(i);
          Object o = row.get(i);
          ValueSummary summary = TypeInference.Type.infer(o);
          TypedColumnWithModifier columnWithModifier = new TypedColumnWithModifier(column, summary.getType(), summary.getModifier());
          Long l = countDistinctByType.get(columnWithModifier) ;
          if(l != null && l < numDistinctValuesCutoff) {
            categoricalVariables.add(columnWithModifier.withValue(summary.getValue(), false));
          }
        }
        List<Tuple2<Tuple2<TypedColumnWithModifierAndValue,TypedColumnWithModifierAndValue>, Long> > ret = new ArrayList<>();
        for(int i = 0;i < categoricalVariables.size();++i) {
          for(int j = i+1;j < categoricalVariables.size();++j) {
            ret.add(new Tuple2<>(new Tuple2<>(categoricalVariables.get(i), categoricalVariables.get(j)), 1L));
          }
        }
        return ret;
      }
    }).reduceByKey(
              new Function2<Long, Long, Long>() {
                @Override
                public Long call(Long x, Long y) throws Exception {
                  return x + y;
                }
              }
      ).mapToPair(new PairFunction<Tuple2<Tuple2<TypedColumnWithModifierAndValue,TypedColumnWithModifierAndValue>,Long>, Tuple2<TypedColumnWithModifierAndValue,TypedColumnWithModifierAndValue>, Double>() {
        @Override
        public Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double> call(Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Long> t) throws Exception {
          long p_xy = t._2;
          Long p_x = categoricalCounts.get(t._1._1);
          if(p_x == null) {
            p_x = 0L;
          }
          Long p_y = categoricalCounts.get(t._1._2);
          if(p_y == null) {
            p_y = 0L;
          }
          return new Tuple2<>(t._1, get_gscore(p_xy, p_x , p_y, totalCount ));
        }
      }).takeOrdered(10, new Comp());
      totalSummary.setConnectedColumns(TotalSummary.toConnectedColumns(g_score_outliers));
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
    return totalSummary;
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
  public static class Comp implements Comparator<Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double>>, Serializable
  {
    @Override
    public int compare(Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double> o1, Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double> o2) {
      return -1*Double.compare(o1._2, o2._2);
    }
  };
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

  public static double get_gscore(long p_xy, long p_x, long p_y, long total) {
    long k11 = p_xy;
    long k12= (p_y - p_xy); //count of x without y
    long k21= (p_x - p_xy); // count of y without x
    long k22= total - (p_x + p_y - p_xy); //count of neither x nor y

    return LogLikelihood.logLikelihoodRatio(k11, k12, k21, k22);
  }
}
