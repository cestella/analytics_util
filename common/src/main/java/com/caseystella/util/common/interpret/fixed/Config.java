package com.caseystella.util.common.interpret.fixed;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public class Config {
    private static ObjectMapper mapper = new ObjectMapper();
    Field[] fields;

    public Field[] getFields() {
        return fields;
    }

    public void setFields(Field[] fields) {
        this.fields = fields;
    }

    public static Config load(Reader input) throws IOException {
        return mapper.readValue(input, new TypeReference<Config>(){});
    }
    public int computeWidth()
    {
        int width = 0;
        for(Field f : getFields())
        {
            width += f.getWidth();
        }
        return width;
    }

    public void validate()
    {
        for(Field f : getFields())
        {
            f.validate();
        }
    }

    public Map<String, Object> process(byte[] value)
    {
        LinkedHashMap<String, Object> ret = new LinkedHashMap<String, Object>();
        for(Field f : getFields())
        {
            ByteBuffer b = ByteBuffer.wrap(value, f.getOffset(), f.getWidth());
            ret.put(f.getName(), f.getConverter().convert(b, f.getType(), f.getConfig()));
        }
        return ret;
    }

}
