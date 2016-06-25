package com.caseystella.cli;

import com.caseystella.input.Mode;
import com.caseystella.output.CursesVisualize;
import com.caseystella.summarize.Summarizer;
import com.caseystella.summarize.Summary;
import com.caseystella.summarize.TotalSummary;
import com.caseystella.util.JSONUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SummarizerCLI {
  public enum SummarizerOptions {
    HELP("h", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "help", false, "This screen");
        o.setRequired(false);
        return o;
      }
    }),
    LOAD("l", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "load", true, "Load an existing summary");
        o.setRequired(false);
        o.setArgName("JSON");
        return o;
      }
    }),
    INPUT("i", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "input", true, "Input source");
        o.setRequired(false);
        o.setArgName("SOURCE");
        return o;
      }
    }),
     MODE("m", new Function<String, Option>() {
       @Override
       public Option apply(String code) {
         Option o = new Option(code, "mode", true, "Type of mode.  One of " + Joiner.on(",").join(Mode.values()));
         o.setRequired(false);
         o.setArgName("MODE");
         return o;
       }
     }),
    INPUT_PROPERTIES("D", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        return OptionBuilder.withArgName("property=value")
                .hasArgs(2)
                .withValueSeparator()
                .withDescription("Input properties")
                .create(code);
      }
    }

    ),
    NUMERIC_SAMPLE_SIZE("ns", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "numeric_sample_size", true, "Sample size for numeric data.");
        o.setRequired(false);
        o.setArgName("NUM");
        return o;
      }
    }),
    NON_NUMERIC_SAMPLE_SIZE("nns", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "non_numeric_sample_size", true, "Sample size for non-numeric data.");
        o.setRequired(false);
        o.setArgName("NUM");
        return o;
      }
    }
    ),
    PERCENTILES("pct", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "percentiles", true, "A comma separated list of percentiles in (0, 100].");
        o.setRequired(false);
        o.setArgName("PCTILE1[,PCTILE2]*");
        return o;
      }
    }
    ),
    OUTPUT("o", new Function<String, Option>() {
      @Override
      public Option apply(String code) {
        Option o = new Option(code, "output", true, "output location");
        o.setRequired(false);
        o.setArgName("SOURCE");
        return o;
      }
    }
    )
    ;
    ;

    Option option;
    String shortCode;
    SummarizerOptions(String shortCode
                     , Function<String, Option> optionHandler
                     )
    {
      this.shortCode = shortCode;
      this.option = optionHandler.apply(shortCode);

    }

    public boolean has(CommandLine cli) {
      return cli.hasOption(shortCode);
    }

    public String get(CommandLine cli) {
      return cli.getOptionValue(shortCode);
    }
    public String get(CommandLine cli, String def) {
      return has(cli)?cli.getOptionValue(shortCode):def;
    }

    public Map<String, String> getProperties(CommandLine cli) {
      Properties p = cli.getOptionProperties(shortCode);
      Map<String, String> ret = new HashMap<>();
      for(Map.Entry<Object, Object> kv : p.entrySet()) {
        ret.put(kv.getKey().toString(), kv.getValue().toString());
      }
      return ret;
    }




    public static CommandLine parse(CommandLineParser parser, String[] args) throws ParseException {
      try {
        CommandLine cli = parser.parse(getOptions(), args);
        if(HELP.has(cli)) {
          printHelp();
          System.exit(0);
        }
        return cli;
      } catch (ParseException e) {
        System.err.println("Unable to parse args: " + Joiner.on(' ').join(args));
        e.printStackTrace(System.err);
        printHelp();
        throw e;
      }
    }

    public static void printHelp() {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "SummarizerCLI", getOptions());
    }

    public static Options getOptions() {
      Options ret = new Options();
      for(SummarizerOptions o : SummarizerOptions.values()) {
        ret.addOption(o.option);
      }
      return ret;
    }
  }

  private static List<Double> getPercentiles(String pctiles) {
    List<Double> ret = new ArrayList<>();
    Iterables.addAll(ret, Iterables.transform(Splitter.on(",").split(pctiles), new com.google.common.base.Function<String, Double>() {
      @Nullable
      @Override
      public Double apply(@Nullable String s) {
        return Double.parseDouble(s.trim());
      }
    }));
    return ret;
  }

  public static void main(String... argv) throws ParseException, IOException {
    Parser parser = new PosixParser();
    CommandLine cli = SummarizerOptions.parse(parser, argv);
    Map<String, Summary> output =null;
    if(SummarizerOptions.LOAD.has(cli)) {
      output = JSONUtils.INSTANCE.load(new File(SummarizerOptions.LOAD.get(cli)), new TypeReference<Map<String, Summary>> (){

      });
    }
    else {
      SparkConf conf = new SparkConf().setAppName("Summarizer");
      conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
      JavaSparkContext sc = new JavaSparkContext(conf);
      String input = SummarizerOptions.INPUT.get(cli);
      Map<String, String> inputOptions = SummarizerOptions.INPUT_PROPERTIES.getProperties(cli);
      Mode mode = Mode.valueOf(SummarizerOptions.MODE.get(cli).toUpperCase());
      DataFrame df = mode.open(input, sc, inputOptions);
      int numericSampleSize = Integer.parseInt(SummarizerOptions.NUMERIC_SAMPLE_SIZE.get(cli, "1500"));
      int nonNumericSampleSize = Integer.parseInt(SummarizerOptions.NON_NUMERIC_SAMPLE_SIZE.get(cli, "20"));
      List<Double> percentiles = getPercentiles(SummarizerOptions.PERCENTILES.get(cli, "25,50,75,95,99"));
      output = Summarizer.summarize(df, numericSampleSize, nonNumericSampleSize, percentiles);
    }
    if(SummarizerOptions.OUTPUT.has(cli)) {
      File out = new File(SummarizerOptions.OUTPUT.get(cli));
      try(PrintWriter pw = new PrintWriter(out)) {
        IOUtils.write(JSONUtils.INSTANCE.toJSON(output, true), pw);
      }
    }
    else {
      new CursesVisualize().display(output);
    }
  }
}
