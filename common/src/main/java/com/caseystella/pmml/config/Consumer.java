package com.caseystella.pmml.config;

import com.caseystella.pmml.provider.ConsumerProvider;

/**
 * Created by cstella on 1/30/15.
 */
public class Consumer {
    String consumerClass;

    public ConsumerProvider getConsumer(Model model) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ConsumerProvider provider = (ConsumerProvider)(Class.forName(consumerClass).newInstance());
        provider.initialize(model.getModelStream());
        return provider;
    }

    public String getConsumerClass() {
        return consumerClass;
    }

    public void setConsumerClass(String consumerClass) {
        this.consumerClass = consumerClass;
    }
}
