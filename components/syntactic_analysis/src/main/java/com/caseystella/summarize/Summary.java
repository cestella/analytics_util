package com.caseystella.summarize;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Created by cstella on 8/14/15.
 */
public class Summary {
  List<Map<String, Object>> countByType = new ArrayList<>();
  List<Map<String, Object>> countDistinctByType = new ArrayList<>();
  List<Map<String, Object>> numericValueSummary =  new ArrayList<>();
  List<Map<String, Object>> nonNumericValueSummary = new ArrayList<>();
  Map<String, String> synonyms = new HashMap<>();
  Long numInvalid = 0L;
  Long totalCount = 0L;

  public Long getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Long totalCount) {
    this.totalCount = totalCount;
  }

  public Long getNumInvalid() {
    return numInvalid;
  }

  public void setNumInvalid(Long numInvalid) {
    this.numInvalid = numInvalid;
  }

  public Map<String, String> getSynonyms() {
    return synonyms;
  }

  public void setSynonyms(Map<String, String> synonyms) {
    this.synonyms = synonyms;
  }

  public List<Map<String, Object>> getCountByType() {
    return countByType;
  }

  public void setCountByType(List<Map<String, Object>> countByType) {
    this.countByType = countByType;
  }

  public List<Map<String, Object>> getCountDistinctByType() {
    return countDistinctByType;
  }

  public void setCountDistinctByType(List<Map<String, Object>> countDistinctByType) {
    this.countDistinctByType = countDistinctByType;
  }

  public List<Map<String, Object>> getNumericValueSummary() {
    return numericValueSummary;
  }

  public void setNumericValueSummary(List<Map<String, Object>> numericValueSummary) {
    this.numericValueSummary = numericValueSummary;
  }

  public List<Map<String, Object>> getNonNumericValueSummary() {
    return nonNumericValueSummary;
  }

  public void setNonNumericValueSummary(List<Map<String, Object>> nonNumericValueSummary) {
    this.nonNumericValueSummary = nonNumericValueSummary;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Summary summary = (Summary) o;

    if (countByType != null ? !countByType.equals(summary.countByType) : summary.countByType != null) return false;
    if (countDistinctByType != null ? !countDistinctByType.equals(summary.countDistinctByType) : summary.countDistinctByType != null)
      return false;
    if (numericValueSummary != null ? !numericValueSummary.equals(summary.numericValueSummary) : summary.numericValueSummary != null)
      return false;
    return nonNumericValueSummary != null ? nonNumericValueSummary.equals(summary.nonNumericValueSummary) : summary.nonNumericValueSummary == null;

  }

  @Override
  public int hashCode() {
    int result = countByType != null ? countByType.hashCode() : 0;
    result = 31 * result + (countDistinctByType != null ? countDistinctByType.hashCode() : 0);
    result = 31 * result + (numericValueSummary != null ? numericValueSummary.hashCode() : 0);
    result = 31 * result + (nonNumericValueSummary != null ? nonNumericValueSummary.hashCode() : 0);
    return result;
  }

  public static Map<String, List<Map<String, Object>>> countByColumn(Map<TypedColumnWithModifier, Long> countByType) {
    Map<String, List<Map<String, Object>>> ret = new HashMap<>();
    for(Map.Entry<TypedColumnWithModifier, Long> kv : countByType.entrySet()) {
      Map<String, Object> val = ImmutableMap.of("type", kv.getKey().type + ":" + kv.getKey().modifier
                                                 ,"count", (Object)kv.getValue()
                                               );
      List<Map<String, Object>> list = ret.get(kv.getKey().column);
      if(list == null) {
        list = new ArrayList<>();
      }
      list.add(val);
      ret.put(kv.getKey().column, list);
    }
    for(Map.Entry<String, List<Map<String, Object>>> kv : ret.entrySet()) {
      Collections.sort(kv.getValue(), new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
          return o1.get("type").toString().compareTo(o2.get("type").toString());
        }
      });
    }
    return ret;
  }
  public static Map<String, List<Map<String, Object>>> summaryToList(Map<TypedColumnWithModifier, Map<String, Double>> summary) {
    Map<String, List<Map<String, Object>>> ret = new HashMap<>();
    for(Map.Entry<TypedColumnWithModifier, Map<String, Double>> kv : summary.entrySet()) {
      Map<String, Object> val = ImmutableMap.of("type", kv.getKey().type + ":" + kv.getKey().modifier
                                                 ,"summary", kv.getValue()
                                               );
      List<Map<String, Object>> list = ret.get(kv.getKey().column);
      if(list == null) {
        list = new ArrayList<>();
      }
      list.add(val);
      ret.put(kv.getKey().column, list);
    }
    for(Map.Entry<String, List<Map<String, Object>>> kv : ret.entrySet()) {
      Collections.sort(kv.getValue(), new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
          return o1.get("type").toString().compareTo(o2.get("type").toString());
        }
      });
    }
    return ret;
  }
}
