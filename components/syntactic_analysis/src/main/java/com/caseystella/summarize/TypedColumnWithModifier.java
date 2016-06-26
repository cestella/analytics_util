package com.caseystella.summarize;

import com.caseystella.type.TypeInference;

public class TypedColumnWithModifier extends TypedColumn {
  TypeInference.Modifier modifier;

  @Override
  public String toString() {
    return "TypedColumnWithModifier{" +
            "modifier=" + modifier +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TypedColumnWithModifier that = (TypedColumnWithModifier) o;

    return modifier == that.modifier;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
    return result;
  }

  public TypedColumnWithModifier(String column, TypeInference.Type type, TypeInference.Modifier modifier) {
    super(column, type);
    this.modifier = modifier;

  }

  public TypedColumnWithModifierAndValue withValue(Object o) {
    return withValue(o, true);
  }
  public TypedColumnWithModifierAndValue withValue(Object o, boolean canonicalize) {
    return new TypedColumnWithModifierAndValue(this, o, canonicalize);
  }

}
