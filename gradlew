#!/bin/sh

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd)

exec java -Xmx64m -Xms64m -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
