package com.caseystella.type;

/**
 * Created by cstella on 8/13/15.
 */
public interface ITypeHandler extends ITypeClassifier {
    public ValueSummary summarize(String name, Object value);
}
