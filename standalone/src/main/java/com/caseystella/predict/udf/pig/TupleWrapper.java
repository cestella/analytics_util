package com.caseystella.predict.udf.pig;

import com.caseystella.predict.udf.ITupleWrapper;
import com.google.common.base.Function;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/**
 * Created by cstella on 8/18/15.
 */
public class TupleWrapper implements ITupleWrapper {
    public static Function<Tuple, ITupleWrapper> TO_WRAPPER = new Function<Tuple, ITupleWrapper>() {
        @Override
        public ITupleWrapper apply(Tuple objects) {
            return new TupleWrapper(objects);
        }
    };
    Tuple t;
    public TupleWrapper(Tuple t)
    {
        this.t = t;
    }
    @Override
    public Object get(int offset) throws IOException {
        return t.get(offset);
    }
}
