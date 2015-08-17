package com.caseystella.predict.udf;

import com.caseystella.predict.FeatureImportance;
import com.caseystella.predict.feature.FeatureMatrix;
import com.caseystella.predict.feature.FeatureType;
import com.caseystella.util.pig.Helper;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import weka.core.Instances;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/16/15.
 */
public class FEATURE_IMPORTANCE extends EvalFunc<DataBag> {
    Map<String, Integer> fieldToPosition = Maps.newHashMap();
    FeatureMatrix matrix;
    String featureMatrixLoc;
    ToArff toArff = new ToArff();
    public FEATURE_IMPORTANCE(String featureMatrixLoc)
    {
        this.featureMatrixLoc = featureMatrixLoc;
    }

    @Override
    public DataBag exec(Tuple tuple) throws IOException {
        if(fieldToPosition.isEmpty())
        {
            loadCols(getInputSchema().getField(1).schema.getField(0).schema);
        }
        String targetVariable = (String)tuple.get(0);
        boolean isRegression = true;
        for(FeatureMatrix.Feature f : matrix.getFeatures())
        {
            if(f.getColumn().equals(targetVariable))
            {
                if(f.getType() == FeatureType.CATEGORICAL)
                {
                    isRegression = false;
                    break;
                }
            }
        }
        DataBag ret = DefaultBagFactory.getInstance().newDefaultBag();
        DataBag data = (DataBag) tuple.get(1);
        Instances instances = toArff.getInstances(data, matrix, targetVariable, fieldToPosition);
        List<FeatureImportance.Importance> importances = null;
        try {
            importances = new FeatureImportance().findImportance(instances, matrix.getAttributes(targetVariable), isRegression);
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute feature importance.", e);
        }
        //System.out.println(importances);
        for(FeatureImportance.Importance i : importances)
        {
            Tuple t = DefaultTupleFactory.getInstance().newTuple(2);
            t.set(0, i.feature);
            t.set(1, i.importanceRank);
            ret.add(t);
        }
        return ret;
    }

    public void loadCols(Schema inputSchema)
    {
        try {

            String featureMatrixStr = IOUtils.toString(Helper.open(featureMatrixLoc, FEATURE_IMPORTANCE.class));
            matrix = FeatureMatrix.create(featureMatrixStr);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open " + featureMatrixLoc, e);
        }
        int i = 0;
        for(Schema.FieldSchema fs : inputSchema.getFields())
        {
            fieldToPosition.put(fs.alias, i);
            ++i;
        }
    }
    public Schema outputSchema(Schema inputSchema)
    {
        try {
            Helper.addFileToContext(featureMatrixLoc, FEATURE_IMPORTANCE.class);
            loadCols(inputSchema.getField(1).schema.getField(0).schema);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open " + featureMatrixLoc, e);
        }

        Schema bagSchema = new Schema();
        try
        {
            bagSchema.add(new Schema.FieldSchema("column", DataType.CHARARRAY));
            bagSchema.add(new Schema.FieldSchema("importance", DataType.DOUBLE));
            return new Schema(new Schema.FieldSchema(getSchemaName("ml", inputSchema), bagSchema,
                    DataType.BAG));
        } catch (FrontendException e) {
            throw new RuntimeException("Unable to create metadata bag schema.", e);
        }

    }
}
