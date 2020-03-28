#!/bin/bash -e

: ${GITHUB_TOKEN:?Exiting release: No GITHUB_TOKEN variable}
: ${GRADLE_PUBLISH_KEY:?Exiting release: No GRADLE_PUBLISH_KEY variable}
: ${GRADLE_PUBLISH_SECRET:?Exiting release: No GRADLE_PUBLISH_SECRET variable}

if [[ -z "$RELEASE" ]]; then
  echo "Exiting release: RELEASE env variable not found"
  exit 0
fi

if [[ "$TRAVIS_BRANCH" != "master" ]] && [[ -n "$TRAVIS_PULL_REQUEST_SHA" ]]; then
  echo "Exiting release: Release should be enabled on master branch only"
  exit 0
fi

RELEASE_TAG="$(git tag --points-at HEAD | grep -P "^release-\d+(\.\d+){0,2}$")"
if [[ -n "$RELEASE_TAG" ]]; then
  echo "Exiting release: Current commmit is already tagged as $RELEASE_TAG"
  exit 0
fi

# Deduce release version from commit message
if [[ "$RELEASE" == "AUTO" ]]; then
  if echo "$TRAVIS_COMMIT_MESSAGE" | grep -P -q '^.*\[ *ci *release *\].*$'; then
    RELEASE="PATCH"
  elif echo "$TRAVIS_COMMIT_MESSAGE" | grep -P -q '^.*\[ *ci *release *minor *\].*$'; then
    RELEASE="MINOR"
  elif echo "$TRAVIS_COMMIT_MESSAGE" | grep -P -q '^.*\[ *ci *release *major *\].*$'; then
    RELEASE="MAJOR"
  elif echo "$TRAVIS_COMMIT_MESSAGE" | grep -P -q '^.*\[ *ci +release +\d+(\.\d+){0,2} *\].*$'; then
    RELEASE="$(echo "$TRAVIS_COMMIT_MESSAGE" | sed -nE 's|^.*\[ *ci +release +([0-9]+(\.[0-9]+){0,2}) *\].*$|\1|p')"
  else
    RELEASE="PATCH"
  fi
fi

# In case of release commits and tags use proper git user
git config --local user.name "travis@travis-ci.org"
git config --local user.email "Travis CI"
git checkout "$TRAVIS_BRANCH" >/dev/null 2>&1

.scripts/release.sh "$RELEASE"
