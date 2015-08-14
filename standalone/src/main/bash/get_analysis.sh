#!/bin/sh
hadoop fs -cat $1/summary/part-* | awk 'BEGIN{print "columns=["}{print $1","}END{print "];"}' > $2
