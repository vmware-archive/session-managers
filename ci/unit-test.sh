#!/usr/bin/env bash

set -e -x

pushd session-managers
  ./mvnw package
popd
