package com.caseystella.input;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;

import java.util.Map;

public enum Mode implements InputHandler{
   SQL(new SQLHandler())
  ,CSV(new CSVHandler())
  ;
  InputHandler handler;
  Mode(InputHandler handler) {
    this.handler = handler;
  }

  @Override
  public DataFrame open(String inputName, JavaSparkContext sc, Map<String, String> properties) {
    return handler.open(inputName, sc, properties);
  }
}
