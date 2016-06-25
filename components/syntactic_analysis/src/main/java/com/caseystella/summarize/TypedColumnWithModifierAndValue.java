package com.caseystella.summarize;

public class TypedColumnWithModifierAndValue extends TypedColumnWithModifier {
  String value;
  public TypedColumnWithModifierAndValue(TypedColumnWithModifier tcwm, Object value) {
    super(tcwm.column, tcwm.type, tcwm.modifier);
    this.value = tcwm.type.getHandler().canonicalize(value + "");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TypedColumnWithModifierAndValue that = (TypedColumnWithModifierAndValue) o;

    return value != null ? value.equals(that.value) : that.value == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }
}
