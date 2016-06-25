#!/bin/bash

export VERSION=${project.version}
export NAME=${project.artifactId}
export JAR=$NAME-$VERSION-shaded.jar
spark-submit --class com.caseystella.cli.SummarizerCLI\
             --master yarn-client\
             --packages com.databricks:spark-csv_2.10:1.4.0\
             --driver-memory 2g \
             --conf "spark.driver.maxResultSize=2048" \
             --conf "spark.executor.extraJavaOptions=-XX:+UseConcMarkSweepGC -XX:+UseParNewGC" \
             --conf "spark.ui.showConsoleProgress=true" \
             ./$JAR \
             "$@"
