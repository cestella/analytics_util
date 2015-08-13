package com.caseystella.pmml.config;

import com.caseystella.pmml.provider.ConsumerProvider;
import com.caseystella.pmml.provider.DataBinding;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by cstella on 1/30/15.
 */
public class Config<BINDING_T extends DataBinding<?>> {
    private static ObjectMapper mapper = new ObjectMapper();
    Model model;
    Consumer consumer;
    BINDING_T binding;

    public BINDING_T getBinding() {
        return binding;
    }

    public void setBinding(BINDING_T binding) {
        this.binding = binding;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public ConsumerProvider getConsumerProvider() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return getConsumer().getConsumer(getModel());
    }


    public void validate() throws IllegalStateException
    {
    }

}
