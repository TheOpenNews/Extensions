#!/bin/sh

set -eux

# kotlinc  -jvm-target 1.8  -language-version 1.7 -d libs/ExtensionsAbstract.jar anynews/src/shared/*.kt 

./gradlew assembleDebug

rm apks/*.apk
mv anynews/alarabiya/build/outputs/apk/debug/onews-alarabiya-debug.apk apks/onews-alarabiya-debug.apk
mv anynews/aljazeera/build/outputs/apk/debug/onews-aljazeera-debug.apk apks/onews-aljazeera-debug.apk
mv anynews/s2jnews/build/outputs/apk/debug/onews-s2jnews-debug.apk apks/onews-s2jnews-debug.apk
mv anynews/trtarabi/build/outputs/apk/debug/onews-trtarabi-debug.apk apks/onews-trtarabi-debug.apk
