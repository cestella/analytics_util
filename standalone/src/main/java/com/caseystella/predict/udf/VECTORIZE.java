package com.caseystella.predict.udf;

import com.caseystella.predict.feature.FeatureMatrix;
import com.caseystella.util.pig.Helper;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/17/15.
 */
public class VECTORIZE extends EvalFunc<DataBag> {
    Map<String, Integer> fieldToPosition = Maps.newHashMap();
    FeatureMatrix matrix;
    String featureMatrixLoc;
    ToArff toArff = new ToArff();
    public VECTORIZE(String featureMatrixLoc)
    {
        this.featureMatrixLoc = featureMatrixLoc;
    }

    public void loadCols(Schema inputSchema)
    {
        try {

            String featureMatrixStr = IOUtils.toString(Helper.open(featureMatrixLoc, VECTORIZE.class));
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

    @Override
    public DataBag exec(Tuple tuple) throws IOException {
        if(fieldToPosition.isEmpty())
        {
            loadCols(getInputSchema().getField(0).schema.getField(0).schema);
        }
        DataBag data = (DataBag) tuple.get(0);
        Instances instances = toArff.getInstances(data, matrix, "", fieldToPosition);
        List<Attribute> attributes = matrix.getAttributes("");
        DataBag ret = DefaultBagFactory.getInstance().newDefaultBag();
        for(Instance instance : instances)
        {
            Tuple t = DefaultTupleFactory.getInstance().newTuple(attributes.size());
            for(int i = 0;i < attributes.size();++i) {
                t.set(i, instance.value(i));
            }
            ret.add(t);
        }
        return ret;
    }

    public Schema outputSchema(Schema inputSchema)
    {
        try {
            Helper.addFileToContext(featureMatrixLoc, VECTORIZE.class);
            loadCols(inputSchema.getField(0).schema.getField(0).schema);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open " + featureMatrixLoc, e);
        }

        Schema tupleSchema = new Schema();
        try
        {
            for(Attribute att : matrix.getAttributes("")) {
                tupleSchema.add(new Schema.FieldSchema(att.name(), DataType.DOUBLE));
            }
            return new Schema(new Schema.FieldSchema(getSchemaName("ml", inputSchema), tupleSchema,
                    DataType.TUPLE));
        } catch (FrontendException e) {
            throw new RuntimeException("Unable to create metadata bag schema.", e);
        }

    }
}
