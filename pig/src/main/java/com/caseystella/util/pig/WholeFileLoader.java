package com.caseystella.util.pig;

import com.caseystella.util.common.WholeFileInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.*;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.List;

/**
 * Created by cstella on 9/2/14.
 */
public class WholeFileLoader extends LoadFunc implements LoadMetadata, LoadPushDown {
    Configuration jobConf;
    RecordReader<Text, BytesWritable> reader;
    @Override
    public void setLocation(String location, Job job) throws IOException {
        FileInputFormat.setInputPaths(job, location);
        this.jobConf = job.getConfiguration();

    }

    @Override
    public InputFormat getInputFormat() throws IOException {
        return new WholeFileInputFormat();
    }

    @Override
    public void prepareToRead(RecordReader recordReader, PigSplit pigSplit) throws IOException {
        this.reader = recordReader;
    }

    @Override
    public Tuple getNext() throws IOException {
        Tuple t = null;
        try {
            boolean notDone = reader.nextKeyValue();
            if (!notDone) {
                return null;
            }
            Text key = reader.getCurrentKey();
            BytesWritable value = reader.getCurrentValue();
            t = TupleFactory.getInstance().newTuple(2);
            t.set(0, key.toString());
            t.set(1, new DataByteArray(value.copyBytes()));
        }
        catch (InterruptedException e) {
            throw new IOException("Unable to read next value", e);
        }
        return t;
    }

    public ResourceSchema getSchema(String s, Job job) throws IOException {
        ResourceSchema ret = new ResourceSchema();
        ret.setFields(new ResourceSchema.ResourceFieldSchema[] {
                new ResourceSchema.ResourceFieldSchema(new Schema.FieldSchema("location", DataType.CHARARRAY))
               ,new ResourceSchema.ResourceFieldSchema(new Schema.FieldSchema("data", DataType.BYTEARRAY))
        });
        return ret;
    }

    public ResourceStatistics getStatistics(String s, Job job) throws IOException {
        return null;
    }

    public String[] getPartitionKeys(String s, Job job) throws IOException {
        return new String[0];
    }

    public void setPartitionFilter(Expression expression) throws IOException {

    }

    public List<OperatorSet> getFeatures() {
        return null;
    }

    public RequiredFieldResponse pushProjection(RequiredFieldList requiredFieldList) throws FrontendException {
        return null;
    }
}
