#!/bin/sh
pig -useHCatalog -param output=$2 -param input=$1 pig/analyze_types.pig
