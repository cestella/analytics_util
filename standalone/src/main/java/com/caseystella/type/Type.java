package com.caseystella.type;

import org.apache.pig.data.DataType;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cstella on 8/13/15.
 */
public enum Type {
     INTEGRAL(new IntegralType())
    ,FLOAT(new FloatType())
    ,DATE(new DateType())
    ,STRING(new StringType())
    ,MISSING(new ITypeHandler() {
        @Override
        public ValueSummary summarize(String name, Object value) {
            return null;
        }

        @Override
        public boolean isLikely(String opaqueValue) {
            return false;
        }

        @Override
        public String canonicalize(String base) {
            return null;
        }

        @Override
        public byte[] supportedPigTypes() {
            return new byte[0];
        }
    })
    ;
    static Map<Byte, Type> pigToType = new HashMap<Byte, Type>();
    static {
        for(int i = values().length - 1;i >= 0;i--)
        {
            Type t = values()[i];
            for(Byte b : t._handler.supportedPigTypes())
            {
                pigToType.put(b, t);
            }
        }
    }
    ITypeHandler _handler;
    Type(ITypeHandler handler)
    {
        _handler = handler;
    }

    public static ValueSummary summarize(String columnName, byte pigType, Object value)
    {
        Type baseType = fromPigType(pigType);
        if(baseType == null) {
            return null;
        }
        return baseType._handler.summarize(columnName, value);
    }

    public static Type fromPigType(byte pigType)
    {
        return pigToType.get(pigType);
    }
}
