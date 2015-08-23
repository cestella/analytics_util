package com.caseystella.type.pig;

import com.caseystella.util.pig.PigTests;
import com.google.common.base.Splitter;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.hadoop.metrics2.source.JvmMetrics;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.PigTest;
import org.apache.pig.tools.parameters.ParseException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cstella on 8/13/15.
 */
public class PivotTest extends PigTests {
    /**
DEFINE PIVOT com.caseystella.type.udf.PIVOT;
DATA = load 'data/pivot_default.dat' using PigStorage(',') as (d:chararray, f:double, s:chararray, i:int);
OUT = foreach DATA generate FLATTEN(PIVOT(*));
store OUT into 'data/pivot_out.dat' using PigStorage(',');
     */
    @Multiline
    static String pivotScript;

    /**
10/30/1980,1.0,cat,2
,2.2,dog,5
1980/10/30,2.0,parrot,7*/
    @Multiline
    static String pivotData;

    /**
(d,STRING,DATE,10/30/1980,dd/dd/dddd)
(f,FLOAT,FLOAT,1.0,1.0)
(s,STRING,STRING,cat,cat)
(i,INTEGRAL,INTEGRAL,2,2)
(d,STRING,MISSING,,)
(f,FLOAT,FLOAT,2.2,2.2)
(s,STRING,STRING,dog,dog)
(i,INTEGRAL,INTEGRAL,5,5)
(d,STRING,DATE,1980/10/30,dddd/dd/dd)
(f,FLOAT,FLOAT,2.0,2.0)
(s,STRING,STRING,parrot,parrot)
(i,INTEGRAL,INTEGRAL,7,7)*/
    @Multiline
    static String expectedOutput;
    @Test
    public void testPivot() throws IOException, ParseException {
        System.out.println(pivotData);
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(new ConsoleAppender(
                    new PatternLayout("%-5p [%t]: %m%n")));
        writeStringToFile("pivot_default.dat", pivotData);
        System.getProperties().setProperty("mapred.map.child.java.opts", "-Xmx1G");
        System.getProperties().setProperty("mapred.reduce.child.java.opts","-Xmx1G");
        System.getProperties().setProperty("io.sort.mb", "10");
        PigTest test = createPigTestFromString(pivotScript);
        List<Tuple> tups = getLinesForAlias(test, "OUT");
        assertOutput(tups, expectedOutput);
    }

    public void assertOutput(List<Tuple> output, String expectedOutput)
    {
        Set<String> expectedSet = new HashSet();
        Set<String> actualSet = new HashSet();
        for(Tuple t : output)
        {
            actualSet.add(t.toString());
        }
        for(String t : Splitter.on('\n').split(expectedOutput))
        {
            Assert.assertTrue(actualSet.contains(t));
            expectedSet.add(t);
        }
        for(String t : actualSet)
        {
            Assert.assertTrue(expectedSet.contains(t));
        }

    }
}
