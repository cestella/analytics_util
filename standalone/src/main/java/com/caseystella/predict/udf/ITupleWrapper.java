package com.caseystella.predict.udf;

import java.io.IOException;

/**
 * Created by cstella on 8/18/15.
 */
public interface ITupleWrapper {
    Object get(int offset) throws IOException;
}
