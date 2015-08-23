package com.caseystella.type;

import org.apache.pig.data.DataType;

/**
 * Created by cstella on 8/13/15.
 */
public class IntegralType  implements ITypeHandler {
    @Override
    public ValueSummary summarize(String name, Object value) {
        ValueSummary summary = new ValueSummary();
        summary.baseType = Type.INTEGRAL;
        if(value == null)
        {
            summary.inferredType = Type.MISSING;
            summary.canonicalValue = null;
            summary.value = null;
        }
        else {
            summary.inferredType = Type.INTEGRAL;
            summary.canonicalValue = canonicalize(value.toString());
            summary.value = value.toString();
        }

        return summary;
    }

    @Override
    public boolean isLikely(String opaqueValue) {
        try {
            Integer.parseInt(opaqueValue);
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
        return new byte[] { DataType.BYTE
                , DataType.INTEGER
                , DataType.LONG
        };
    }

}
