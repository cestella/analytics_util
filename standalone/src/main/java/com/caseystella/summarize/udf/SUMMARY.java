package com.caseystella.summarize.udf;

import com.caseystella.summarize.Summary;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/14/15.
 */
public class SUMMARY extends EvalFunc<String> {
    List<String> columns = new ArrayList<String>();

    public static final Function<Tuple, String> TO_STRING = new Function<Tuple, String>()
    {

        @Override
        public String apply(Tuple objects) {
            try {
                return objects.get(0).toString();
            } catch (ExecException e) {
                throw new RuntimeException("Unable to process " + objects);
            }
        }
    };

    public static Map<String, Double> tupleToQuantile(Tuple t, String... quantiles) throws ExecException {
        Map<String, Double> ret = Maps.newLinkedHashMap();
        for(int i = 0;i < quantiles.length;++i)
        {
            ret.put(quantiles[i], ((Number) t.get(i)).doubleValue());
        }
        return ret;
    }
    @Override
    public String exec(Tuple objects) throws IOException {
        if(columns.isEmpty())
        {
            loadCols(getInputSchema());
        }
        Summary summary = new Summary();
        int i = 0;
        for(Object t : objects)
        {
            String columnName = columns.get(i++);
            if(columnName.equalsIgnoreCase("type"))
            {
                summary.setType(t.toString());
            }
            else if(columnName.equalsIgnoreCase("num_values"))
            {
                summary.setNumValues(((Number) t).longValue());
            }
            else if(columnName.equalsIgnoreCase("num_unique_values"))
            {
                summary.setNumUniqueValues(((Number) t).longValue());
            }
            else if(columnName.equalsIgnoreCase("samples"))
            {
                Iterable<String> str = Iterables.transform((DataBag) t, TO_STRING);
                summary.setSample(Lists.newArrayList(str));
            }
            else if(columnName.equalsIgnoreCase("formats"))
            {
                Iterable<String> str = Iterables.transform((DataBag) t, TO_STRING);
                summary.setCanonicalizedRepresentations(Lists.newArrayList(str));
            }
            else if(columnName.equalsIgnoreCase("quantiles"))
            {
                summary.setQuantiles(tupleToQuantile((Tuple)t, "min"
                                                             , "25th"
                                                             , "50th"
                                                             , "75th"
                                                             , "95th"
                                                             , "99th"
                                                             , "max"
                                                    )
                                    );
            }
            else
            {
                throw new IllegalStateException("Unable to handle column " + columnName);
            }
        }
        return summary.writeToString();
    }
    public Schema outputSchema(Schema inputSchema)
    {
        loadCols(inputSchema);
        return super.outputSchema(inputSchema);
    }

    public void loadCols(Schema inputSchema)
    {
        for(Schema.FieldSchema fs : inputSchema.getFields())
        {
            columns.add(fs.alias);
        }
    }
}
