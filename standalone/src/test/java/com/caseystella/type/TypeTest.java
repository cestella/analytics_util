package com.caseystella.type;

import junit.framework.Assert;
import org.apache.pig.data.DataType;
import org.junit.Test;

/**
 * Created by cstella on 8/13/15.
 */
public class TypeTest {
    @Test
    public void testTypeInference()
    {
        Assert.assertNull(Type.fromPigType(DataType.BAG));
        Assert.assertNotNull(Type.fromPigType(DataType.FLOAT));
        Assert.assertNotNull(Type.fromPigType(DataType.INTEGER));
        Assert.assertNotNull(Type.fromPigType(DataType.CHARARRAY));
        //Integer
        {
            ValueSummary summary = Type.summarize("foo", DataType.LONG, 1);
            Assert.assertEquals(summary.baseType, Type.INTEGRAL);
            Assert.assertEquals(summary.inferredType, Type.INTEGRAL);
            Assert.assertEquals(summary.canonicalValue, "1");
        }
        //Float
        {
            ValueSummary summary = Type.summarize("foo", DataType.FLOAT, 1.0f);
            Assert.assertEquals(summary.baseType, Type.FLOAT);
            Assert.assertEquals(summary.inferredType, Type.FLOAT);
            Assert.assertEquals(summary.canonicalValue, "1.0");
        }
        //NULL
        {
            ValueSummary summary = Type.summarize("foo", DataType.LONG, null);
            Assert.assertEquals(summary.baseType, Type.INTEGRAL);
            Assert.assertEquals(summary.inferredType, Type.MISSING);
            Assert.assertEquals(summary.canonicalValue, null);
        }
        //String -> Integer
        {
            ValueSummary summary = Type.summarize("foo", DataType.CHARARRAY, "1");
            Assert.assertEquals(summary.baseType, Type.STRING);
            Assert.assertEquals(summary.inferredType, Type.INTEGRAL);
            Assert.assertEquals(summary.canonicalValue, "1");
        }
        //String -> Float
        {
            ValueSummary summary = Type.summarize("foo", DataType.CHARARRAY, "1.2");
            Assert.assertEquals(summary.baseType, Type.STRING);
            Assert.assertEquals(summary.inferredType, Type.FLOAT);
            Assert.assertEquals(summary.canonicalValue, "1.2");
        }
        //String -> Date
        {
            ValueSummary summary = Type.summarize("foo", DataType.CHARARRAY, "10/30/1980");
            Assert.assertEquals(summary.baseType, Type.STRING);
            Assert.assertEquals(summary.inferredType, Type.DATE);
            Assert.assertEquals(summary.canonicalValue, "dd/dd/dddd");
        }
    }

    @Test
    public void testCanonicalization()
    {
        DateType dt = new DateType();
        Assert.assertEquals("dddd/dd/dd", dt.canonicalize("2015/05/04"));
    }

    @Test
    public void testDateLikelihoodMatcher()
    {
        String[] likelyDates = new String[]{
                 "2015/05/01"
                ,"August 05, 2015"
                ,"05/01/2015"
                ,"05/01/15"
                ,"05 Aug 1991"
                ,"05 Aug 91"
                ,"05 Aug, 91"
                ,"05-01-2015"
                ,"05-01-15"
                ,"2015-05-01"
                ,"2015.05.01"
                ,"05.01.2015"
                ,"05.01.15"
                ,"2010-11-17 01:12 pm"
                , "11-5-2014 11:11:51"
                , "11 May 2014 Eastern European Summer Time"
                , "Sun, 11 May 2014 23:11:51 EEST"
                , "Sunday, 11 May 2014 23:11:51 EEST"
                , "Sun May 11 23:11:51 EEST 2014"
                , "Sunday May 11 23:11:51 EEST 2014"
                , "10/30/1980"
                                           };
        String[] unlikelyDates = new String[] {
                "C:/foo/bar/grok"
               ,"C:/foo/05/grok"
               ,"/foo/05/grok/dump-06-05-01.txt"
               , "The dog ate the poo from the road in August."
               , "Next monday @ 7:00"
               , null
               , "null"
               , ""
               , "555-555-1234" //phone number
               , "012-34-5678" // US social security number (random ;)
               , "012341928"
               , "11"
               , "Sunday May turd jack 11"
        };
        DateType dt = new DateType();
        for(String likelyDate : likelyDates)
        {
            Assert.assertTrue("Expected " + likelyDate + " to be a date.", dt.isLikely(likelyDate));
        }
        for(String unlikelyDate : unlikelyDates)
        {
            Assert.assertFalse("Did not expect " + unlikelyDate + " to be a date.", dt.isLikely(unlikelyDate));
        }
    }
}
