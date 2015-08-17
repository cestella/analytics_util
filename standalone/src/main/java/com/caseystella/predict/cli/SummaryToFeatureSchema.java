package com.caseystella.predict.cli;

import com.caseystella.predict.feature.FeatureMatrix;
import com.caseystella.predict.feature.FeatureType;
import com.caseystella.summarize.Summary;
import com.caseystella.summarize.TotalSummary;
import com.caseystella.type.Type;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.List;
import java.util.Map;


/**
 * Created by cstella on 8/14/15.
 */
public class SummaryToFeatureSchema {
    public static void main(String... argv) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter outWriter = new PrintWriter(System.out);
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        double missingPctThreshold = 0.25;
        // create the Options
        Options options = new Options();
        options.addOption( "h", "help", false, "Show help" );
        options.addOption( OptionBuilder.withLongOpt("input")
                .withDescription( "file or \"stdin\"" )
                .hasArg()
                .withArgName("location")
                .create("i") );
        options.addOption( OptionBuilder.withLongOpt("output")
                .withDescription( "file or \"stdout\"" )
                .hasArg()
                .withArgName("location")
                .create("o") );
        options.addOption( OptionBuilder.withLongOpt("missing_tolerance")
                .withDescription( "Percent missing tolerated." )
                .hasArg()
                .withArgName("PCT")
                .create("m") );
        try {
            CommandLine commandLine = parser.parse(options, argv);
            if(commandLine.hasOption("input") && !commandLine.getOptionValue('i').toString().equalsIgnoreCase("stdin")) {
                inputReader = new BufferedReader(new FileReader(new File(commandLine.getOptionValue('i'))));
            }

            if(commandLine.hasOption("output")&& !commandLine.getOptionValue('o').toString().equalsIgnoreCase("stdout")) {
                outWriter = new PrintWriter(new FileOutputStream(new File(commandLine.getOptionValue('o'))));
            }
            if(commandLine.hasOption("missing_tolerance"))
            {
                missingPctThreshold = Double.parseDouble(commandLine.getOptionValue('m').toString());
            }
        } catch (Exception e) {
            System.out.println("Unexpected exception:" + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("summary2featureConfig", options);
            System.out.println("STACK TRACE\n----------------");
            e.printStackTrace();
        }
        List<FeatureMatrix.Feature> features = Lists.newArrayList();
        for(String line = null;(line = inputReader.readLine()) != null;)
        {
            TotalSummary summary = TotalSummary.create(line);
            FeatureMatrix.Feature feature = createFeature(summary, missingPctThreshold);
            if(feature != null)
            {
                features.add(feature);
            }

        }
        inputReader.close();
        FeatureMatrix matrix = new FeatureMatrix();
        matrix.setFeatures(features);
        outWriter.println(matrix.writeToString());
        outWriter.flush();
        outWriter.close();
    }
    public static FeatureMatrix.Feature createFeature(TotalSummary summary, double missingPctThreshold)
    {
        FeatureMatrix.Feature ret = new FeatureMatrix.Feature();
        ret.setColumn(summary.getColumn());
        Map<String, String> typeDistribution = summary.getTypeDistribution();
        Map<String, Object> config = Maps.newHashMap();

        double floatPct = typeDistribution.containsKey(Type.INTEGRAL.toString())
                          ? getPct(typeDistribution.get(Type.INTEGRAL.toString()))
                          :0d;
        double intPct = typeDistribution.containsKey(Type.INTEGRAL.toString())
                          ? getPct(typeDistribution.get(Type.INTEGRAL.toString()))
                          :0d;
        double missingPct = typeDistribution.containsKey(Type.MISSING.toString())
                          ? getPct(typeDistribution.get(Type.MISSING.toString()))
                          :0d;

        double stringPct = typeDistribution.containsKey(Type.STRING.toString())
                          ? getPct(typeDistribution.get(Type.STRING.toString()))
                          :0d;
        if(missingPct > missingPctThreshold)
        {
            return null;
        }
        boolean isNumeric = 1.0 - (floatPct + intPct) > 0.90;
        //numeric
        if( isNumeric )
        {

            if( intPct - floatPct > 0.90)
            {
                ret.setType(FeatureType.INTEGRAL);
            }
            else
            {
                ret.setType(FeatureType.FLOAT);
            }
            return ret;
        }
        //string
        if(typeDistribution.containsKey(Type.STRING) && stringPct > .90)
        {
            Summary s = null;
            for(Summary t : summary.getTypeSummaries())
            {
                if(t.getType().equals(Type.STRING))
                {
                    s = t;
                    break;
                }
            }
            if( s.getNumUniqueValues() == s.getSample().size())
            {
                //categorical
                ret.setType(FeatureType.CATEGORICAL);
                config.put("categories", s.getSample());
                return ret;
            }
            else
            {
                return null;
            }
        }
        return null;
    }
    private static double getPct(String pct)
    {
        return Double.parseDouble(pct.replace('%', ' ').trim())/100;
    }
}
