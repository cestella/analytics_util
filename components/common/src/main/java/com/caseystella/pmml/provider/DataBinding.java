package com.caseystella.pmml.provider;

import com.caseystella.pmml.provider.ConsumerProvider;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by cstella on 1/30/15.
 */
public interface DataBinding<INPUT_T> {
    Map<FieldName, FieldValue> convertRow(INPUT_T input);
    LinkedHashMap<String, ?> getOutputSchema(ConsumerProvider provider);
    LinkedHashMap<String, ?> getInputSchema(ConsumerProvider provider);

}
