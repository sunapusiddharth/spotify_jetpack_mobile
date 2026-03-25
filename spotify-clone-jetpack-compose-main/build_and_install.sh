#!/bin/bash

# Spotify Clone - Build and Install Script
# This script builds the app and installs it on a connected Android device/emulator

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Project variables
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_DIR="$PROJECT_DIR"
APP_PACKAGE="com.music.stream.neptune"
BUILD_VARIANT="debug"  # or "release"

# Resolve Android SDK path from environment or standard install locations.
resolve_android_sdk_path() {
    if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
        printf '%s\n' "$ANDROID_HOME"
        return 0
    fi

    if [ -n "$ANDROID_SDK_ROOT" ] && [ -d "$ANDROID_SDK_ROOT" ]; then
        printf '%s\n' "$ANDROID_SDK_ROOT"
        return 0
    fi

    for candidate in "$HOME/Library/Android/sdk" "$HOME/Android/Sdk"; do
        if [ -d "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done

    return 1
}

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Step 1: Check if adb is available
check_adb() {
    log_info "Checking for ADB..."
    if ! command -v adb &> /dev/null; then
        log_error "ADB not found. Please install Android SDK tools."
        exit 1
    fi
    log_success "ADB found"
}

# Step 2: Check for connected devices
check_devices() {
    log_info "Checking for connected devices/emulators..."
    devices=$(adb devices | grep -v "List of attached" | grep -v "^$")

    if [ -z "$devices" ]; then
        log_error "No devices or emulators found!"
        log_info "Please start an emulator or connect a device and try again."
        exit 1
    fi

    log_success "Connected devices/emulators:"
    adb devices
}

# Step 3: Make gradlew executable
make_gradlew_executable() {
    log_info "Making gradlew executable..."
    chmod +x "$GRADLE_DIR/gradlew"
    log_success "gradlew is executable"
}

# Step 4: Ensure Android SDK is configured for Gradle
ensure_android_sdk_config() {
    log_info "Ensuring Android SDK path is configured..."

    local sdk_path
    if ! sdk_path="$(resolve_android_sdk_path)"; then
        log_error "Android SDK not found. Set ANDROID_HOME/ANDROID_SDK_ROOT or install to ~/Library/Android/sdk."
        exit 1
    fi

    # Export for this process so adb/gradle use the same SDK location.
    export ANDROID_HOME="$sdk_path"
    export ANDROID_SDK_ROOT="$sdk_path"

    local local_properties_path="$GRADLE_DIR/local.properties"
    local escaped_sdk_path
    escaped_sdk_path="${sdk_path//\\/\\\\}"

    if [ -f "$local_properties_path" ]; then
        if grep -q '^sdk.dir=' "$local_properties_path"; then
            sed -i.bak "s|^sdk.dir=.*|sdk.dir=$escaped_sdk_path|" "$local_properties_path"
            rm -f "$local_properties_path.bak"
        else
            printf '\nsdk.dir=%s\n' "$escaped_sdk_path" >> "$local_properties_path"
        fi
    else
        printf 'sdk.dir=%s\n' "$escaped_sdk_path" > "$local_properties_path"
    fi

    log_success "Android SDK configured: $sdk_path"
}

# Step 5: Clean build
clean_build() {
    log_info "Cleaning previous build..."
    cd "$GRADLE_DIR"
    ./gradlew clean
    log_success "Build cleaned"
}

# Step 6: Build the APK
build_apk() {
    log_info "Building APK for $BUILD_VARIANT variant..."
    cd "$GRADLE_DIR"
    # macOS ships Bash 3.2 by default, so avoid Bash 4-only ${var^} syntax.
    local variant_capitalized
    variant_capitalized="$(printf '%s' "$BUILD_VARIANT" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')"
    ./gradlew "assemble${variant_capitalized}" -x test
    log_success "APK built successfully"
}

# Step 7: Find the built APK
find_apk() {
    log_info "Finding built APK..."
    APK_PATH=$(find "$GRADLE_DIR/app/build/outputs/apk/$BUILD_VARIANT" -name "*.apk" -type f | head -1)

    if [ -z "$APK_PATH" ]; then
        log_error "APK not found!"
        exit 1
    fi

    log_success "APK found at: $APK_PATH"
}

# Step 8: Uninstall existing app
uninstall_app() {
    log_info "Checking if app is installed..."
    if adb shell pm list packages | grep -q "$APP_PACKAGE"; then
        log_warning "App is already installed. Uninstalling..."
        adb uninstall "$APP_PACKAGE" || true
        log_success "App uninstalled"
    else
        log_info "App is not currently installed"
    fi
}

# Step 9: Install the APK
install_apk() {
    log_info "Installing APK on device..."
    adb install -r "$APK_PATH"
    log_success "APK installed successfully"
}

# Step 10: Verify installation
verify_installation() {
    log_info "Verifying installation..."
    if adb shell pm list packages | grep -q "$APP_PACKAGE"; then
        log_success "App installation verified!"
    else
        log_error "Installation verification failed!"
        exit 1
    fi
}

# Step 11: Launch the app (optional)
launch_app() {
    log_info "Launching app..."
    adb shell am start -n "$APP_PACKAGE/.MainActivity"
    log_success "App launched"
    # read -p "Do you want to launch the app now? (y/n) " -n 1 -r
    # echo
    # if [[ $REPLY =~ ^[Yy]$ ]]; then
    #     log_info "Launching app..."
    #     adb shell am start -n "$APP_PACKAGE/.MainActivity"
    #     log_success "App launched"
    # fi
}

# Main execution
main() {
    echo -e "${GREEN}"
    echo "╔════════════════════════════════════════╗"
    echo "║  Spotify Clone - Build & Install Tool  ║"
    echo "╚════════════════════════════════════════╝"
    echo -e "${NC}"

    check_adb
    check_devices
    make_gradlew_executable
    ensure_android_sdk_config

    # Ask about clean build
    read -p "Do you want to perform a clean build? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        clean_build
    fi

    build_apk
    find_apk
    uninstall_app
    install_apk
    verify_installation
    launch_app

    echo -e "${GREEN}"
    echo "╔════════════════════════════════════════╗"
    echo "║     Build & Install Completed! ✓       ║"
    echo "╚════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Run main function
main "$@"
