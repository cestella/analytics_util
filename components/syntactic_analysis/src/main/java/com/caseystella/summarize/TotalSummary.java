package com.caseystella.summarize;

import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TotalSummary implements Serializable {
  Map<String, Summary> columnSummaries = new HashMap<>();
  List<Map<String, Object> > connectedColumns = new ArrayList<>();


  public Map<String, Summary> getColumnSummaries() {
    return columnSummaries;
  }

  public void setColumnSummaries(Map<String, Summary> columnSummaries) {
    this.columnSummaries = columnSummaries;
  }

  public List<Map<String, Object>> getConnectedColumns() {
    return connectedColumns;
  }

  public void setConnectedColumns(List<Map<String, Object>> connectedColumns) {
    this.connectedColumns = connectedColumns;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TotalSummary that = (TotalSummary) o;

    if (getColumnSummaries() != null ? !getColumnSummaries().equals(that.getColumnSummaries()) : that.getColumnSummaries() != null)
      return false;
    return getConnectedColumns() != null ? getConnectedColumns().equals(that.getConnectedColumns()) : that.getConnectedColumns() == null;

  }

  @Override
  public int hashCode() {
    int result = getColumnSummaries() != null ? getColumnSummaries().hashCode() : 0;
    result = 31 * result + (getConnectedColumns() != null ? getConnectedColumns().hashCode() : 0);
    return result;
  }

  public static List<Map<String, Object>> toConnectedColumns(List<Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double>> g_score_outliers) {
    List<Map<String, Object> > connectedColumns = new ArrayList<>();
    for(Tuple2<Tuple2<TypedColumnWithModifierAndValue, TypedColumnWithModifierAndValue>, Double> score : g_score_outliers) {
      int comp = score._1._1.column.compareTo(score._1._2.column);
      String col1 = comp < 0?score._1._1.column:score._1._2.column;
      String col2 = comp > 0?score._1._1.column:score._1._2.column;
      Map<String, Object> obj = new HashMap<>();
      obj.put("column 1", col1);
      obj.put("column 2", col2);
      obj.put("score", score._2);
      connectedColumns.add(obj);
    }
    return connectedColumns;
  }
}
