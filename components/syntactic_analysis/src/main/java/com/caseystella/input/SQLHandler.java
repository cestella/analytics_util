package com.caseystella.input;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;

import java.util.Map;

public class SQLHandler implements InputHandler {
  public static final String SQL_TYPE_CONF = "sqlType";
  @Override
  public DataFrame open(String inputName, JavaSparkContext sc, Map<String, String> properties) {
    String sqlTypeObj = properties.get(SQL_TYPE_CONF);
    if(sqlTypeObj != null && sqlTypeObj.equalsIgnoreCase("hive")) {
      HiveContext hc = new org.apache.spark.sql.hive.HiveContext(sc);
      return hc.sql(inputName);
    }
    else {
      SQLContext sqlContext = new SQLContext(sc);
      return sqlContext.sql(inputName);
    }
  }
}
