package com.caseystella.util.pig.udf;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.*;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

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
        /*
        Use tika to extract the content into a the first value in the tuple, and the second value is a bag of key/value
        pairs representing the metadata from the document.
         */
        Parser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ByteArrayInputStream bis = new ByteArrayInputStream(((DataByteArray)objects.get(1)).get());
        //set the content type directly as tika has a bear of a time
        metadata.set(Metadata.CONTENT_TYPE, new Tika().detect((String)objects.get(0)));
        StringWriter writer = new StringWriter();
        try {
            //this is where Tika parses the document
            parser.parse(bis,
                    new WriteOutContentHandler(writer),
                    metadata,
                    new ParseContext());
        } catch (SAXException e) {
            throw new RuntimeException("Unable to parse.", e);
        } catch (TikaException e) {

            throw new RuntimeException("Unable to parse.", e);
        }
        String content = writer.toString();
        DataBag metadataBag = bagFactory.newDefaultBag();
        Tuple ret = tupleFactory.newTuple(2);
        ret.set(0, content);
        for(String key : metadata.names())
        {
            String value = metadata.get(key);
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
