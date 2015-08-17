package com.caseystella.type;

import org.apache.pig.data.DataType;

import java.util.Arrays;

/**
 * Created by cstella on 8/13/15.
 */
public class StringType implements ITypeHandler {
    @Override
    public ValueSummary summarize(String name, Object value) {
        ValueSummary ret = new ValueSummary();
        ret.baseType = Type.STRING;
        if(value == null || value.toString().trim().length() == 0)
        {
            ret.inferredType = Type.MISSING;
            ret.canonicalValue = null;
            ret.value = null;
        }
        else
        {
            ret.inferredType = Type.STRING;
            String val = value.toString();
            ret.canonicalValue = canonicalize(val);
            ret.value = val;
            for(Type t : Arrays.asList(Type.INTEGRAL, Type.FLOAT, Type.DATE))
            {
                if(t._handler.isLikely(val))
                {
                    if(t == Type.FLOAT && Double.isNaN(Double.parseDouble(val)))
                    {
                        ret.inferredType = Type.MISSING;
                        ret.canonicalValue = null;
                    }
                    else
                    {
                        ret.inferredType = t;
                        ret.canonicalValue = t._handler.canonicalize(val);
                    }
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean isLikely(String opaqueValue) {
        return false;
    }

    @Override
    public String canonicalize(String base) {
        return base.toLowerCase();
    }

    @Override
    public byte[] supportedPigTypes() {
        return new byte[]{DataType.CHARARRAY};
    }
}
