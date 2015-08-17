package com.caseystella.predict.udf;

import com.google.common.collect.Maps;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.Map;

/**
 * Created by cstella on 8/16/15.
 */
public class OVERLAPPING_COLUMNS extends EvalFunc<DataBag> {
    double minimumInclusionThreshold = 0.5;
    public OVERLAPPING_COLUMNS()
    {

    }
    public OVERLAPPING_COLUMNS(String minimumInclusionThreshold)
    {
        this.minimumInclusionThreshold = Double.parseDouble(minimumInclusionThreshold);
    }

    @Override
    public DataBag exec(Tuple tuple) throws IOException {
        Map<String, Integer> columnToNumFold = Maps.newHashMap();
        DataBag ret = DefaultBagFactory.getInstance().newDefaultBag();
        int numFolds = 0;
        for(Tuple t : (DataBag)tuple.get(0))
        {
            DataBag db = (DataBag) t.get(0);
            for(Tuple result : db)
            {
                String col = (String) result.get(0);
                Integer num = columnToNumFold.get(col);
                if(num == null)
                {
                    num = 0;
                }
                columnToNumFold.put(col, num+1);
            }
            numFolds++;
        }
        for(Map.Entry<String, Integer> entry : columnToNumFold.entrySet())
        {
            double pctFolds = (1.0*entry.getValue())/numFolds;
            if(pctFolds > minimumInclusionThreshold)
            {
                Tuple t = DefaultTupleFactory.getInstance().newTuple(1);
                t.set(0, entry.getKey());
            }
        }

        return ret;
    }
    public Schema outputSchema(Schema inputSchema)
    {

        Schema bagSchema = new Schema();
        try
        {
            bagSchema.add(new Schema.FieldSchema("column", DataType.CHARARRAY));
            return new Schema(new Schema.FieldSchema(getSchemaName("ml", inputSchema), bagSchema,
                    DataType.BAG));
        } catch (FrontendException e) {
            throw new RuntimeException("Unable to create metadata bag schema.", e);
        }

    }
}
