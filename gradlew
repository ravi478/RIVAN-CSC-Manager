#!/bin/sh
# RIVAN CSC Manager Gradle bootstrap wrapper.
# Downloads Gradle 9.6.0 once if it is not already cached.
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.6.0
DIST_NAME=gradle-${GRADLE_VERSION}-bin.zip
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/rivan-wrapper/dists/${GRADLE_VERSION}"
GRADLE_HOME="$CACHE_DIR/gradle-${GRADLE_VERSION}"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$CACHE_DIR"
  TMP="$CACHE_DIR/$DIST_NAME"
  if [ ! -f "$TMP" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 -o "$TMP" "https://services.gradle.org/distributions/$DIST_NAME"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$TMP" "https://services.gradle.org/distributions/$DIST_NAME"
    else
      echo "curl or wget is required to download Gradle." >&2
      exit 1
    fi
  fi
  rm -rf "$CACHE_DIR/gradle-${GRADLE_VERSION}"
  if command -v unzip >/dev/null 2>&1; then
    unzip -q "$TMP" -d "$CACHE_DIR"
  else
    echo "unzip is required to extract Gradle." >&2
    exit 1
  fi
fi
exec "$GRADLE_HOME/bin/gradle" "$@"
