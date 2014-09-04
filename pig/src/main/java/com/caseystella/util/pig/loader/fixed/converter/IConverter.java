package com.caseystella.util.pig.loader.fixed.converter;

import com.caseystella.util.pig.loader.fixed.Field;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public interface IConverter {
    public Object convert(ByteBuffer bytes, Field.Type type, Map<String, Object> config);
    public void validate(Map<String, Object> config);
}
