package com.caseystella.util.common.interpret.fixed.converter;


import com.caseystella.util.common.interpret.fixed.Field;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public class BinaryConverter implements IConverter{
    public static String ORDER_PARAM = "order";
    @Override
    public Object convert(ByteBuffer bytes, Field.Type type, Map<String, Object> config) {
        switch(type)
        {
            case INT:
                return bytes.asIntBuffer().get();
            case FLOAT:
                return bytes.asFloatBuffer().get();
            case DOUBLE:
                return bytes.asDoubleBuffer().get();
            case STRING:
                return bytes.asCharBuffer().toString();
            case BYTES:
                return bytes.compact();
            default:
                throw new UnsupportedOperationException("Type " + type + " unsupported for " + getClass());
        }
    }

    @Override
    public void validate(Map<String, Object> config) {

    }
}
