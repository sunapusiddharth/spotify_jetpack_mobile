#!/bin/bash
# Setup All Build Scripts
# Run this once to create and configure all build scripts

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Creating build scripts..."

# Create quick_install.sh
cat > "$PROJECT_DIR/quick_install.sh" << 'EOF'
#!/bin/bash
set -e
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
APP_PACKAGE="com.music.stream.neptune"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_VARIANT="debug"
log_info "Quick Install Script"
APK_PATH=$(find "$PROJECT_DIR/app/build/outputs/apk/$BUILD_VARIANT" -name "*.apk" -type f | head -1)
[ -z "$APK_PATH" ] && { log_error "APK not found"; exit 1; }
log_info "Uninstalling existing app..."
adb uninstall "$APP_PACKAGE" || true
log_info "Installing APK..."
adb install -r "$APK_PATH"
log_success "Installation complete!"
EOF
chmod +x "$PROJECT_DIR/quick_install.sh"

# Create build_only.sh
cat > "$PROJECT_DIR/build_only.sh" << 'EOF'
#!/bin/bash
set -e
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_VARIANT="debug"
log_info "Making gradlew executable..."
chmod +x "$PROJECT_DIR/gradlew"
log_info "Building APK..."
cd "$PROJECT_DIR"
./gradlew "assemble${BUILD_VARIANT^}" -x test
APK_PATH=$(find "$PROJECT_DIR/app/build/outputs/apk/$BUILD_VARIANT" -name "*.apk" -type f | head -1)
[ -z "$APK_PATH" ] && { log_error "Build failed"; exit 1; }
log_success "APK built successfully!"
log_info "Location: $APK_PATH"
EOF
chmod +x "$PROJECT_DIR/build_only.sh"

# Create uninstall_app.sh
cat > "$PROJECT_DIR/uninstall_app.sh" << 'EOF'
#!/bin/bash
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
APP_PACKAGE="com.music.stream.neptune"
log_info "Uninstalling app..."
adb uninstall "$APP_PACKAGE" && log_success "App uninstalled"
EOF
chmod +x "$PROJECT_DIR/uninstall_app.sh"

# Create launch_app.sh
cat > "$PROJECT_DIR/launch_app.sh" << 'EOF'
#!/bin/bash
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
APP_PACKAGE="com.music.stream.neptune"
adb shell am start -n "$APP_PACKAGE/.MainActivity"
log_success "App launched!"
EOF
chmod +x "$PROJECT_DIR/launch_app.sh"

# Create view_logs.sh
cat > "$PROJECT_DIR/view_logs.sh" << 'EOF'
#!/bin/bash
BLUE='\033[0;34m'
NC='\033[0m'
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
APP_PACKAGE="com.music.stream.neptune"
log_info "Displaying real-time logs (Ctrl+C to exit)"
adb logcat | grep "$APP_PACKAGE"
EOF
chmod +x "$PROJECT_DIR/view_logs.sh"

# Create spotify-build.sh (master script)
cat > "$PROJECT_DIR/spotify-build.sh" << 'EOF'
#!/bin/bash
set -e
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
show_usage() { cat << USAGE
Spotify Clone Build Script
Usage: ./spotify-build.sh [command]
Commands:
  (none) or 'all'    Full build & install
  build              Build APK only
  install            Install pre-built APK
  uninstall          Remove app
  launch             Start app
  logs               View live logs
Examples:
  ./spotify-build.sh
  ./spotify-build.sh build
  ./spotify-build.sh install
USAGE
}
COMMAND="${1:-all}"
case "$COMMAND" in
    all) log_info "Full build & install"; ./build_and_install.sh ;;
    build) log_info "Building APK"; ./build_only.sh ;;
    install) log_info "Installing APK"; ./quick_install.sh ;;
    uninstall) log_info "Uninstalling"; ./uninstall_app.sh ;;
    launch) log_info "Launching"; ./launch_app.sh ;;
    logs) log_info "Showing logs"; ./view_logs.sh ;;
    *) show_usage ;;
esac
EOF
chmod +x "$PROJECT_DIR/spotify-build.sh"

echo "✓ All build scripts created and made executable!"
echo ""
echo "Available commands:"
echo "  ./spotify-build.sh              (or ./spotify-build.sh all)"
echo "  ./spotify-build.sh build"
echo "  ./spotify-build.sh install"
echo "  ./spotify-build.sh uninstall"
echo "  ./spotify-build.sh launch"
echo "  ./spotify-build.sh logs"
echo ""
echo "Quick start: ./spotify-build.sh"
