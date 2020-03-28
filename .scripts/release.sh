#!/bin/bash -e
# Usage release.sh [RELEASE]
#   RELEASE: PATCH (default), MINOR, MAJOR or semver (eg 1.2.3)

VERSION="${1:-PATCH}"

publish() {
  if [[ -n "$GRADLE_PUBLISH_KEY" ]]; then
    ./gradlew publishPlugins \
      -Pgradle.publish.key=$GRADLE_PUBLISH_KEY \
      -Pgradle.publish.secret=$GRADLE_PUBLISH_SECRET
  else
    ./gradlew publishPlugins
  fi
}

release() {
  local ARGS="$@"
  if [[ -n "$GITHUB_TOKEN" ]]; then
    ARGS="$ARGS -Prelease.customUsername=$GITHUB_TOKEN"
  fi
  ./gradlew release -Ppublish $ARGS
}

if [[ -z "$CI" ]]; then
  # CI servers split build and release
  ./gradlew build --scan &&
    ./gradlew coveralls
fi

echo "Releasing: $VERSION"
if [[ "$VERSION" == "PATCH" ]]; then
  release && publish
elif [[ "$VERSION" == "MINOR" ]]; then
  release -Prelease.versionIncrementer=incrementMinor && publish
elif [[ "$VERSION" == "MAJOR" ]]; then
  release -Prelease.versionIncrementer=incrementMajor && publish
elif [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  release -Prelease.forceVersion="$VERSION" && publish
else
  echo "Unrecognized version: $VERSION"
  echo "Expected one of: \"PATCH\", \"MINOR\", \"MAJOR\" or semver (eg 1.2.3)"
  exit 1
fi
