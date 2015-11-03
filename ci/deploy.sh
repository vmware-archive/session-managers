#!/usr/bin/env bash

set -e -x

pushd session-managers
  ./mvnw -Dmaven.test.skip=true deploy
popd
