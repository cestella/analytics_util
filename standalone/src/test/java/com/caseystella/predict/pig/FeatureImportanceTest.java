package com.caseystella.predict.pig;

import com.caseystella.predict.FeatureMatrixTest;
import com.caseystella.util.pig.PigTests;
import com.google.common.base.Joiner;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.PigTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by cstella on 8/17/15.
 */
public class FeatureImportanceTest extends PigTests {

    /**
     DEFINE GEN_TARGETS com.caseystella.predict.udf.GEN_TARGETS('data/iris_features.json');
     DEFINE FEATURE_IMPORTANCE com.caseystella.predict.udf.FEATURE_IMPORTANCE('data/iris_features.json');
     DATA = load 'data/iris.csv' using PigStorage(',') as ( sepal_length:chararray
                                                          , sepal_width:chararray
                                                          , petal_length:chararray
                                                          , petal_width:chararray
                                                          , iris_class:chararray
                                                          );
     G = group DATA all;
     BY_TARGETS = foreach G generate FLATTEN(GEN_TARGETS()), DATA as data;
     WITH_IMPORTANCE = foreach BY_TARGETS generate target, FLATTEN(FEATURE_IMPORTANCE(target, data));
     OUT = order WITH_IMPORTANCE by target;
     store OUT into 'data/iris_importance.dat' using PigStorage(',');
     */
    @Multiline
    public static String script;

    @Test
    public void integrationTest() throws Exception
    {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(new ConsoleAppender(
                new PatternLayout("%-5p [%t]: %m%n")));
        writeStringToFile("iris.csv", FeatureMatrixTest.irisData);
        writeStringToFile("iris_features.json", FeatureMatrixTest.featureConfig);
        System.getProperties().setProperty("mapred.map.child.java.opts", "-Xmx1G");
        System.getProperties().setProperty("mapred.reduce.child.java.opts","-Xmx1G");
        System.getProperties().setProperty("io.sort.mb", "10");
        PigTest test = createPigTestFromString(script);
        List<Tuple> tups = getLinesForAlias(test, "OUT");
        //stupid random forest is stupid and random, so I can't say much here other than it should find SOME connections
        //between columns in the iris dataset.
        Assert.assertTrue(tups.size() > 0);
    }
}
