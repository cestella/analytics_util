package com.caseystella.util.pig.loader.fixed;

import com.caseystella.util.common.input.fixed.FixedWidthInputFormat;
import com.caseystella.util.pig.Helper;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.*;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by cstella on 9/3/14.
 */
public class Loader extends LoadFunc implements LoadMetadata, LoadPushDown {
    Config config;
    String configFile = null;
    RecordReader<LongWritable, BytesWritable> reader;
    public Loader(String configFile)
    {
       this.configFile = configFile;
    }
    @Override
    public void setLocation(String location, Job job) throws IOException {
        FileInputFormat.setInputPaths(job, location);
    }

    @Override
    public InputFormat getInputFormat() throws IOException {

        return new FixedWidthInputFormat(getConfig().computeWidth());
    }
    private Config getConfig() throws IOException {
        if(config == null)
        {
            config = Config.load(Helper.open(configFile, Loader.class));
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
            LongWritable key = reader.getCurrentKey();
            byte[] value = reader.getCurrentValue().copyBytes();
            t = TupleFactory.getInstance().newTuple();
            for(Field f : getConfig().getFields())
            {
                ByteBuffer b = ByteBuffer.wrap(value, f.getOffset(), f.getWidth());
                t.append(f.getConverter().convert(b, f.getType(), f.getConfig()));
            }
        }
        catch (InterruptedException e) {
            throw new IOException("Unable to read next value", e);
        }
        return t;
    }

    @Override
    public ResourceSchema getSchema(String s, Job job) throws IOException {
        Helper.addFileToContext(configFile, Loader.class);
        getConfig().validate();
        ResourceSchema ret = new ResourceSchema();
        ResourceSchema.ResourceFieldSchema[] fields = new ResourceSchema.ResourceFieldSchema[getConfig().getFields().length];
        int i = 0;
        for(Field f : getConfig().getFields())
        {
            fields[i] = new ResourceSchema.ResourceFieldSchema(new Schema.FieldSchema(f.getName(), f.getType().getPigType()));
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
