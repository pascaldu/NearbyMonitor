#!/usr/bin/env sh
set -eu
GRADLE_VERSION="9.5.0"
BASE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/bootstrap"
GRADLE_HOME="$BASE_DIR/gradle-$GRADLE_VERSION"
ZIP_FILE="$BASE_DIR/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$BASE_DIR"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP_FILE" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  else
    echo "curl ou wget est nécessaire pour télécharger Gradle $GRADLE_VERSION" >&2
    exit 1
  fi
  unzip -q -o "$ZIP_FILE" -d "$BASE_DIR"
  rm -f "$ZIP_FILE"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
