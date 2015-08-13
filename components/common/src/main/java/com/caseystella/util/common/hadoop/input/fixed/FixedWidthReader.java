package com.caseystella.util.common.hadoop.input.fixed;


import org.apache.hadoop.io.BytesWritable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cstella on 9/3/14.
 */
public class FixedWidthReader implements Closeable {
    private InputStream in;
    private byte[] buffer;
    // the number of bytes of real data in the buffer
    private int bufferLength = 0;
    // the current position in the buffer
    private int bufferPosn = 0;

    // The line delimiter

    /**
     * Create a line reader that reads from the given stream using the
     * default buffer-size (64k).
     * @param in The input stream
     * @throws IOException
     */
    public FixedWidthReader(InputStream in) {
        this.in = in;
    }





    /**
     * Close the underlying stream.
     * @throws IOException
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Read one line from the InputStream into the given Text.
     *
     * @param str the object to store the given line (without newline)
     *  the rest of the line is silently discarded.
     *  in this call.  This is only a hint, because if the line cross
     *  this threshold, we allow it to happen.  It can overshoot
     *  potentially by as much as one buffer length.
     *
     * @return the number of bytes read including the (longest) newline
     * found.
     *
     * @throws IOException if the underlying stream throws
     */
    public int readLine(BytesWritable str) throws IOException {
        return in.read(str.getBytes());
    }


}
