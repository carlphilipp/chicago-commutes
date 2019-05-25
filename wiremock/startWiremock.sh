#!/bin/sh

current_dir=`dirname $0`
. ${current_dir}/VERSION

jar_name="wiremock-standalone-$WIREMOCK_VERSION.jar"
jar="$current_dir/$jar_name"

if [[ ! -f ${jar} ]]; then
    echo "Downloading wiremock..."
    wget -P ${current_dir} http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/$WIREMOCK_VERSION/$jar_name
fi

java -jar $jar --root-dir ${current_dir}
