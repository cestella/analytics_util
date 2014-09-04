package com.caseystella.util.common.interpret.fixed.converter;


import com.caseystella.util.common.interpret.fixed.Field;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public interface IConverter {
    public Object convert(ByteBuffer bytes, Field.Type type, Map<String, Object> config);
    public void validate(Map<String, Object> config);
}
