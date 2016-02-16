#!/bin/bash

baseName="pcf-demo"

inputDir=     # required
outputDir=    # required
versionFile=  # optional

while [ $# -gt 0 ]; do
  case $1 in
    -i | --input-dir )
      inputDir=$2
      shift
      ;;
    -o | --output-dir )
      outputDir=$2
      shift
      ;;
    -v | --version-file )
      versionFile=$2
      shift
      ;;
    * )
      echo "Unrecognized option: $1" 1>&2
      exit 1
      ;;
  esac
  shift
done

if [ ! -d "$inputDir" ]; then
  echo "missing input directory!"
  exit 1
fi

if [ ! -d "$outputDir" ]; then
  echo "missing output directory!"
  exit 1
fi

if [ -f "$versionFile" ]; then
  version=`cat $versionFile`
  baseName="${baseName}-${version}"
fi

inputWar=`find $inputDir -name '*.war'`
outputWar="${outputDir}/${baseName}.war"

echo "Renaming ${inputWar} to ${outputWar}"

cp ${inputWar} ${outputWar}
