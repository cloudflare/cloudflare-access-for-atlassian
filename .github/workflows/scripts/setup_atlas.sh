#!/bin/bash
set -exu

echo "Installing Atlassian SDK"

wget https://marketplace.atlassian.com/download/plugins/atlassian-plugin-sdk-tgz -O atlassian-plugin-sdk.tgz
tar -xvzf atlassian-plugin-sdk.tgz -C /opt
# find the extracted path name
ATLASSIAN_SDK_PATH=$(find /opt -type d -name "atlassian-plugin-sdk-*")
echo "Atlassian SDK extracted in $ATLASSIAN_SDK_PATH"

# Add the Atlassian SDK to the PATH
echo "$ATLASSIAN_SDK_PATH/bin" >> "$GITHUB_PATH"
export PATH="$ATLASSIAN_SDK_PATH/bin:$PATH"

echo "Atlassian SDK installed successfully:"

atlas-version
