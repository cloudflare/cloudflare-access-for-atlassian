#!/bin/bash
set -ex

# This packages the plugins using atlas-mvn and removes the generate packaged test jars
# that are not used and shouldn't appear in releases.

atlas-mvn --batch-mode package
find ./ -type f -wholename '*target/*tests.jar' -print -exec rm {} \;