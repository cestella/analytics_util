package com.caseystella.util.common.interpret.xpath;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by cstella on 9/4/14.
 */
public class Config {
    private static ObjectMapper mapper = new ObjectMapper();
    private Field[] fields;

    public Field[] getFields() {
        return fields;
    }

    public void setFields(Field[] fields) {
        this.fields = fields;
    }
    public static Config load(Reader input) throws IOException {
        Config config = mapper.readValue(input, new TypeReference<Config>(){});
        return config;
    }

    public Map<String, String> getContent(Document doc) throws JDOMException {
        LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        for(Field f : getFields())
        {
            String val = f.extractXPath(doc);
            ret.put(f.getName(), val);
        }
        return ret;
    }


}
