#!/bin/bash

# run all tests with e2e on different browsers and resolutions

resolutions=("1920x1080" "1280x720" "800x480")
browsers=("firefox" "edge" "chcrome")

mvn '-Dtest=!edu.sumdu.tss.elephant.cucumber.*Test' test

for browser in "${browsers[@]}"
do
  for resolution in "${resolutions[@]}"
  do
    mvn -Dtest="edu.sumdu.tss.elephant.cucumber.*Test" -Dbrowser="${browser}" -Dresolution="${resolution}" test
  done
done