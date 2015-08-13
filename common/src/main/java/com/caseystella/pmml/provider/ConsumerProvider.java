package com.caseystella.pmml.provider;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldValue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 1/30/15.
 */
public interface ConsumerProvider {
    public List<FieldName> getActiveFields();
    public List<FieldName> getPredictedFields();
    public List<FieldName> getOutputFields();
    public Map<FieldName, ?> evaluate(Map<FieldName, FieldValue> arguments);
    public void initialize(InputStream modelInputStream);
}
