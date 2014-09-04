package com.caseystella.util.common.interpret.fixed.converter;


import com.caseystella.util.common.interpret.fixed.Field;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public class TextConverter implements IConverter {
    public static String ENCODING_PARAM = "encoding";
    @Override
    public Object convert(ByteBuffer bytes, Field.Type type, Map<String, Object> config) {
        Charset charset = Charset.defaultCharset();
        if(config != null && config.containsKey(ENCODING_PARAM))
        {
            charset = Charset.forName((String)config.get(ENCODING_PARAM));
        }
        String str = charset.decode(bytes).toString();
        switch(type)
        {
            case INT:
                return Integer.parseInt(str.trim());
            case FLOAT:
                return Float.parseFloat(str.trim());
            case DOUBLE:
                return Double.parseDouble(str.trim());
            case STRING:
                return str;
            case BYTES:
                return bytes.compact();
            default:
                throw new UnsupportedOperationException("Type " + type + " unsupported for " + getClass());
        }
    }

    @Override
    public void validate(Map<String, Object> config) {
        if(config  != null && config.containsKey(ENCODING_PARAM))
        {
            if(Charset.forName((String)config.get(ENCODING_PARAM)) == null)
            {
                throw new RuntimeException("Unable to find encoding: " + config.get(ENCODING_PARAM));
            }
        }
    }
}
