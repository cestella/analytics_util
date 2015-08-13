package com.caseystella.pmml.provider;

import java.io.InputStream;

/**
 * Created by cstella on 1/30/15.
 */
public interface ModelProvider {
    public InputStream getModel(String modelIdentifier);

}
