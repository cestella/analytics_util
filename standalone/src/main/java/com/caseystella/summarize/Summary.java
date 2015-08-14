package com.caseystella.summarize;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/14/15.
 */
public class Summary {
    static ObjectMapper _mapper = new ObjectMapper();
    private String type;
    private long numUniqueValues;
    private long numValues;
    private List<String> sample;
    private List<String> canonicalizedRepresentations;
    private Map<String, Double> quantiles;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getNumUniqueValues() {
        return numUniqueValues;
    }

    public void setNumUniqueValues(long numUniqueValues) {
        this.numUniqueValues = numUniqueValues;
    }

    public long getNumValues() {
        return numValues;
    }

    public void setNumValues(long numValues) {
        this.numValues = numValues;
    }

    public List<String> getSample() {
        return sample;
    }

    public void setSample(List<String> sample) {
        this.sample = sample;
    }

    public List<String> getCanonicalizedRepresentations() {
        return canonicalizedRepresentations;
    }

    public void setCanonicalizedRepresentations(List<String> canonicalizedRepresentations) {
        this.canonicalizedRepresentations = canonicalizedRepresentations;
    }

    public Map<String, Double> getQuantiles() {
        return quantiles;
    }

    public void setQuantiles(Map<String, Double> quantiles) {
        this.quantiles = quantiles;
    }

    public static Summary create(String s) throws IOException {
        return _mapper.readValue(s, Summary.class);
    }

    public String writeToString() throws IOException {
        return _mapper.writeValueAsString(this);
    }
}
