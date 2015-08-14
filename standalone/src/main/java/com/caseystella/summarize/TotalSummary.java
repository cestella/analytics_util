package com.caseystella.summarize;

import com.google.common.collect.Maps;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        Map<String, String> typeDistribution = Maps.newHashMap();
        Map<String, Double> distribution = Maps.newHashMap();
        String dominantType = null;
        long dominantTypeCnt = 0;
        long total = 0;
        for(Summary s : summaries)
        {
            if(s.getNumValues() > dominantTypeCnt)
            {
                dominantType = s.getType();
                dominantTypeCnt = s.getNumValues();
            }
            total += s.getNumValues();
            distribution.put(s.getType(), 1.0*s.getNumValues());
        }
        for(Summary s : summaries)
        {
            double pct = 100.0*distribution.get(s.getType())/total;
            typeDistribution.put(s.getType(), String.format("%.2f", pct) + "%");
        }
        setPredictedType(dominantType);
        setTypeSummaries(summaries);
        setTypeDistribution(typeDistribution);

    }
}
