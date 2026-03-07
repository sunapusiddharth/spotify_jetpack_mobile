#!/bin/bash

# Quick install script:
# - No full clean build
# - Sets adb reverse for localhost:9000
# - Installs app on connected device(s)

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_PACKAGE="com.music.stream.neptune"
APP_ACTIVITY="com.music.stream.neptune/.MainActivity"
BUILD_VARIANT="debug"
PORT="9000"

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

check_adb() {
    if ! command -v adb >/dev/null 2>&1; then
        log_error "adb not found in PATH. Open Android Studio terminal or add platform-tools to PATH."
        exit 1
    fi
    log_success "Using adb: $(command -v adb)"
}

get_connected_devices() {
    adb devices | awk '/\tdevice$/{print $1}'
}

check_devices() {
    DEVICES="$(get_connected_devices)"
    if [ -z "$DEVICES" ]; then
        log_error "No connected devices/emulators found."
        exit 1
    fi
    log_success "Connected devices:"
    printf '%s\n' "$DEVICES"
}

make_gradlew_executable() {
    chmod +x "$PROJECT_DIR/gradlew"
}

ensure_proxy_reverse() {
    log_info "Setting adb reverse tcp:${PORT} -> tcp:${PORT} on each device..."
    while IFS= read -r device; do
        [ -z "$device" ] && continue
        adb -s "$device" reverse "tcp:${PORT}" "tcp:${PORT}"
        log_success "Reverse set on $device"
    done <<< "$DEVICES"
}

find_existing_apk() {
    find "$PROJECT_DIR/app/build/outputs/apk/$BUILD_VARIANT" -name "*.apk" -type f | head -1
}

build_if_needed() {
    APK_PATH="$(find_existing_apk || true)"

    if [ -n "$APK_PATH" ] && [ -f "$APK_PATH" ]; then
        log_info "Using existing APK: $APK_PATH"
        return
    fi

    log_warning "No APK found. Building assembleDebug (no clean)..."
    cd "$PROJECT_DIR"
    ./gradlew assembleDebug -x test
    APK_PATH="$(find_existing_apk || true)"

    if [ -z "$APK_PATH" ] || [ ! -f "$APK_PATH" ]; then
        log_error "APK not found even after build."
        exit 1
    fi

    log_success "Built APK: $APK_PATH"
}

install_apk() {
    log_info "Installing APK on each connected device..."
    while IFS= read -r device; do
        [ -z "$device" ] && continue
        adb -s "$device" install -r "$APK_PATH"
        if ! adb -s "$device" shell pm list packages | grep -q "$APP_PACKAGE"; then
            log_error "Install verification failed on $device"
            exit 1
        fi
        log_success "Installed on $device"
    done <<< "$DEVICES"
}

restart_app() {
    log_info "Restarting app on each connected device..."
    while IFS= read -r device; do
        [ -z "$device" ] && continue
        adb -s "$device" shell am force-stop "$APP_PACKAGE" || true
        log_info "Starting launcher activity on $device..."
        adb -s "$device" shell am start -W -n "$APP_ACTIVITY"

        local pid
        pid="$(adb -s "$device" shell pidof -s "$APP_PACKAGE" 2>/dev/null | tr -d '\r')"
        if [ -z "$pid" ]; then
            log_error "App did not start on $device"
            exit 1
        fi

        log_success "Relaunched on $device (pid=$pid)"
    done <<< "$DEVICES"
}

show_quick_logs_hint() {
    log_info "To stream logs for your app run:"
    echo "adb logcat --pid=\"\$(adb shell pidof -s $APP_PACKAGE)\""
}

show_proxy_status() {
    log_info "Current adb reverse mappings:"
    while IFS= read -r device; do
        [ -z "$device" ] && continue
        echo "--- $device ---"
        adb -s "$device" reverse --list || true
    done <<< "$DEVICES"
}

main() {
    echo -e "${GREEN}Quick Install + Proxy Setup${NC}"
    check_adb
    check_devices
    make_gradlew_executable
    ensure_proxy_reverse
    build_if_needed
    install_apk
    restart_app
    show_proxy_status
    show_quick_logs_hint
    log_success "Done. App should now reach backend via localhost:${PORT}."
}

main "$@"
