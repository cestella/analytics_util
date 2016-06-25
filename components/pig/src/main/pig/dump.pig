register ./pig-1.0-SNAPSHOT-shaded.jar;


DEFINE WholeFileLoader com.caseystella.util.pig.loader.WholeFileLoader();
DEFINE extractor com.caseystella.util.pig.udf.ContentExtractor();
DEFINE get_last com.caseystella.util.pig.udf.GetToken('/', '-1');
DEFINE get_first com.caseystella.util.pig.udf.GetToken('.', '0');
DEFINE MultiStorage  org.apache.pig.piggybank.storage.MultiStorage('$output', '0');
A = load '$input' using WholeFileLoader;

B = foreach A generate get_first(get_last(location)) as location
                     , extractor(location, data);
C = foreach B generate location, content;

-- this generates a relation of tuples with the following fields:
--      location : chararray
--      content : chararray (the text content of the file)
--      metadata : { (key:chararray, value:chararray) }
-- The metadata is a bag of key/value pairs representing metadata about the document extracted from Tika
DESCRIBE B;
rmf $output
STORE C into '$output' USING MultiStorage;
