package com.caseystella.predict.feature;

import com.caseystella.predict.Constants;
import com.google.common.collect.Lists;
import org.codehaus.jackson.map.ObjectMapper;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/14/15.
 */
public class FeatureMatrix {
    static ObjectMapper _mapper = new ObjectMapper();

    public static class Feature
    {
        FeatureType type;
        String column;
        Map<String, Object> config;

        public FeatureType getType() {
            return type;
        }

        public void setType(FeatureType type) {
            this.type = type;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public Map<String, Object> getConfig() {
            return config;
        }

        public void setConfig(Map<String, Object> config) {
            this.config = config;
        }
    }
    List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
    public static FeatureMatrix create(String s) throws IOException {
        return _mapper.readValue(s, FeatureMatrix.class);
    }

    public String writeToString() throws IOException {
        return _mapper.defaultPrettyPrintingWriter().writeValueAsString(this);
    }

    private static ArrayList<Attribute> featureToAttribute(Feature feature, boolean isTarget)
    {
        ArrayList<Attribute> ret = new ArrayList<Attribute>();
        if(feature.getType() == FeatureType.CATEGORICAL)
        {
            ArrayList<String> categories = Lists.newArrayList((ArrayList<String>) feature.getConfig().get("categories"));
            if(!isTarget)
            {
                for(String category : categories)
                {
                    ret.add(new Attribute(feature.getColumn() + Constants.ONE_HOT_ENCODING_SEPARATOR + category));
                }
                ret.add(new Attribute(feature.getColumn() + Constants.ONE_HOT_ENCODING_SEPARATOR + "missing"));
            }
            else
            {
                ret.add(new Attribute(feature.getColumn(), categories));
            }
        }
        else
        {
            ret.add(new Attribute(feature.getColumn()));
        }
        return ret;
    }
    public ArrayList<Attribute> getAttributes(String targetVariable)
    {
        ArrayList<Attribute> ret = Lists.newArrayList();
        Attribute targetAttribute = null;
        for(Feature feature : getFeatures())
        {
            if(!feature.getColumn().equals(targetVariable))
            {
                for(Attribute att: featureToAttribute(feature, false))
                {
                    ret.add(att);
                }

            }
            else
            {
                targetAttribute = featureToAttribute(feature, true).get(0);
            }
        }
        if(targetAttribute != null)
        {
            ret.add(targetAttribute);
        }
        else
        {
            throw new IllegalStateException("Must have a target attribute!");
        }
        return ret;
    }
}
