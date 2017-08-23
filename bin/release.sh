#!/usr/bin/env bash

commit_prefix="chore: [release] "

function get_release_version {
    DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
    version=`cat $DIR/version_to_release`

    format="^[0-9]+\\.[0-9]+\\.[0-9]+$"

    if ! [[ $version =~ $format ]]; then
        echo "Invalid format in release version: $version"
        exit 1
    fi
}

function prepare_release {
    echo "Preparing release of wiremock-pact-generator v$version"
    mvn --batch-mode release:prepare -Dtag=v${version} -DreleaseVersion=${version} \
        -DscmCommentPrefix="$commit_prefix"
}

function rollback {
    echo "Rolling back changes"
    mvn release:rollback
    exit 1
}

function perform_release {
    echo "Performing release of wiremock-pact-generator v$version"
    mvn release:perform
}

function main {
    get_release_version
    prepare_release || rollback
    perform_release || rollback
}

main
