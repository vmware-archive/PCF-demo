#!/bin/bash

# required
inputManifest=  outputManifest=

# optional
host=$CF_MANIFEST_HOST  path=

while [ $# -gt 0 ]; do
  case $1 in
    -i | --input-manifest )
      inputManifest=$2
      shift
      ;;
    -o | --output-manifest )
      outputManifest=$2
      shift
      ;;
    -n | --host )
      host=$2
      shift
      ;;
    -p | --path )
      path=$2
      shift
      ;;
    * )
      echo "Unrecognized option: $1" 1>&2
      exit 1
      ;;
  esac
  shift
done

if [ ! -f "$inputManifest" ]; then
  echo "missing input manifest!"
  exit 1
fi

if [ -z "$outputManifest" ]; then
  echo "missing output manifest!"
  exit 1
fi

cp $inputManifest $outputManifest

if [ ! -z "$host" ]; then
  sed -i "s|host: .*$|host: ${host}|g" $outputManifest
fi

if [ ! -z "$path" ]; then
  if [ -d "$path" ]; then
    path=`find $path -name '*.war'`
  fi
  # ugly hack: cf-resource is running in the directory of the last run task, so
  # we need to ../ to the parent directory to find the war file.
  sed -i -- "s|path: .*$|path: ../${path}|g" $outputManifest
fi

cat $outputManifest
