#!/bin/bash

set -euo pipefail

KEYSTORE_FILE="${KEYSTORE_FILE:-app/neptune-release.jks}"
KEY_ALIAS="${KEY_ALIAS:-neptune}"
KS_PASS="${KS_PASS:-neptune123}"
KEY_PASS="${KEY_PASS:-$KS_PASS}"
OUTPUT_APK="${OUTPUT_APK:-spotify-neptune-release.apk}"
APK_PATH="app/build/outputs/apk/release/app-release.apk"

if ! command -v keytool >/dev/null 2>&1; then
    echo "ERROR: keytool not found. Install a JDK and ensure keytool is on PATH."
    exit 1
fi

if [ ! -x "./gradlew" ]; then
    echo "ERROR: ./gradlew not found or not executable."
    exit 1
fi

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "No keystore found. Generating a new one..."

    keytool -genkeypair \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -keystore "$KEYSTORE_FILE" \
        -storepass "$KS_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Neptune, O=Neptune, L=Unknown, ST=Unknown, C=IN"

    echo "Keystore created: $KEYSTORE_FILE"
fi

echo
echo "Building signed release APK..."
./gradlew :app:assembleRelease \
    -PRELEASE_STORE_FILE="$KEYSTORE_FILE" \
    -PRELEASE_STORE_PASSWORD="$KS_PASS" \
    -PRELEASE_KEY_ALIAS="$KEY_ALIAS" \
    -PRELEASE_KEY_PASSWORD="$KEY_PASS"

if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "$OUTPUT_APK"
    SIZE=$(du -h "$OUTPUT_APK" | cut -f1)
    echo
    echo "=== BUILD SUCCESSFUL ==="
    echo "APK: $(pwd)/$OUTPUT_APK ($SIZE)"
    echo
    echo "To install via ADB:"
    echo "  adb install -r $OUTPUT_APK"
else
    echo "ERROR: APK not found at $APK_PATH"
    exit 1
fi