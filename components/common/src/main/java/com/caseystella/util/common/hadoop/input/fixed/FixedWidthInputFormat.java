package com.caseystella.util.common.hadoop.input.fixed;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * Created by cstella on 9/3/14.
 */
public class FixedWidthInputFormat extends FileInputFormat<LongWritable, BytesWritable> {
    public static final String WIDTH_KEY = "fwif.record.width";
    Integer width = null;
    public FixedWidthInputFormat(int width)
    {
       this();
        this.width = width;
    }
    public FixedWidthInputFormat()
    {
       super();
    }
    @Override
    public RecordReader<LongWritable, BytesWritable>
    createRecordReader(InputSplit split,
                       TaskAttemptContext context) {
        if(width == null)
        {
            String widthStr= context.getConfiguration().get(WIDTH_KEY);
            width = Integer.parseInt(widthStr);
        }
        return new FixedWidthRecordReader(width);
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        final CompressionCodec codec =
                new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
        if (null == codec) {
            return true;
        }
        return codec instanceof SplittableCompressionCodec;
    }
}
