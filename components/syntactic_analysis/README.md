#Introduction
The SummarizerCLI project is intended to be a utility which will give
semantic and syntactic summarization of data held in Hadoop.  The data
can be either a flat CSV or in Hive table.

#Build

Build from the root of this project (../..) using `mvn clean package`
#Usage

This project creates a .tar.gz file in target containing an uber jar of
all of the dependencies required for the execution of this utility.

```
usage: SummarizerCLI
 -D <property=value>                                 Input properties
 -h,--help                                           This screen
 -i,--input <SOURCE>                                 Input source
 -l,--load <JSON>                                    Load an existing
                                                     summary
 -m,--mode <MODE>                                    Type of mode.  One
                                                     of SQL,CSV
 -nns,--non_numeric_sample_size <NUM>                Sample size for
                                                     non-numeric data.
 -ns,--numeric_sample_size <NUM>                     Sample size for
                                                     numeric data.
 -o,--output <SOURCE>                                output location
 -pct,--percentiles <PCTILE1[,PCTILE2]*>             A comma separated
                                                     list of percentiles
                                                     in (0, 100].
 -smo,--similarity_min_occurrance <NUM_OCCURANCES>   Min Occurrances to
  be
                                                     considered for
                                                     synonyms
 -ssc,--similarity_score_cutoff <SCORE_CUTOFF>       Similarity score
                                                     cutoff.  Scores are
                                                     cosine sim., so
they
                                                     range from [0,1],
                                                     closer to 1 is more
                                                     similar
 -svs,--similarity_vec_size <DIM>                    Vector Size
```

#Example Usage
`./syntactic_summarizer.sh -i practice_fusion -m CSV -nns 20 -ns 1000 -o output_data.json -ssc 0.95` 

This will take a CSV file called `practice_fusion` located in the user
home directory in HDFS, which has a header row and emit a JSON
summarization file `output_data.json` which can then be loaded via
`./syntactic_summarizer.sh -l output_data.json` to explore via a console
graphical interface summarizing syntactic and semantic elements of each column.
