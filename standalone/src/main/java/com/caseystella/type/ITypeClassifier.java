package com.caseystella.type;

import java.util.List;

/**
 * Created by cstella on 8/13/15.
 */
public interface ITypeClassifier {
    public boolean isLikely(String opaqueValue);
    public String canonicalize(String base);
    public byte[] supportedPigTypes();
}
