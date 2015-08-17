package com.caseystella.predict.udf;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;

/**
 * Created by cstella on 8/16/15.
 */
public class GEN_FOLDS extends EvalFunc<DataBag> {
    int numFolds;
    public GEN_FOLDS(String numFolds)
    {
        this.numFolds = Integer.parseInt(numFolds);
    }
    @Override
    public DataBag exec(Tuple tuple) throws IOException {
        DataBag ret = DefaultBagFactory.getInstance().newDefaultBag();
        for(int i = 0;i < numFolds;++i)
        {
            Tuple t = DefaultTupleFactory.getInstance().newTuple(1);
            t.set(0, i);
            ret.add(t);
        }
        return ret;
    }

    public Schema outputSchema(Schema inputSchema)
    {

        Schema bagSchema = new Schema();
        try
        {
            bagSchema.add(new Schema.FieldSchema("fold", DataType.INTEGER));
            return new Schema(new Schema.FieldSchema(getSchemaName("folds", inputSchema), bagSchema,
                    DataType.BAG));
        } catch (FrontendException e) {
            throw new RuntimeException("Unable to create metadata bag schema.", e);
        }

    }
}
