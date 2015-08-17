package com.caseystella.predict.udf;

import com.caseystella.predict.Constants;
import com.caseystella.predict.feature.FeatureMatrix;
import com.caseystella.predict.feature.FeatureType;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import weka.core.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by cstella on 8/15/15.
 */
public class ToArff {
    public static class TO_INSTANCE implements Function<Tuple, Instance>
    {
        private FeatureMatrix fm;
        private String targetVariable;
        private ArrayList<Attribute> attributes;
        private Map<String, Integer> columnToPosition;
        private Map<String, Integer> attributeNameToPosition;
        public TO_INSTANCE(FeatureMatrix fm, ArrayList<Attribute> attributes, String targetVariable, Map<String, Integer> columnToPosition)
        {
            this.fm = fm;
            this.targetVariable = targetVariable;
            this.attributes = attributes;
            this.columnToPosition = columnToPosition;
            this.attributeNameToPosition = Maps.newHashMap();
            int i = 0;
            for(Attribute attribute : attributes)
            {
                attributeNameToPosition.put(attribute.name(), i++);
            }
        }
        public Double getNumericValue(FeatureMatrix.Feature feature, Object value, boolean isTargetVariable)
        {
            if(value == null)
            {
                if(isTargetVariable)
                {
                    return null;
                }
                else
                {
                    return Utils.missingValue();
                }
            }
            Double d = null;
            try
            {
                d = Double.parseDouble(value.toString());
                return d;
            }
            catch(NumberFormatException nfe)
            {
                return Utils.missingValue();
            }
        }

        public Map<String, Double> getCategoricalValue(FeatureMatrix.Feature feature, Object value, boolean isTargetVariable)
        {
            if(value == null && isTargetVariable)
            {
                return null;
            }
            Map<String, Double> ret = Maps.newHashMap();
            ArrayList<String> categories = Lists.newArrayList((ArrayList<String>) feature.getConfig().get("categories"));
            boolean foundCategory = false;
            for(int categoryId = 0;categoryId < categories.size() ;++categoryId)
            {
                String category = categories.get(categoryId);
                if(isTargetVariable)
                {

                    if(value.toString().equals(category))
                    {
                        ret.put(feature.getColumn(), (double)categoryId);
                        foundCategory = true;
                        break;
                    }
                }
                else
                {
                    if(value == null)
                    {
                        ret.put(feature.getColumn() + Constants.ONE_HOT_ENCODING_SEPARATOR + category, Utils.missingValue());
                        foundCategory = true;
                    }
                    if(value.toString().equals(category))
                    {
                       ret.put(feature.getColumn() + Constants.ONE_HOT_ENCODING_SEPARATOR + category, 1.0d);
                        foundCategory = true;
                    }
                    else
                    {
                        ret.put(feature.getColumn() + Constants.ONE_HOT_ENCODING_SEPARATOR + category, 0d);
                        foundCategory = true;
                    }
                }
            }
            double missing = foundCategory?1.0:0.0;
            if(!foundCategory && isTargetVariable)
            {
                return null;
            }
            if (!isTargetVariable) {
                ret.put(feature.getColumn() + Constants.ONE_HOT_ENCODING_SEPARATOR + "missing", missing);
            }
            return ret;
        }

        @Override
        public Instance apply(Tuple objects) {
            double[] ret = new double[attributes.size()];
            int featuresFilledIn = 0;
            for(FeatureMatrix.Feature feature : fm.getFeatures())
            {
                boolean isTargetVariable = feature.getColumn().equals(targetVariable);

                Object o = null;
                try {
                    Integer pos = columnToPosition.get(feature.getColumn());
                    if(pos == null)
                    {
                        throw new IllegalStateException("Unable to find column " + feature.getColumn() + " in the pig schema: "
                                + Joiner.on("\n").join(columnToPosition.entrySet()));
                    }
                    o = objects.get(columnToPosition.get(feature.getColumn()));
                } catch (ExecException e) {
                    throw new IllegalStateException(e);
                }
                if(feature.getType() == FeatureType.CATEGORICAL)
                {
                    Map<String, Double> vals = getCategoricalValue(feature, o, isTargetVariable);
                    if(vals == null)
                    {
                        //skip this instance, the target variable is missing..derp.
                        throw new NoTargetVariableException(feature.getColumn());
                    }
                    for(Map.Entry<String, Double> entry : vals.entrySet())
                    {
                        int pos = attributeNameToPosition.get(entry.getKey());
                        ret[pos] = entry.getValue();
                        featuresFilledIn++;
                    }
                }
                else if(feature.getType() == FeatureType.FLOAT || feature.getType() == FeatureType.INTEGRAL)
                {
                    Double val = getNumericValue(feature, o, isTargetVariable);
                    if(val == null)
                    {
                        throw new NoTargetVariableException(feature.getColumn());
                    }
                    ret[attributeNameToPosition.get(feature.getColumn())] = val;
                    featuresFilledIn++;
                }
            }
            if(featuresFilledIn != ret.length)
            {
                throw new IllegalStateException("Could not find all features.  Found " + featuresFilledIn + " != " + ret.length);
            }
            return new DenseInstance(1.0, ret);
        }
    }

    public static class NoTargetVariableException extends RuntimeException
    {
        public NoTargetVariableException(String name)
        {
            super("Could not find target variable " + name);
        }

    }
    public Instances getInstances(Iterable<Tuple> bag, FeatureMatrix matrix, String targetVariable, Map<String, Integer> columnToPositions)
    {
        ArrayList<Attribute> attributes = matrix.getAttributes(targetVariable);
        Instances instances =  new Instances(targetVariable, attributes, 0);
        for(Instance instance : Iterables.transform(bag, new TO_INSTANCE(matrix, attributes, targetVariable, columnToPositions)))
        {
            try
            {
                instances.add(instance);
            }
            catch(NoTargetVariableException ntve)
            {
                //skip
            }
        }
        return instances;
    }
}
