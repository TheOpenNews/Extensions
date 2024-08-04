#!/bin/sh

set -eux

classpath="libs/json.jar:libs/okhttp-4.9.1.jar:libs/okio-3.6.0-all.jar:libs/okio-jvm-3.4.0.jar"
kotlinc -classpath $classpath -include-runtime  -d  build/build.jar anynews/s2jnews/src/shared/*.kt anynews/s2jnews/src/extension/*.kt
kotlin -classpath $classpath:build/build.jar anynews.extensions.S2JNewsKt
