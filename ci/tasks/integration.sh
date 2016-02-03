#!/bin/sh

# Set the basedir for Maven wrapper script to find the .mvn folder
export MAVEN_BASEDIR=pcfdemo

pcfdemo/mvnw --file=pcfdemo/pom.xml clean verify
