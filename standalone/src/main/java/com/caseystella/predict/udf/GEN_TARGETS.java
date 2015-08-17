package com.caseystella.predict.udf;

import com.caseystella.predict.feature.FeatureMatrix;
import com.caseystella.util.pig.Helper;
import org.apache.commons.io.IOUtils;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;

/**
 * Created by cstella on 8/16/15.
 */
public class GEN_TARGETS extends EvalFunc<DataBag> {
    FeatureMatrix matrix;
    String featureMatrixLoc;
    public GEN_TARGETS(String featureMatrixLoc)
    {
        this.featureMatrixLoc = featureMatrixLoc;
    }
    @Override
    public DataBag exec(Tuple tuple) throws IOException {
        if(matrix == null)
        {
            loadMatrix();
        }
        DataBag ret = DefaultBagFactory.getInstance().newDefaultBag();
        for(FeatureMatrix.Feature feature : matrix.getFeatures())
        {
            Tuple t = DefaultTupleFactory.getInstance().newTuple(1);
            t.set(0, feature.getColumn());
            ret.add(t);
        }
        return ret;
    }

    public void loadMatrix()
    {
        try {

            String featureMatrixStr = IOUtils.toString(Helper.open(featureMatrixLoc, GEN_TARGETS.class));
            matrix = FeatureMatrix.create(featureMatrixStr);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open " + featureMatrixLoc, e);
        }
    }

    public Schema outputSchema(Schema inputSchema)
    {
        try {
            Helper.addFileToContext(featureMatrixLoc, GEN_TARGETS.class);
            loadMatrix();
        } catch (IOException e) {
            throw new RuntimeException("Unable to open " + featureMatrixLoc, e);
        }

        Schema bagSchema = new Schema();
        try
        {
            bagSchema.add(new Schema.FieldSchema("target", DataType.CHARARRAY));
            return new Schema(new Schema.FieldSchema(getSchemaName("ml", inputSchema), bagSchema,
                    DataType.BAG));
        } catch (FrontendException e) {
            throw new RuntimeException("Unable to create metadata bag schema.", e);
        }

    }
}
