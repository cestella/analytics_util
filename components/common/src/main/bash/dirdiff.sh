#!/bin/sh
CLASSPATH=""
java -cp "./common-1.0-SNAPSHOT-shaded.jar:/etc/hadoop/conf:/usr/lib/hadoop/client/*" com.caseystella.util.common.hadoop.ingest.DirDiff "$@"
