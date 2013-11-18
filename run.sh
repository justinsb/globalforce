#!/bin/bash

if [ ! -f target/models.jar ]; then
curl "http://central.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.3.0/stanford-corenlp-3.3.0-models.jar" -o target/models.jar
fi

CLASSPATH_PREFIX="target/models.jar"
export CLASSPATH_PREFIX
sh target/bin/webapp
