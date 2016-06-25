package com.caseystella.type;


import com.google.common.base.Optional;

public interface TypeHandler {
  Optional<ValueSummary> summarize(Object o);
  String canonicalize(String o);

}
