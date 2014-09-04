package com.caseystella.util.common.enrich;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Created by cstella on 9/4/14.
 */
public enum ExtractContent {
    INSTANCE;

    public Map.Entry<String,Metadata> extractTextWithMetadata(String path, byte[] content) throws TikaException, SAXException, IOException
    {

        /*
        Use tika to extract the content into a the first value in the tuple, and the second value is a bag of key/value
        pairs representing the metadata from the document.
         */
        Parser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        //set the content type directly as tika has a bear of a time
        metadata.set(Metadata.CONTENT_TYPE, new Tika().detect(path));
        StringWriter writer = new StringWriter();
        //this is where Tika parses the document
        parser.parse(bis,
                new WriteOutContentHandler(writer),
                metadata,
                new ParseContext());
        return new AbstractMap.SimpleImmutableEntry<String, Metadata>(writer.toString(), metadata);
    }
}
