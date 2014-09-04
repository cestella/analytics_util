package com.caseystella.util.pig.udf;

import com.caseystella.util.common.enrich.ExtractContent;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by cstella on 9/3/14.
 */
public class ContentExtractor extends EvalFunc<Tuple> {
    TupleFactory tupleFactory;
    BagFactory bagFactory;
    public ContentExtractor()
    {
        tupleFactory = TupleFactory.getInstance();
        bagFactory = DefaultBagFactory.getInstance();
    }
    @Override
    public Tuple exec(Tuple objects) throws IOException {


        Map.Entry<String, Metadata> content ;
        try {
            content = ExtractContent.INSTANCE.extractTextWithMetadata((String)objects.get(0)
                                                                     ,((DataByteArray)objects.get(1)).get()
                                                                     );

        } catch (SAXException e) {
            throw new RuntimeException("Unable to parse.", e);
        } catch (TikaException e) {

            throw new RuntimeException("Unable to parse.", e);
        }
        DataBag metadataBag = bagFactory.newDefaultBag();
        Tuple ret = tupleFactory.newTuple(2);
        ret.set(0, content.getKey());
        for(String key : content.getValue().names())
        {
            String value = content.getValue().get(key);
            Tuple t = tupleFactory.newTuple(2);
            t.set(0, key);
            t.set(1, value);
            metadataBag.add(t);
        }
        ret.set(1, metadataBag);
        return ret;
    }

    public Schema outputSchema(Schema inputSchema)
    {
        Schema ret = new Schema();
        try
        {
            ret.add(new Schema.FieldSchema("content", DataType.CHARARRAY));
            Schema bagSchema = new Schema();
            bagSchema.add(new Schema.FieldSchema("key", DataType.CHARARRAY));
            bagSchema.add(new Schema.FieldSchema("value", DataType.CHARARRAY));
                ret.add(new Schema.FieldSchema("metadata", bagSchema, DataType.BAG));
            return new Schema(new Schema.FieldSchema(getSchemaName("extracted", inputSchema), ret,
                    DataType.TUPLE));
        } catch (FrontendException e) {
                throw new RuntimeException("Unable to create metadata bag schema.", e);
         }

    }
}
