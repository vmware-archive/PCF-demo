#!/bin/sh

baseName="pcf-demo"
srcDir=
versionFile=

while getopts "d:v:" opt; do
  case $opt in
    d) srcDir=$OPTARG
       ;;
    v) versionFile=$OPTARG
       ;;
  esac
done
shift $((OPTIND - 1))

if [ ! -d "$srcDir" ]; then
  echo "missing source directory!"
  exit 1
fi

if [ -f "$versionFile" ]; then
  version=`cat versionFile`
  baseName="${baseName}-${version}"
fi

srcWar=`find $srcDir -name *.war`
dstWar="${baseName}.war"

echo "Renaming ${srcWar} to ${dstWar}"

cp ${srcWar} ${dstWar}

ls -al
