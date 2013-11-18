#!/bin/bash

if [ ! -f target/stanford-corenlp-3.3.0-models.jar ]; then
curl "http://central.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.3.0/stanford-corenlp-3.3.0-models.jar" -o target/stanford-corenlp-3.3.0-models.jar
fi

sh target/bin/webapp
