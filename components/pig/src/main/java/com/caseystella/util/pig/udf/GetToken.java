package com.caseystella.util.pig.udf;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * Created by cstella on 8/24/15.
 */
public class GetToken extends EvalFunc<String> {
    String splitter;
    int index;

    /**
     * Gets the index'th element of a tuple.  Including negative indexing (-1 is last element)
     * @param splitter
     * @param index
     */
    public GetToken(String splitter, String index)
    {
        this.splitter = splitter;
        this.index = Integer.parseInt(index);
    }

    @Override
    public String exec(Tuple tuple) throws IOException {
        String in = (String) tuple.get(0);
        Iterable<String> splits = Splitter.on(splitter).split(in);
        int size = Iterables.size(splits);
        if(index >= 0 ) {
            return Iterables.get(splits, index, null);
        }
        else {
            return Iterables.get(splits, size + index, null);
        }
    }
}
