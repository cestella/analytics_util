package com.caseystella.predict;

import com.caseystella.predict.feature.FeatureMatrix;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hr.irb.fastRandomForest.FastRandomForest;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import smile.data.AttributeDataset;
import smile.data.parser.ArffParser;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/16/15.
 */
public class FeatureImportance {
    public static class Importance implements Comparable<Importance>
    {
        public String feature;
        public double importanceRank;

        @Override
        public String toString() {
            return "{" +
                    "feature='" + feature + '\'' +
                    ", importanceRank=" + importanceRank +
                    '}';
        }


        @Override
        public int compareTo(Importance o) {
            return -1*Double.compare(importanceRank, o.importanceRank);
        }
    }
    public List<Importance> findImportance(Instances instances, List<Attribute> attributes, boolean isRegression) throws Exception {
        ArffParser arffParser = new ArffParser();
        arffParser.setResponseIndex(attributes.size() - 1);
        AttributeDataset dataset= arffParser.parse(new StringBufferInputStream(instances.toString()));
        double[][] x = dataset.toArray(new double[dataset.size()][]);

        List<Importance> rankedImportances = Lists.newArrayList();
        double[] importances = null;
        int numTrees = (int)Math.floor(Math.sqrt(attributes.size()));
        if(numTrees < 200)
        {
            numTrees = 200;
        }
        if(isRegression) {
            double[] y = dataset.toArray(new double[dataset.size()]);
            smile.regression.RandomForest forest =
                    new smile.regression.RandomForest(dataset.attributes(), x, y, numTrees);
            importances = forest.importance();

        }
        else
        {
            int[] y = dataset.toArray(new int[dataset.size()]);
             smile.classification.RandomForest forest =
                    new smile.classification.RandomForest(dataset.attributes(), x, y, numTrees);
            importances = forest.importance();
        }

        int i = 0;
        for(double importance : importances)
        {
            Importance imp= new Importance();
            imp.feature = attributes.get(i).name();
            imp.importanceRank = importance;
            rankedImportances.add(imp);
            i++;
        }
        Collections.sort(rankedImportances);
        ArrayList<Importance> ret = new ArrayList<Importance>();
        ret.add(rankedImportances.get(0));
        ret.add(rankedImportances.get(1));
        for(i = 2;i < rankedImportances.size();++i)
        {
            double madScore = getMADScore(rankedImportances, i);
            if(madScore > 3.5)
            {
                //stop, we've dropped precipitously
                break;
            }
            else
            {
                ret.add(rankedImportances.get(i));
            }
        }
        if(ret.size() == rankedImportances.size())
        {

            return new ArrayList<Importance>();
        }
        return ret;
    }
    private double getMADScore(List<Importance> importances, int index)
    {
        Double median = null;
        {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i = 0; i < index; ++i) {
                stats.addValue(importances.get(i).importanceRank);
            }
            median = stats.getPercentile(50);
        }
        Double mad = null;
        {
            DescriptiveStatistics stats = new DescriptiveStatistics();
            for (int i = 0; i < index; ++i) {
                stats.addValue(Math.abs(importances.get(i).importanceRank - median));
            }
            mad = stats.getPercentile(50);
        }
        return Math.abs(0.6745*(importances.get(index).importanceRank - median)/mad);
    }
}
