package com.caseystella.type.udf;

import com.caseystella.type.Type;
import com.caseystella.type.ValueSummary;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/10/15.
 */
public class PIVOT extends EvalFunc<DataBag> {
    TupleFactory tupleFactory = TupleFactory.getInstance();
    BagFactory bagFactory = BagFactory.getInstance();
    Map<Integer, String> columns = new LinkedHashMap<Integer, String>();
    Map<Integer, Byte> columnToType = new LinkedHashMap<Integer, Byte>();
    @Override
    public DataBag exec(Tuple objects) throws IOException {
        if(columns.size() == 0)
        {
            loadCols(getInputSchema());
        }
        DataBag ret = bagFactory.newDefaultBag();
        for(int i = 0;i < objects.size();++i)
        {
            if(!columns.containsKey(i))
            {
                continue;
            }
            Tuple t = tupleFactory.newTuple(5);

            Object o = objects.get(i);
            String colName = columns.get(i);
            ValueSummary summary = Type.summarize(colName, columnToType.get(i), o);

            int idx = 0;
            t.set(idx++, colName);
            t.set(idx++, summary.baseType.toString());
            t.set(idx++, summary.inferredType.toString());
            t.set(idx++, summary.value);
            t.set(idx++, summary.canonicalValue);
            ret.add(t);
        }
        return ret;
    }
    public void loadCols(Schema inputSchema)
    {
        int i = 0;
        for(Schema.FieldSchema fs : inputSchema.getFields())
        {

            if(Type.fromPigType(fs.type) != null)
            {
                columns.put(i, fs.alias);
                columnToType.put(i, fs.type);
            }
            ++i;
        }
    }
    public Schema outputSchema(Schema inputSchema)
    {
        loadCols(inputSchema);

        Schema bagSchema = new Schema();
        try
        {
            bagSchema.add(new Schema.FieldSchema("column", DataType.CHARARRAY));
            bagSchema.add(new Schema.FieldSchema("type", DataType.CHARARRAY));
            bagSchema.add(new Schema.FieldSchema("inferred_type", DataType.CHARARRAY));
            bagSchema.add(new Schema.FieldSchema("value", DataType.CHARARRAY));
            bagSchema.add(new Schema.FieldSchema("canonical_value", DataType.CHARARRAY));
            return new Schema(new Schema.FieldSchema(getSchemaName("extracted", inputSchema), bagSchema,
                    DataType.BAG));
        } catch (FrontendException e) {
            throw new RuntimeException("Unable to create metadata bag schema.", e);
        }

    }
}
