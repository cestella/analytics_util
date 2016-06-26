package com.caseystella.summarize;

import com.caseystella.util.JSONUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class SummarizerIntegrationTest {
  private transient JavaSparkContext sc;
  private transient SQLContext sqlContext;
  @Before
  public void setup() {
    SparkConf conf  = new SparkConf();
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
    sc = new JavaSparkContext("local", "JavaAPISuite", conf);
    sqlContext = new SQLContext(sc);
  }

  public <T> DataFrame createDf(List<T> data, Class<T> clazz) {
    JavaRDD<T> rdd = sc.parallelize(data);
    return sqlContext.createDataFrame(rdd, clazz);
  }

  public static class Row {
    String column1;
    String column2;
    String column3;
    String column4;
    String column5;
    public Row(String column1, String column2, String column3, String column4, String column5) {
      this.column1 = column1;
      this.column2 = column2;
      this.column3 = column3;
      this.column4 = column4;
      this.column5 = column5;
    }

    public String getColumn1() {
      return column1;
    }

    public void setColumn1(String column1) {
      this.column1 = column1;
    }

    public String getColumn2() {
      return column2;
    }

    public void setColumn2(String column2) {
      this.column2 = column2;
    }

    public String getColumn3() {
      return column3;
    }

    public void setColumn3(String column3) {
      this.column3 = column3;
    }

    public String getColumn4() {
      return column4;
    }

    public void setColumn4(String column4) {
      this.column4 = column4;
    }

    public String getColumn5() {
      return column5;
    }

    public void setColumn5(String column5) {
      this.column5 = column5;
    }

    @Override
    public String toString() {
      return "Row{" +
              "column1='" + column1 + '\'' +
              ", column2='" + column2 + '\'' +
              ", column3='" + column3 + '\'' +
              ", column4='" + column4 + '\'' +
              ", column5='" + column5 + '\'' +
              '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Row row = (Row) o;

      if (getColumn1() != null ? !getColumn1().equals(row.getColumn1()) : row.getColumn1() != null) return false;
      if (getColumn2() != null ? !getColumn2().equals(row.getColumn2()) : row.getColumn2() != null) return false;
      if (getColumn3() != null ? !getColumn3().equals(row.getColumn3()) : row.getColumn3() != null) return false;
      if (getColumn4() != null ? !getColumn4().equals(row.getColumn4()) : row.getColumn4() != null) return false;
      return getColumn5() != null ? getColumn5().equals(row.getColumn5()) : row.getColumn5() == null;

    }

    @Override
    public int hashCode() {
      int result = getColumn1() != null ? getColumn1().hashCode() : 0;
      result = 31 * result + (getColumn2() != null ? getColumn2().hashCode() : 0);
      result = 31 * result + (getColumn3() != null ? getColumn3().hashCode() : 0);
      result = 31 * result + (getColumn4() != null ? getColumn4().hashCode() : 0);
      result = 31 * result + (getColumn5() != null ? getColumn5().hashCode() : 0);
      return result;
    }
  }

  @Test
  public void test() throws JsonProcessingException {
    DataFrame df = createDf(ImmutableList.of(new Row("1", "1.5", "Dec 01, 1994", null, "foo")
                                            ,new Row("2", "-7.5", "20150204", "", null)
                                            ,new Row("3", "-2.5", "", "", null)
                                            ,new Row("NaN", "-2.5", "", "", null)
                                            ,new Row("pigeons", "-2.5", "", "", "several")
                                            ,new Row("null", "25", "", "", "foo")
                                            ,new Row("2.3", "-2.5", "", "", "casey")
                                            ,new Row("2.3", "-2", "", "", "foo")
                                            ,new Row("4", "-2", "", "", "several")
                                            ,new Row("4", "-2", "", "", "chicken")
                                            ,new Row("5", "-2", "", "", "chicken")
                                            ,new Row("5", "-2", "", "", "chicken")
                                            )
                           , Row.class
                           );
    TotalSummary summary = Summarizer.summarize(df, 5, 3, ImmutableList.of(10d, 25d, 50d, 75d, 95d, 99d), 5 );
    System.out.println(JSONUtils.INSTANCE.toJSON(summary, true));
  }

  @After
  public void shutdown() {
    sc.stop();
  }

}
