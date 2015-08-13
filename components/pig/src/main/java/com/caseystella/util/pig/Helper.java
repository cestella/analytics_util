package com.caseystella.util.pig;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.apache.pig.impl.io.FileLocalizer;
import org.apache.pig.impl.io.ResourceNotFoundException;
import org.apache.pig.impl.util.UDFContext;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by cstella on 9/3/14.
 */
public class Helper {
    /**
     * Add a local file's content to the UDF context, so it can be used in the backend.
     * Should be called from the frontend.
     * @param localFilename
     * @param clazz
     * @param <T>
     * @throws IOException
     */
    public static <T> void addFileToContext(String localFilename, Class<T> clazz) throws IOException {
        UDFContext pigContext = UDFContext.getUDFContext();
        File f = new File(localFilename);
        String content = Joiner.on('\n').join(Files.readLines(f, Charset.defaultCharset()));
        pigContext.getUDFProperties(clazz).setProperty(localFilename, content);
    }

    public static <T> Reader open(String resource, Class<T> clazz) throws IOException
    {
        UDFContext pigContext = UDFContext.getUDFContext();
        InputStream iStream = null;
        if(pigContext.getUDFProperties(clazz).containsKey(resource))
        {
            // Then we've added the resource on the frontend, let's load it.
            return new StringReader(pigContext.getUDFProperties(clazz).getProperty(resource));
        }
        //otherwise, let's try to grab the resource using the classloader
        iStream = ClassLoader.class.getResourceAsStream(resource);
        if(iStream != null)
        {
            return new BufferedReader(new InputStreamReader(iStream));
        }
        else
        {
            FileLocalizer.FetchFileRet fetchFileRet = null;
            try {
                fetchFileRet = FileLocalizer.fetchResource(resource);
            }
            catch(ResourceNotFoundException rnfe)
            {
                throw new RuntimeException("Could not find localized resource: " + resource, rnfe);
            }
            return new BufferedReader(new FileReader(fetchFileRet.file));
        }
    }
}
