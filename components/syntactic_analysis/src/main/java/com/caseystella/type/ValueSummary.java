package com.caseystella.type;

public class ValueSummary {
  Object value;
  TypeInference.Type type;
  TypeInference.Modifier modifier;
  private ValueSummary(Object value, TypeInference.Type type, TypeInference.Modifier modifier) {
    this.value = value;
    this.type = type;
    this.modifier = modifier;
  }
  public static ValueSummary of(Object value, TypeInference.Type type, TypeInference.Modifier modifier) {
    return new ValueSummary(value, type, modifier);
  }

  public Object getValue() {
    return value;
  }

  public TypeInference.Type getType() {
    return type;
  }

  public TypeInference.Modifier getModifier() {
    return modifier;
  }

  @Override
  public String toString() {
    return "ValueSummary{" +
            "value=" + value +
            ", type=" + type +
            ", modifier=" + modifier +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ValueSummary that = (ValueSummary) o;

    if (value != null ? !value.toString().equals(that.value.toString()) : that.value != null) return false;
    if (type != that.type) return false;
    return modifier == that.modifier;

  }

  @Override
  public int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
    return result;
  }
}
