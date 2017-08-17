#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
version=`cat $DIR/version_to_release`

format="^[0-9]+\\.[0-9]+\\.[0-9]+$"

if ! [[ $version =~ $format ]]; then
    echo "Invalid format in release version: $version"
    exit 1
fi

echo "Preparing release of wiremock-pact-generator v$version"

mvn --version
mvn --batch-mode release:prepare -Dtag=v${version} -DreleaseVersion=${version}

if [ $? -eq 0 ]; then
    set -e
    mvn release:perform
else
    mvn release:rollback
    exit 1
fi
