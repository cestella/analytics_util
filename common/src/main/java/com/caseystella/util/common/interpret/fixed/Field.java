package com.caseystella.util.common.interpret.fixed;

import com.caseystella.util.common.interpret.fixed.converter.Converter;

import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public class Field
{
    public static enum Type
    {
        BYTES
       ,STRING
       ,INT
       ,FLOAT
       ,DOUBLE
        ;

    }
    int width;
    int offset;
    String name;
    Type type;
    Converter converter;
    Map<String, Object> config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }
    public void validate()
    {
        getConverter().validate(getConfig());
    }
}
