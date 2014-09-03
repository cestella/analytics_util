register ./textProcessing-1.0-SNAPSHOT-shaded.jar;

DEFINE WholeFileLoader com.hortonworks.example.pig.WholeFileLoader();
DEFINE extractor com.hortonworks.example.pig.TikaExtractor();

A = load '$input' using WholeFileLoader;

B = foreach A generate location as location
                     , FLATTEN(extractor(location, data));
-- this generates a relation of tuples with the following fields:
--      location : chararray
--      content : chararray (the text content of the file)
--      metadata : { (key:chararray, value:chararray) }
-- The metadata is a bag of key/value pairs representing metadata about the document extracted from Tika
DESCRIBE B;
rmf $output
STORE B into '$output' using PigStorage(',');
