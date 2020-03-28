#!/bin/bash -e

if [[ "$TRAVIS_BRANCH" == "master" ]] && [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then
  ./gradlew coveralls
else
  echo "Skipping coverage for non master branch"
fi
