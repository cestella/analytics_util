package com.caseystella.summarize;

import com.caseystella.type.TypeInference;

import java.io.Serializable;

public class TypedColumn implements Serializable {
  String column;
  TypeInference.Type type;
  public TypedColumn(String column, TypeInference.Type type) {
    this.column = column;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TypedColumn that = (TypedColumn) o;

    if (column != null ? !column.equals(that.column) : that.column != null) return false;
    return type == that.type;

  }

  @Override
  public int hashCode() {
    int result = column != null ? column.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
