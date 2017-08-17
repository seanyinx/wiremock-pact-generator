#!/usr/bin/env bash

# Increment a version string using Semantic Versioning (SemVer).


function on_error {
  echo "usage: $(basename $0) [major|minor|patch]"
  exit 1
}

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

version=`cat $DIR/version_to_release`

a=( ${version//./ } )

if [ ${#a[@]} -ne 3 ]
then
  on_error
fi

function major {
  ((a[0]++))
  a[1]=0
  a[2]=0
}

function minor {
  ((a[1]++))
  a[2]=0
}

function patch {
  ((a[2]++))
}

case $1 in
 major) major;;
 minor) minor;;
 patch) patch;;
 *) on_error;;
esac

new_version="${a[0]}.${a[1]}.${a[2]}"
echo "${new_version}" > $DIR/version_to_release

git add $DIR/version_to_release
git commit -m "chore: bump version to $new_version"

echo "Version bumped from $version to $new_version"
