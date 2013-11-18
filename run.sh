#!/bin/bash

mkdir -p models

if [ ! -f "models/stanford-corenlp-3.3.0-models.jar" ]; then
curl "http://central.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.3.0/stanford-corenlp-3.3.0-models.jar" -o target/stanford-corenlp-3.3.0-models.jar
fi

CLASSPATH_PREFIX="models/stanford-corenlp-3.3.0-models.jar"
export CLASSPATH_PREFIX

sh target/bin/webapp
