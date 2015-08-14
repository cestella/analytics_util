package com.caseystella.util.pig;

import com.google.common.base.Splitter;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.metrics2.source.JvmMetrics;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.pig.data.Tuple;
import org.apache.pig.pigunit.PigTest;
import org.apache.pig.tools.parameters.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Created by cstella on 8/13/15.
 */
public class PigTests {
    @BeforeClass
    public static void beforeClass()
    {
        // TODO make it configurable whether this happens, for travis-ci we can't spam the logs so much,
        // however otherwise it is useful to see the errors
        //Logger.getRootLogger().removeAllAppenders();
        Logger.getLogger(JvmMetrics.class).setLevel(Level.INFO);
    }


    protected String[] getDefaultArgs()
    {
        String[] args = {
                getDataDirParam()
        };
        return args;
    }

    protected List<String> getDefaultArgsAsList()
    {
        String[] args = getDefaultArgs();
        List<String> argsList = new ArrayList<String>(args.length);
        for (String arg : args)
        {
            argsList.add(arg);
        }
        return argsList;
    }

    protected PigTest createPigTestFromString(String str, String... args) throws IOException
    {
        return createPigTest(str.split("\n"),args);
    }

    protected PigTest createPigTest(String[] lines, String... args) throws IOException
    {
        // append args to list of default args
        List<String> theArgs = getDefaultArgsAsList();
        for (String arg : args)
        {
            theArgs.add(arg);
        }

        for (String arg : theArgs)
        {
            String[] parts = arg.split("=",2);
            if (parts.length == 2)
            {
                for (int i=0; i<lines.length; i++)
                {
                    lines[i] = lines[i].replaceAll(Pattern.quote("$" + parts[0]), parts[1]);
                }
            }
        }

        return new PigTest(lines);
    }

    protected PigTest createPigTest(String scriptPath, String... args) throws IOException
    {
        return createPigTest(getLinesFromFile(scriptPath), args);
    }

    protected String getDataDirParam()
    {
        return "DATA_DIR=" + getDataPath();
    }

    protected String getDataPath()
    {
        File out = new File(System.getProperty("user.dir"), "data");
        if(!out.exists())
        {
            out.mkdirs();
        }
        return out.getAbsolutePath();
    }



    protected List<Tuple> getLinesForAlias(PigTest test, String alias) throws IOException, org.apache.pig.tools.parameters.ParseException {
        return getLinesForAlias(test,alias,true);
    }

    protected List<Tuple> getLinesForAlias(PigTest test, String alias, boolean logValues) throws IOException, org.apache.pig.tools.parameters.ParseException {
        Iterator<Tuple> tuplesIterator = test.getAlias(alias);
        List<Tuple> tuples = new ArrayList<Tuple>();
        if (logValues)
        {
            System.out.println(String.format("Values for %s: ", alias));
        }
        while (tuplesIterator.hasNext())
        {
            Tuple tuple = tuplesIterator.next();
            if (logValues)
            {
                System.out.println(tuple.toString());
            }
            tuples.add(tuple);
        }
        return tuples;
    }
    protected void writeStringToFile(String fileName, String lines) throws IOException {
        File inputFile = deleteIfExists(getFile(fileName));
        FileWriter writer = new FileWriter(inputFile);
        for (String line : Splitter.on('\n').split(lines))
        {
            writer.write(line + "\n");
        }
        writer.close();
    }
    protected void writeLinesToFile(String fileName, String... lines) throws IOException
    {
        File inputFile = deleteIfExists(getFile(fileName));
        writeLinesToFile(inputFile, lines);
    }

    protected void writeLinesToFile(File file, String[] lines) throws IOException
    {
        FileWriter writer = new FileWriter(file);
        for (String line : lines)
        {
            writer.write(line + "\n");
        }
        writer.close();
    }

    protected void assertOutput(PigTest test, String alias, String... expected) throws IOException, ParseException
    {
        List<Tuple> tuples = getLinesForAlias(test, alias);
        assertEquals(expected.length, tuples.size());
        int i=0;
        for (String e : expected)
        {
            assertEquals(tuples.get(i++).toString(), e);
        }
    }

    protected File deleteIfExists(File file)
    {
        if (file.exists())
        {
            file.delete();
        }
        return file;
    }

    protected File getFile(String fileName)
    {
        return new File(getDataPath(), fileName).getAbsoluteFile();
    }

    /**
     * Gets the lines from a given file.
     *
     * @param relativeFilePath The path relative to the datafu-tests project.
     * @return The lines from the file
     * @throws IOException
     */
    protected String[] getLinesFromFile(String relativeFilePath) throws IOException
    {
        // assume that the working directory is the datafu-tests project
        File file = new File(System.getProperty("user.dir"), relativeFilePath).getAbsoluteFile();
        BufferedInputStream content = new BufferedInputStream(new FileInputStream(file));
        Object[] lines = IOUtils.readLines(content).toArray();
        String[] result = new String[lines.length];
        for (int i=0; i<lines.length; i++)
        {
            result[i] = (String)lines[i];
        }
        return result;
    }
}
