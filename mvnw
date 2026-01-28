#!/bin/sh
# Minimal Maven Wrapper (works without system Maven)
BASEDIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_DIR="$BASEDIR/.mvn/wrapper"
PROPS_FILE="$WRAPPER_DIR/maven-wrapper.properties"
JAR_FILE="$WRAPPER_DIR/maven-wrapper.jar"

WRAPPER_URL=$(grep -E '^wrapperUrl=' "$PROPS_FILE" | cut -d'=' -f2-)

download() {
  url="$1"
  dest="$2"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$url" -o "$dest"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$url" -O "$dest"
  else
    echo "curl yoki wget topilmadi. Iltimos Maven Wrapper jar'ni qo'lda yuklang."
    exit 1
  fi
}

if [ ! -f "$JAR_FILE" ]; then
  echo "Downloading Maven Wrapper..."
  mkdir -p "$WRAPPER_DIR"
  download "$WRAPPER_URL" "$JAR_FILE" || exit 1
fi

exec java -classpath "$JAR_FILE" -Dmaven.multiModuleProjectDirectory="$BASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
