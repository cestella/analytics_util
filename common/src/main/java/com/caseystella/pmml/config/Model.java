package com.caseystella.pmml.config;

import com.caseystella.pmml.provider.ModelProvider;

import java.io.InputStream;

/**
 * Created by cstella on 1/30/15.
 */
public class Model  {
    String modelProviderClass;
    String modelLocation;
    public ModelProvider modelProvider() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //TODO: fill me in
        ModelProvider provider = (ModelProvider)(Class.forName(modelProviderClass).newInstance());
        return null;
    }

    public String getModelProviderClass() {
        return modelProviderClass;
    }

    public void setModelProviderClass(String modelProviderClass) {
        this.modelProviderClass = modelProviderClass;
    }

    public String getModelLocation() {
        return modelLocation;
    }

    public void setModelLocation(String modelLocation) {
        this.modelLocation = modelLocation;
    }

    public InputStream getModelStream() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return modelProvider().getModel(getModelLocation());
    }


}
