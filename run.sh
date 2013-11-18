#!/bin/bash

cd target
wget -N "http://central.maven.org/maven2/edu/stanford/nlp/stanford-corenlp/3.3.0/stanford-corenlp-3.3.0-models.jar"
cd ..

sh target/bin/webapp
