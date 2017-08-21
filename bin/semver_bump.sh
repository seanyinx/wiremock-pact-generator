#!/usr/bin/env bash


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
bump=$1

function on_error {
  echo "usage: $(basename $0) [major|minor|patch]"
  exit 1
}

function get_current_version {
    version=`jq -r .version $DIR/next_version.json`
    v=( ${version//./ } )

    if [ ${#v[@]} -ne 3 ]; then
        on_error
    fi
}

function bump_version {
    case $bump in
     major)
        ((v[0]++))
        v[1]=0
        v[2]=0
        ;;
     minor)
        ((v[1]++))
        v[2]=0
        ;;
     patch)
        ((v[2]++))
        ;;
     *) on_error;;
    esac

    new_version="${v[0]}.${v[1]}.${v[2]}"

    jq .version=\"$new_version\" $DIR/next_version.json > $DIR/next_version.json.next
    mv $DIR/next_version.json.next $DIR/next_version.json

    echo "Version bumped from $version to $new_version"
}

function generate_changelog {
    mvn package -P changelog || (echo "Failed to generate changelog" && on_error)
}

function generate_third_party {
    mvn license:add-third-party

    if [ -f target/generated-sources/license/THIRD-PARTY.txt ]; then
        cp target/generated-sources/license/THIRD-PARTY.txt $DIR/../THIRD-PARTY.txt
    else
        echo "Could not find generated THIRD-PARTY.txt"
        on_error
    fi
}

function commit_changes {
    set -e
    git add $DIR/next_version.json
    git add $DIR/../THIRD-PARTY.txt
    git add $DIR/../CHANGELOG.md
    git commit -m "chore: prepare release for v$new_version"
}

function main {
    get_current_version
    bump_version
    generate_changelog
    generate_third_party
    commit_changes
}

main
