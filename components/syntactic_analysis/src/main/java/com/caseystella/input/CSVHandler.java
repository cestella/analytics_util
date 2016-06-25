package com.caseystella.input;

import com.caseystella.util.ConversionUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.*;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CSVHandler implements InputHandler {
  public enum Options {
    QUERY("query")
    ,HAS_HEADER("hasHeader")
    ,INFER_SCHEMA("inferSchema" )
    ,EXPLICIT_SCHEMA("withSchema" )
    ;
    String optionName;
    Options(String optionName) {
      this.optionName = optionName;
    }
    public  String get(Map<String, String> properties) {
      return properties.get(optionName);
    }
    public <T> T get(Map<String, String> properties, T defaultVal, Class<T> clazz) {
      String s = properties.get(optionName);
      if(s == null) {
        return defaultVal;
      }
      T ret = ConversionUtils.convert(s, clazz);
      if(ret == null) {
        return defaultVal;
      }
      return ret;
    }

    public boolean has(Map<String, String> properties) {
      return properties.containsKey(optionName);
    }
  }
  public enum SqlTypes {
    STRING(DataTypes.StringType),
    INTEGER(DataTypes.IntegerType),
    DATE(DataTypes.DateType),
    DOUBLE(DataTypes.DoubleType),
    FLOAT(DataTypes.FloatType),
    LONG(DataTypes.LongType);
    DataType dt;
    SqlTypes(DataType dt) {
      this.dt = dt;
    }

    public DataType getDataType() {
      return dt;
    }
  }
  private StructType customSchema(String schemaDef) {
    List<Tuple2<String, DataType>> schema = new ArrayList<>();
    for(String i : Splitter.on(",").split(schemaDef)) {
      String columnName = i;
      DataType dt = DataTypes.StringType;
      if(i.contains(":")) {
        Iterable<String> tokens = Splitter.on(":").split(i);
        columnName = Iterables.getFirst(tokens, null);
        dt = SqlTypes.valueOf(Iterables.getLast(tokens, "").toUpperCase()).getDataType();
      }
      schema.add(new Tuple2<>(columnName, dt));
    }
    StructField[] fields = new StructField[schema.size()];
    for(int i = 0;i < schema.size();++i) {
      Tuple2<String, DataType> s = schema.get(i);
      fields[i] = new StructField(s._1, s._2, true, Metadata.empty());
    }
    return new StructType(fields);
  }
  @Override
  public DataFrame open(String inputName, JavaSparkContext sc, Map<String, String> properties) {
    SQLContext sqlContext = new SQLContext(sc);
    DataFrameReader reader = sqlContext.read()
                                       .format("com.databricks.spark.csv")
                                       .option("header", Options.HAS_HEADER.get(properties, "true", String.class))
                                       .option("inferSchema", Options.INFER_SCHEMA.get(properties, "true", String.class))
                                       ;
    if(Options.EXPLICIT_SCHEMA.has(properties)) {
      reader = reader.schema(customSchema(Options.EXPLICIT_SCHEMA.get(properties)));
    }
    if(Options.QUERY.has(properties)) {
      DataFrame df = reader.load(inputName);
      String tableName = Iterables.getFirst(Splitter.on('.').split(Iterables.getLast(Splitter.on('/').split(inputName), inputName)), inputName);
      System.out.println("Registering " + tableName + "...");
      df.registerTempTable(tableName);
      return df.sqlContext().sql(Options.QUERY.get(properties));
    }
    else {
      return reader.load(inputName);
    }
  }
}
