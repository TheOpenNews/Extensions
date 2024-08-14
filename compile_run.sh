#!/bin/sh

set -eux

classpath="libs/json.jar:libs/okhttp-4.9.1.jar:libs/okio-3.6.0-all.jar:libs/okio-jvm-3.4.0.jar:libs/commons-text-1.12.0.jar:libs/commons-lang3-3.15.0.jar:libs/jsoup-1.15.3.jar"
kotlinc -classpath $classpath  -d  build/ExtensionsAbstract.jar anynews/src/shared/*.kt # anynews/trtarabi/src/extension/*.kt
kotlin -classpath $classpath:build/build.jar anynews.extension.trtarabi.TrtArabiKt

# kotlinc  -jvm-target 1.8  -language-version 1.7 -d libs/ExtensionsAbstract.jar anynews/src/shared/*.kt 
