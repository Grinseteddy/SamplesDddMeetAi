#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
find src -name '*.java' | sort > build/sources.txt
# The compiler module also works on JDK installations missing the javac launcher.
java -m jdk.compiler/com.sun.tools.javac.Main --release 17 -Xlint:all -Werror -d build/classes @build/sources.txt
java -cp build/classes cookingassistance.domain.DomainSmokeTest
