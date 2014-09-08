package com.caseystella.util.common.hadoop.ingest;

import com.google.common.base.Predicate;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cstella on 9/6/14.
 */
public class Config implements Predicate<File>
{
    /**
     * Returns the result of applying this predicate to {@code input}. This method is <i>generally
     * expected</i>, but not absolutely required, to have the following properties:
     * <p/>
     * <ul>
     * <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>; that is, {@code (a, b)} implies that {@code predicate.apply(a) ==
     * predicate.apply(b))}.
     * </ul>
     *
     * @param input
     * @throws NullPointerException if {@code input} is null and this predicate does not accept null
     *                              arguments
     */
    @Override
    public boolean apply(@Nullable File input) {
        boolean match = true;
        if(getNumPartitions() != null)
        {
            int hash = input.getName().hashCode();
            match = hash % getNumPartitions() == getPartitionId();
        }
        return match;
    }

    public static class Mapping implements Predicate<File>
    {
        private String destination;
        private String source;
        private String blacklistFile;
        private List<Pattern> blacklistPatterns = new ArrayList<Pattern>();

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getBlacklistFile() {
            return blacklistFile;
        }

        public void setBlacklistFile(String blacklistFile) {
            this.blacklistFile = blacklistFile;
        }

        void initialize() throws IOException {
            if(blacklistFile != null)
            {
                BufferedReader reader = new BufferedReader(new FileReader(blacklistFile));
                for(String line = null;(line = reader.readLine()) != null;)
                {
                    Pattern pattern = Pattern.compile(line.trim());
                    blacklistPatterns.add(pattern);
                }
            }
            if(getSource() == null || getSource().length() == 0)
            {
                throw new AssertionError("Source is empty or null");
            }
            if(getDestination() == null || getDestination().length() == 0)
            {
                throw new AssertionError("Source is empty or null");
            }
        }


        /**
         * Returns the result of applying this predicate to {@code input}. This method is <i>generally
         * expected</i>, but not absolutely required, to have the following properties:
         * <p/>
         * <ul>
         * <li>Its execution does not cause any observable side effects.
         * <li>The computation is <i>consistent with equals</i>; that is, {@code (a, b)} implies that {@code predicate.apply(a) ==
         * predicate.apply(b))}.
         * </ul>
         *
         * @param input
         * @throws NullPointerException if {@code input} is null and this predicate does not accept null
         *                              arguments
         */
        @Override
        public boolean apply(File input)
        {

            for(Pattern p : blacklistPatterns)
            {
                try {
                    Matcher m = p.matcher(input.getCanonicalPath());
                    if(m.matches())
                    {
                        return false;
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to canonicalize the path for " + input);
                }
            }
            return true;
        }
    }
    static ObjectMapper mapper = new ObjectMapper();
    private Mapping[] mappings;
    private Integer partitionId;
    private Integer numPartitions;

    public Mapping[] getMappings() {
        return mappings;
    }

    public void setMappings(Mapping[] mappings) {
        this.mappings = mappings;
    }

    public Integer getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    public Integer getNumPartitions() {
        return numPartitions;
    }

    public void setNumPartitions(Integer numPartitions) {
        this.numPartitions = numPartitions;
    }
    public static Config load(Reader input) throws IOException {
        return mapper.readValue(input, new TypeReference<Config>(){});
    }
    public void initialize() throws IOException {
        if(getNumPartitions() != null && getNumPartitions() > 0 && getPartitionId() == null)
        {
            throw new AssertionError("If you specify a number of partitions, you have to specify a partition ID");
        }
        for(Mapping m : getMappings())
        {
            m.initialize();
        }
    }
}
