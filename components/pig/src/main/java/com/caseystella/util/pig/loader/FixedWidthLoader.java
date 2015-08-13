package com.caseystella.util.pig.loader;

import com.caseystella.util.common.interpret.fixed.Config;
import com.caseystella.util.common.interpret.fixed.Field;
import com.caseystella.util.common.hadoop.input.fixed.FixedWidthInputFormat;
import com.caseystella.util.pig.Helper;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
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
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.List;

import static com.caseystella.util.common.interpret.fixed.Field.Type.*;

/**
 * Created by cstella on 9/3/14.
 */
public class FixedWidthLoader extends LoadFunc implements LoadMetadata, LoadPushDown {
    Config config;
    String configFile = null;
    RecordReader<LongWritable, BytesWritable> reader;
    static EnumMap<Field.Type, Byte> typeToPigType = new EnumMap<Field.Type, Byte>(Field.Type.class);
    static
    {
        typeToPigType.put(BYTES, DataType.BYTEARRAY);
        typeToPigType.put(STRING, DataType.CHARARRAY);
        typeToPigType.put(INT, DataType.INTEGER);
        typeToPigType.put(FLOAT, DataType.FLOAT);
        typeToPigType.put(DOUBLE, DataType.DOUBLE);
    }
    public FixedWidthLoader(String configFile)
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
            config = Config.load(Helper.open(configFile, FixedWidthLoader.class));
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
            byte[] value = reader.getCurrentValue().copyBytes();
            t = TupleFactory.getInstance().newTuple();
            for(Field f : getConfig().getFields())
            {
                ByteBuffer b = ByteBuffer.wrap(value, f.getOffset(), f.getWidth());
                switch(f.getType())
                {
                    case BYTES:
                        ByteBuffer buff = (ByteBuffer) f.getConverter().convert(b, f.getType(), f.getConfig());
                        t.append(new DataByteArray(buff.array(), 0, f.getWidth()));
                        break;
                    default:
                        t.append(f.getConverter().convert(b, f.getType(), f.getConfig()));
                        break;
                }
            }
        }
        catch (InterruptedException e) {
            throw new IOException("Unable to read next value", e);
        }
        return t;
    }

    @Override
    public ResourceSchema getSchema(String s, Job job) throws IOException {
        Helper.addFileToContext(configFile, FixedWidthLoader.class);
        getConfig().validate();
        ResourceSchema ret = new ResourceSchema();
        ResourceSchema.ResourceFieldSchema[] fields = new ResourceSchema.ResourceFieldSchema[getConfig().getFields().length];
        int i = 0;
        for(Field f : getConfig().getFields())
        {
            fields[i] = new ResourceSchema.ResourceFieldSchema(new Schema.FieldSchema(f.getName(), typeToPigType.get(f.getType())));
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
