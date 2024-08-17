#!/bin/sh

set -eux

# classpath="libs/json.jar:libs/okhttp-4.9.1.jar:libs/okio-3.6.0-all.jar:libs/okio-jvm-3.4.0.jar:libs/commons-text-1.12.0.jar:libs/commons-lang3-3.15.0.jar:libs/jsoup-1.15.3.jar"
# kotlinc -classpath $classpath  -d  build/build.jar anynews/src/shared/*.kt anynews/s2jnews/src/extension/*.kt
# kotlin -classpath $classpath:build/build.jar anynews.extension.s2jnews.S2JNewsKt
# kotlinc  -jvm-target 1.8  -language-version 1.7 -d libs/ExtensionsAbstract.jar anynews/src/shared/*.kt 


rm apks/*.apk
mv anynews/alarabiya/build/outputs/apk/debug/onews-alarabiya-debug.apk apks/onews-alarabiya-debug.apk
mv anynews/aljazeera/build/outputs/apk/debug/onews-aljazeera-debug.apk apks/onews-aljazeera-debug.apk
mv anynews/s2jnews/build/outputs/apk/debug/onews-s2jnews-debug.apk apks/onews-s2jnews-debug.apk
mv anynews/trtarabi/build/outputs/apk/debug/onews-trtarabi-debug.apk apks/onews-trtarabi-debug.apk
