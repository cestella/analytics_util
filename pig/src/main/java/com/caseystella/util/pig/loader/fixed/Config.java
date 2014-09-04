package com.caseystella.util.pig.loader.fixed;

import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.Reader;
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

}
