package com.caseystella.util.pig.loader;

import com.caseystella.util.common.interpret.xpath.Config;
import com.caseystella.util.common.interpret.xpath.Field;
import com.caseystella.util.pig.Helper;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.pig.*;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 9/4/14.
 */
public class XPathLoader extends LoadFunc implements LoadMetadata, LoadPushDown {
    Config config;
    String configFile = null;
    RecordReader<LongWritable, Text> reader;
    private SAXBuilder builder = new SAXBuilder();
    public XPathLoader(String configFile)
    {
       this.configFile = configFile;
    }
    @Override
    public void setLocation(String location, Job job) throws IOException {
        FileInputFormat.setInputPaths(job, location);
    }

    @Override
    public InputFormat getInputFormat() throws IOException {

        return new TextInputFormat();
    }
    private Config getConfig() throws IOException {
        if(config == null)
        {
            config = Config.load(Helper.open(configFile, XPathLoader.class));
        }
        return config;
    }
    @Override
    public void prepareToRead(RecordReader recordReader, PigSplit pigSplit) throws IOException {
        getConfig();
        reader = recordReader;
    }

    @Override
    public Tuple getNext() throws IOException {
        Tuple t = null;
        try {
            boolean notDone = reader.nextKeyValue();
            if (!notDone) {
                return null;
            }
            t = TupleFactory.getInstance().newTuple();
            String value = reader.getCurrentValue().toString();
            Document doc = null;
            try {
                doc = builder.build(new StringReader(value));
                for(Map.Entry<String, String> entry : getConfig().getContent(doc).entrySet())
                {
                    t.append(entry.getValue());
                }
            } catch (JDOMException e) {
                throw new RuntimeException("Unable to parse XML: " + value);
            }

        }
        catch (InterruptedException e) {
            throw new IOException("Unable to read next value", e);
        }
        return t;
    }

    @Override
    public ResourceSchema getSchema(String s, Job job) throws IOException {
        Helper.addFileToContext(configFile, XPathLoader.class);
        ResourceSchema ret = new ResourceSchema();
        ResourceSchema.ResourceFieldSchema[] fields = new ResourceSchema.ResourceFieldSchema[getConfig().getFields().length];
        int i = 0;
        for(Field f : getConfig().getFields())
        {
            fields[i] = new ResourceSchema.ResourceFieldSchema(new Schema.FieldSchema(f.getName(), DataType.CHARARRAY));
            i++;
        }
        ret.setFields(fields);
        return ret;
    }

    @Override
    public ResourceStatistics getStatistics(String s, Job job) throws IOException {
        return null;
    }

    @Override
    public String[] getPartitionKeys(String s, Job job) throws IOException {
        return new String[0];
    }

    @Override
    public void setPartitionFilter(Expression expression) throws IOException {

    }

    @Override
    public List<OperatorSet> getFeatures() {
        return null;
    }

    @Override
    public RequiredFieldResponse pushProjection(RequiredFieldList requiredFieldList) throws FrontendException {
        return null;
    }
}
