package com.caseystella.type;

import org.apache.pig.data.DataType;

/**
 * Created by cstella on 8/13/15.
 */
public class FloatType implements ITypeHandler {
    @Override
    public ValueSummary summarize(String name, Object value) {
        ValueSummary summary = new ValueSummary();
        summary.baseType = Type.FLOAT;
        if(value == null || Double.isNaN(((Number)value).doubleValue()))
        {
            summary.inferredType = Type.MISSING;
            summary.canonicalValue = null;
            summary.value = null;
        }
        else {
            summary.inferredType = Type.FLOAT;
            summary.canonicalValue = canonicalize(value.toString());
            summary.value = value.toString();
        }
        return summary;
    }

    @Override
    public boolean isLikely(String opaqueValue) {
        try {
            Double.parseDouble(opaqueValue);
            return true;
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
    }

    @Override
    public String canonicalize(String base) {
        return base;
    }

    @Override
    public byte[] supportedPigTypes() {
        return new byte[] {
                DataType.FLOAT, DataType.DOUBLE
        };
    }

}
