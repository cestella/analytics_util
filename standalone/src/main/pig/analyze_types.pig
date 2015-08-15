set pig.splitCombination false;
%default analysis_home '.';
%default num_samples '10';
%default datafu_jar '/usr/hdp/current/pig-client/lib/datafu.jar'

register $analysis_home/standalone-1.0-SNAPSHOT-shaded.jar;
register $datafu_jar;

DEFINE HCatLoader org.apache.hive.hcatalog.pig.HCatLoader();
DEFINE PIVOT com.caseystella.type.udf.PIVOT;
DEFINE SUMMARY com.caseystella.summarize.udf.SUMMARY;
DEFINE TOTAL_SUMMARY com.caseystella.summarize.udf.TOTAL_SUMMARY;
DEFINE RESERVOIR_SAMPLE datafu.pig.sampling.ReservoirSample('$num_samples');
DEFINE Quantile datafu.pig.stats.StreamingQuantile('0.0','0.25','0.5','0.75','0.95', '0.99', '1.0');

DATA = load '$input' using HCatLoader;
PIVOTED_DATA = foreach DATA generate FLATTEN(PIVOT(*)) as ( column:chararray
                                                          , type:chararray
                                                          , inferred_type:chararray
                                                          , value:chararray
                                                          , canonical_value:chararray
                                                          );

split PIVOTED_DATA into STRINGS if inferred_type == 'STRING'
                      , INTEGRALS if inferred_type == 'INTEGRAL'
                      , FLOATS if inferred_type == 'FLOAT'
                      , DATES if inferred_type == 'DATE'
                      , MISSING if inferred_type == 'MISSING'
                 ;

-- Handle Strings
STRINGS_G = group STRINGS by column;
STRINGS_PRE= foreach STRINGS_G {
   VALUES = foreach STRINGS generate value;
   DIST_VALUES = distinct VALUES;
   SAMPLES = RESERVOIR_SAMPLE(DIST_VALUES);
   generate group as column
          , 'STRING' as type
          , SIZE(VALUES) as num_values
          , SIZE(DIST_VALUES) as num_unique_values
          , SAMPLES as samples
          ;
};
STRINGS_SUMMARY = foreach STRINGS_PRE generate column, SUMMARY(type, num_values, num_unique_values, samples) as summary;

-- Handle Dates
DATES_G = group DATES by column;
DATES_PRE = foreach DATES_G {
   VALUES = foreach DATES generate value;
   DIST_VALUES = distinct VALUES;
   CAN_VALUES = foreach DATES generate canonical_value;
   DIST_CAN_VALUES = distinct CAN_VALUES;
   SAMPLES = RESERVOIR_SAMPLE(DIST_VALUES);
   generate group as column
          , 'DATE' as type
          , SIZE(VALUES) as num_values
          , SIZE(DIST_VALUES) as num_unique_values
          , SAMPLES as samples
          , DIST_CAN_VALUES as formats
          ;
};
DATES_SUMMARY = foreach DATES_PRE generate column, SUMMARY(type, num_values, num_unique_values, samples, formats)as summary;

-- Handle integers
INTEGRALS_G = group INTEGRALS by column;
INTEGRALS_PRE = foreach INTEGRALS_G {
   VALUES = foreach INTEGRALS generate (int)value;
   DIST_VALUES = distinct VALUES;
   QUANTILES = Quantile(VALUES);
   generate group as column
          , 'INTEGRAL' as type
          , SIZE(VALUES) as num_values
          , SIZE(DIST_VALUES) as num_unique_values
          , QUANTILES as quantiles;
};
INTEGRALS_SUMMARY = foreach INTEGRALS_PRE generate column
                                                 , SUMMARY( type
                                                          , num_values
                                                          , num_unique_values
                                                          , quantiles
                                                          ) as summary;
-- Handle floats 
FLOATS_G = group FLOATS by column;
FLOATS_PRE = foreach FLOATS_G {
   VALUES = foreach FLOATS generate (double)value;
   DIST_VALUES = distinct VALUES;
   QUANTILES = Quantile(VALUES);
   generate group as column
          , 'FLOAT' as type
          , SIZE(VALUES) as num_values
          , SIZE(DIST_VALUES) as num_unique_values
          , QUANTILES as quantiles;
};
FLOATS_SUMMARY = foreach FLOATS_PRE generate column
                                                 , SUMMARY( type
                                                          , num_values
                                                          , num_unique_values
                                                          , quantiles
                                                          ) as summary;

-- Handle nulls 
MISSING_G = group MISSING by column;
MISSING_PRE = foreach MISSING_G {
   generate group as column
          , 'NULL' as type
          , SIZE(MISSING) as num_values
          ;
};
MISSING_SUMMARY = foreach MISSING_PRE generate column, SUMMARY(type, num_values) as summary;

U = union STRINGS_SUMMARY, MISSING_SUMMARY, FLOATS_SUMMARY, INTEGRALS_SUMMARY, DATES_SUMMARY;
BY_COL = group U by column;
SUMMARIES = foreach BY_COL {
   SUMMARIES = foreach U generate summary;
   generate TOTAL_SUMMARY( group, SUMMARIES) as summary;
} 

rmf $output/summary
store SUMMARIES into '$output/summary' using PigStorage(',');
