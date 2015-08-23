package com.caseystella.summarize.udf;

import com.caseystella.summarize.Summary;
import com.caseystella.summarize.TotalSummary;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cstella on 8/14/15.
 */
public class TOTAL_SUMMARY extends EvalFunc<String> {
    @Override
    public String exec(Tuple objects) throws IOException {
        TotalSummary ret = new TotalSummary();
        String column = (String) objects.get(0);
        ret.setColumn(column);
        List<Summary> summaries = new ArrayList<Summary>();
        for(Tuple t : (DataBag)objects.get(1))
        {
            summaries.add(Summary.create((String) t.get(0)));
        }
        ret.updateSummaries(summaries);
        return ret.writeToString();
    }
}
