package com.caseystella.util.common.hadoop.ingest;

import com.google.common.base.Joiner;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.io.PrintWriter;

/**
 * Created by cstella on 9/7/14.
 */
public class AtomicPut {
    private static enum Opts
    {
        HELP(OptionBuilder.withLongOpt("help")
                .withDescription("Print this message")
                .create("h")
                , "h"
        )
        ,TEMP_DIR(OptionBuilder.hasArg()
            .withArgName("tmp_dir")
            .withDescription("Specify the tmp dir to use.")
            .withLongOpt("dir")
            .isRequired()
            .create("t")
            , "t"
    )
        ;
        static Options options = new Options();
        static CommandLineParser parser = new PosixParser();
        static
        {
            for(Opts opt : values())
            {
                options.addOption(opt.option);
            }
        }
        String code;
        Option option;
        Opts(Option option, String code)
        {
            this.option = option;
            this.code = code;
        }
        public boolean has(CommandLine commandLine)
        {
            return commandLine.hasOption(code);
        }
        public String get(CommandLine commandLine)
        {
            return commandLine.getOptionValue(code);
        }
        public static void printHelp(PrintWriter pw)
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "AtomicPut", options , true);

            formatter.printHelp(pw, HelpFormatter.DEFAULT_WIDTH
                    , "AtomicPut"
                    , null, options
                    , HelpFormatter.DEFAULT_LEFT_PAD
                    , HelpFormatter.DEFAULT_DESC_PAD
                    , null
                    , true
            );

        }
        public static CommandLine parse(String... argv)
        {

            try
            {
                return parser.parse( options, argv );
            }
            catch(ParseException ex)
            {
                printHelp(new PrintWriter(System.err));
                return null;
            }
        }
    }
    public static void main(String... argv)
    {
        CommandLine line = Opts.parse(argv);
        if(line == null)
        {
            System.exit(-1);
        }
        Path tmpDir = new Path(Opts.TEMP_DIR.get(line));
        String[] args = line.getArgs();
        for(int i = 0;i < args.length;i+=2)
        {

            File src = new File(args[i].trim());
            if(!src.exists())
            {
                System.err.println("Unable to find " + src);
                System.exit(-4);
            }
            String dst = args[i+1];

            FileSystem fs = null;
            try {
                fs = FileSystem.newInstance(new Configuration());
                if(!fs.exists(tmpDir))
                {
                    System.err.println("Tmp Dir " + tmpDir + " does not exist, please create it and rerun.");
                    System.exit(-3);
                }
                Path tmpLoc = null;
                {
                    String tmp = Opts.TEMP_DIR.get(line) + "/" + Joiner.on('_')
                            .join( src.getCanonicalPath()
                                    .replace(" ", "_")
                                    .replace("/", "_")
                                    .replace("\\", "_")
                                    , "" + System.currentTimeMillis()
                            )
                            ;
                    tmpLoc = new Path(tmp);
                }
                fs.copyFromLocalFile(new Path(src.getPath()), tmpLoc);
                fs.deleteOnExit(tmpLoc);
                Path fullDst = new Path(dst + "/" + src.getName());
                if(!fs.rename(tmpLoc, fullDst))
                {
                    System.err.println("Unable to move from tmpLoc " + tmpLoc + " to " + dst);
                    System.exit(-5);
                }
                fs.cancelDeleteOnExit(tmpLoc);
                System.out.println("Copied " + src + " to " + fullDst);

            } catch (IOException e) {
                e.printStackTrace(System.err);
                System.exit(-2);
            }
        }
    }
}
