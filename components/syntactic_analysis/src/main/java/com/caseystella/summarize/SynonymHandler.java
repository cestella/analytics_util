package com.caseystella.summarize;

import com.caseystella.util.ConversionUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import scala.Function1;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

public class SynonymHandler implements Serializable {

  private transient JavaPairRDD<String, List<Tuple2<String, Double>>> word2SynonymRDD;
  public SynonymHandler(DataFrame df
                       , final List<String> columns
                       , int minOccurrance
                       , int vectorSize
                       , final double scoreCutoff)
  {
    JavaRDD<List<String>> rows =
    df.javaRDD().map(new Function<Row, List<String>>() {
      @Override
      public List<String> call(Row row) throws Exception {
        List<String> str = new ArrayList<>();
        for(int i = 0;i < columns.size();++i) {
          str.add(columns.get(i) + ":" + row.get(i));
        }
        return str;
      }
    }).cache();



    // Learn a mapping from words to Vectors.
    Word2Vec word2Vec = new Word2Vec()
            .setVectorSize(vectorSize)
            .setSeed(0)
            .setWindowSize(columns.size())
            .setMinCount(minOccurrance);
    final Word2VecModel model = word2Vec.fit(rows);
    JavaRDD<String> vocabulary = rows.flatMap(new FlatMapFunction<List<String>, String>() {
      @Override
      public Iterable<String> call(List<String> row) throws Exception {
        List<String> ret = new ArrayList<String>();
        for(String val : row) {
          ret.add(val);
        }
        return ret;
      }
    }).distinct();
    word2SynonymRDD =
    vocabulary.flatMapToPair(new PairFlatMapFunction<String, String, List<Tuple2<String, Double>>>() {
      @Override
      public Iterable<Tuple2<String, List<Tuple2<String, Double>>>> call(String s) throws Exception {
        List<Tuple2<String, Double>> synonyms = getSynonyms(model, s, scoreCutoff);
        if(synonyms.size() > 0) {
          return ImmutableList.of(new Tuple2<>(s, synonyms));
        }
        return Collections.emptyList();
      }
    }).cache();
  }
  public Map<String, String> findAllSynonyms() {
    List<Tuple2<String, Tuple2<String, Double>>> allSynonyms =
    word2SynonymRDD.flatMapToPair(new PairFlatMapFunction<Tuple2<String,List<Tuple2<String,Double>>>, String, Tuple2<String, Double>>() {
      @Override
      public Iterable<Tuple2<String, Tuple2<String, Double>>> call(Tuple2<String, List<Tuple2<String, Double>>> t) throws Exception {
        List<Tuple2<String, Tuple2<String, Double>>> ret = new ArrayList<>();
        for(Tuple2<String, Double> kv : t._2) {
          ret.add(new Tuple2<>(t._1, new Tuple2<>(kv._1, kv._2)));
        }
        return ret;
      }
    }).takeOrdered(20, new ScoreComparator());
    Map<String, String> ret = new LinkedHashMap<>();
    for(Tuple2<String, Tuple2<String, Double>> synonym : allSynonyms) {
      if(!synonym._1.equals(synonym._2._1)) {
        ret.put(synonym._1, synonym._2._1);
      }
    }
    return ret;
  }

  public static class ScoreComparator implements Comparator<Tuple2<String, Tuple2<String, Double>>>, Serializable {
      @Override
      public int compare(Tuple2<String, Tuple2<String, Double>> o1, Tuple2<String, Tuple2<String, Double>> o2) {
        return -1*Double.compare(o1._2._2, o2._2._2);
      }
    }

  public Map<String, Map<String, String>> findSynonymsByColumn() {

    Map<String, List<Tuple2<String, Double>>> wordToSynonym = word2SynonymRDD.collectAsMap();
    Map<String, Set<Tuple2<Map.Entry<String, String>, Double>>>  synonymMap = new HashMap<>();
    for(Map.Entry<String, List<Tuple2<String, Double>>> kv : wordToSynonym.entrySet()) {
      Tuple2<String, String> cw = word2columnVal(kv.getKey());
      String column = cw._1;
      String word= cw._2;
      Set<Tuple2<Map.Entry<String, String>, Double>> list = synonymMap.get(column);
      if(list == null) {
        list = new HashSet<>();
        synonymMap.put(column, list);
      }
      for(Tuple2<String, Double> synonym : kv.getValue()) {
        Tuple2<String, String> synonymCw = word2columnVal(synonym._1);
        if(synonymCw._1.equals(column)) {
          String synonymWord = synonymCw._2;
          int comparision = word.compareTo(synonymWord);
          if (comparision != 0) {
            String left = comparision < 0 ? word : synonymWord;
            String right = comparision > 0 ? word : synonymWord;
            list.add(new Tuple2<Map.Entry<String, String>, Double>(new AbstractMap.SimpleEntry<>(left, right), synonym._2));
          }
        }
      }
    }
    Map<String, Map<String, String>>  ret= new HashMap<>();
    for(Map.Entry<String, Set<Tuple2<Map.Entry<String, String>, Double>>> kv : synonymMap.entrySet()) {
      List<Tuple2<Map.Entry<String, String>, Double>> l = new ArrayList<>(kv.getValue());
      Collections.sort(l, new Comparator<Tuple2<Map.Entry<String, String>, Double>>() {
        @Override
        public int compare(Tuple2<Map.Entry<String, String>, Double> o1, Tuple2<Map.Entry<String, String>, Double> o2) {
          return -1*Double.compare(o1._2, o2._2);
        }
      }
      );
      Map<String, String> v = new LinkedHashMap<>();
      for(Tuple2<Map.Entry<String, String>, Double> t : l) {
        v.put(t._1.getKey(), t._1.getValue());
      }
      ret.put(kv.getKey(), v);
    }
    return ret;
  }
  public static Tuple2<String, String> word2columnVal(String word ) {
    Iterable<String> tokens = Splitter.on(":").split(word);
    String column = Iterables.getFirst(tokens, null);
    if(column != null) {
      return new Tuple2<>(column, Joiner.on(":").join(Iterables.skip(tokens, 1)));
    }
    return null;
  }
  public static List<Tuple2<String, Double> > getSynonyms(Word2VecModel model, String word, double scoreCutoff) {
    List<Tuple2<String, Double>> ret = new ArrayList<>();
    try {
      for (Tuple2<String, Object> r : model.findSynonyms(word, 10)) {
        String w = r._1;
        Double score = Double.parseDouble(r._2.toString());
        boolean eitherNonNumbers = (ConversionUtils.convert(word, Double.class) == null || ConversionUtils.convert(w, Double.class) == null );
        boolean neitherNull = !StringUtils.isEmpty(word) && !StringUtils.isEmpty(w) && !word.trim().equals("null") && !w.trim().equals("null");
        if(score > scoreCutoff && eitherNonNumbers && neitherNull) {
            ret.add(new Tuple2<>(w, score));
        }
      }
    }
    catch(IllegalStateException ise) {
      //in this situation we want to skip.  Generally this means that a  word isn't in the vocabulary
    }
    return ret;
  }
}
