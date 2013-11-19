#!/bin/bash

mkdir -p models

if [ ! -f "models/stanford-corenlp-3.3.0-models.jar" ]; then
curl "http://central.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.3.0/stanford-corenlp-3.3.0-models.jar" -o models/stanford-corenlp-3.3.0-models.jar
fi

ls models/

CLASSPATH_PREFIX="models/stanford-corenlp-3.3.0-models.jar"
export CLASSPATH_PREFIX

sh webapp/target/bin/webapp
