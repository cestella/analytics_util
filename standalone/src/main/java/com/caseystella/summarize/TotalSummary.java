package com.caseystella.summarize;

import com.google.common.collect.Maps;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 * Created by cstella on 8/14/15.
 */
public class TotalSummary {
    static ObjectMapper _mapper = new ObjectMapper();
    String column;
    String predictedType;
    Map<String, String> typeDistribution;
    List<Summary> typeSummaries;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getPredictedType() {
        return predictedType;
    }

    public void setPredictedType(String predictedType) {
        this.predictedType = predictedType;
    }

    public Map<String, String> getTypeDistribution() {
        return typeDistribution;
    }

    public void setTypeDistribution(Map<String, String> typeDistribution) {
        this.typeDistribution = typeDistribution;
    }

    public List<Summary> getTypeSummaries() {
        return typeSummaries;
    }

    public void setTypeSummaries(List<Summary> typeSummaries) {
        this.typeSummaries = typeSummaries;
    }

    public static TotalSummary create(String s) throws IOException {
        return _mapper.readValue(s, TotalSummary.class);
    }

    public String writeToString() throws IOException {
        return _mapper.writeValueAsString(this);
    }

    public void updateSummaries(List<Summary> summaries)
    {
        Map<String, String> typeDistribution = Maps.newLinkedHashMap();
        Collections.sort(summaries, new Comparator<Summary>() {
            @Override
            public int compare(Summary o1, Summary o2) {
                return -1*Long.compare(o1.getNumValues(), o2.getNumValues());
            }
        }
        );
        long total = 0;
        for(Summary s : summaries)
        {
            total += s.getNumValues();
        }
        for(Summary s : summaries)
        {
            double pct = 100.0*s.getNumValues()/total;
            typeDistribution.put(s.getType(), String.format("%.2f", pct) + "%");
        }
        setPredictedType(summaries.get(0).getType());
        setTypeSummaries(summaries);
        setTypeDistribution(typeDistribution);

    }
}
