# 🎵 Spotify Clone - Build & Install Scripts Complete Guide

## 📦 What Was Created

A complete suite of build and deployment automation scripts for the Spotify-clone Jetpack Compose Android app.

### Files Created:

#### Scripts (Shell Scripts)
- **`spotify-build.sh`** - Master script (use this!)
- **`build_and_install.sh`** - Full custom workflow
- **`build_only.sh`** - Build APK without installing
- **`quick_install.sh`** - Install pre-built APK quickly
- **`uninstall_app.sh`** - Remove app from device
- **`launch_app.sh`** - Start the app
- **`view_logs.sh`** - View real-time logs
- **`setup_build_scripts.sh`** - Creates all scripts if needed

#### Documentation
- **`QUICK_START.md`** - Quick reference guide
- **`BUILD_INSTALL_README.md`** - Detailed documentation
- **`BUILD_SCRIPTS_INFO.md`** - Script comparison & workflows
- **`BUILD_SCRIPTS_COMPLETE.md`** - This file

---

## 🚀 Quick Start (3 Steps)

### Step 1: Setup Scripts (First Time Only)
```bash
cd /Users/pratima.suryavanshi/Downloads/projects/spotify_mobile_jetpack/spotify-clone-jetpack-compose-main
bash setup_build_scripts.sh
```

### Step 2: Ensure Emulator is Running
- Open Android Studio → Device Manager
- Click Play button on an emulator
- Or via terminal: `emulator -avd YourAVDName`

### Step 3: Build & Install
```bash
./spotify-build.sh
```

**Done!** The app will automatically build, install, and launch.

---

## 📋 Available Commands

### Using the Master Script (EASIEST)

```bash
./spotify-build.sh              # Full build & install
./spotify-build.sh build        # Build only
./spotify-build.sh install      # Install only
./spotify-build.sh uninstall    # Remove app
./spotify-build.sh launch       # Start app
./spotify-build.sh logs         # View logs
```

### Using Individual Scripts

```bash
./build_and_install.sh          # Complete workflow
./build_only.sh                 # Build APK
./quick_install.sh              # Install APK
./uninstall_app.sh              # Uninstall
./launch_app.sh                 # Launch
./view_logs.sh                  # Logs
```

---

## ⚡ Recommended Workflows

### Workflow 1: First-Time Setup ⭐
```bash
# Initialize (one time)
bash setup_build_scripts.sh

# Build & install
./spotify-build.sh

# Done! App is running.
```

### Workflow 2: Rapid Development with Logs
**Terminal 1 (Watch logs):**
```bash
./spotify-build.sh logs
```

**Terminal 2 (Build & install):**
```bash
# After making code changes:
./spotify-build.sh build
./spotify-build.sh install

# Repeat as needed - very fast!
```

### Workflow 3: Clean Rebuild
```bash
# When having issues
./spotify-build.sh uninstall
./spotify-build.sh

# Or rebuild from scratch
./build_and_install.sh  # Choose "yes" for clean build
```

### Workflow 4: Just Get APK (No Install)
```bash
./spotify-build.sh build
# APK is at: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎯 Visual Workflow Guide

### Flow 1: First Time
```
Start Emulator
    ↓
setup_build_scripts.sh
    ↓
./spotify-build.sh
    ↓
App Running ✓
```

### Flow 2: Development Iteration
```
Code Changes
    ↓
./spotify-build.sh build
    ↓
./spotify-build.sh install
    ↓
Test App
    ↓
Back to Code Changes (Loop)
```

### Flow 3: Full Rebuild
```
./spotify-build.sh uninstall
    ↓
./spotify-build.sh build
    ↓
./spotify-build.sh install
    ↓
./spotify-build.sh launch
```

---

## 📁 Directory Structure

```
spotify-clone-jetpack-compose-main/
├── 🎬 Master Script
│   └── spotify-build.sh              ⭐ USE THIS!
│
├── 📜 Individual Scripts
│   ├── build_and_install.sh
│   ├── build_only.sh
│   ├── quick_install.sh
│   ├── uninstall_app.sh
│   ├── launch_app.sh
│   └── view_logs.sh
│
├── 🔧 Setup Script
│   └── setup_build_scripts.sh
│
├── 📚 Documentation
│   ├── QUICK_START.md
│   ├── BUILD_INSTALL_README.md
│   ├── BUILD_SCRIPTS_INFO.md
│   └── BUILD_SCRIPTS_COMPLETE.md (this file)
│
└── 📱 App Source
    └── app/src/...
```

---

## ✅ Prerequisites

Before running the scripts, ensure you have:

- ✅ **Android SDK Tools** - includes ADB
- ✅ **JDK 11+** - `java -version`
- ✅ **Android Emulator running** OR connected device
- ✅ **Gradle** - (included in project)

**Quick Check:**
```bash
adb devices        # Should show your device
java -version      # Should be JDK 11+
```

---

## 🔧 Troubleshooting

### Problem: Scripts Won't Run
```bash
# Solution
bash setup_build_scripts.sh
bash spotify-build.sh
```

### Problem: "No devices found"
```bash
# List connected devices
adb devices

# If empty, start emulator from Android Studio
# Or: emulator -avd YourAVDName
```

### Problem: "Permission denied"
```bash
bash setup_build_scripts.sh
```

### Problem: Build fails
```bash
# Check Java version
java -version  # Should be 11+

# Try manual build
./gradlew assembleDebug
```

### Problem: Installation fails
```bash
./spotify-build.sh uninstall
./spotify-build.sh install
```

### Problem: App won't launch
```bash
./spotify-build.sh logs  # Check logs for errors
```

---

## 📊 Performance Comparison

| Approach | Time | Use Case |
|----------|------|----------|
| Full build & install | ~2-3 min | First setup |
| Build only | ~1-2 min | Getting APK |
| Build + install | ~2-3 min | Code changes |
| Re-install only | ~10 sec | No code changes |
| Just launch | ~2 sec | Already installed |

---

## 💡 Pro Tips

1. **Keep emulator running** between builds - redeployment is faster
2. **Use separate terminals**:
   - Terminal 1: `./spotify-build.sh logs`
   - Terminal 2: Building and installing
3. **Watch logs while testing** to catch errors immediately
4. **Clean build for major changes:**
   ```bash
   ./build_and_install.sh
   # Answer "yes" to clean build prompt
   ```

---

## 🎬 Example Session

```bash
# Step 1: First time setup
cd ~/Downloads/projects/spotify_mobile_jetpack/spotify-clone-jetpack-compose-main
bash setup_build_scripts.sh

# Step 2: Make sure emulator is running
emulator -avd Pixel_4_API_30 &

# Step 3: Build and install
./spotify-build.sh

# Step 4: Open another terminal to watch logs
./spotify-build.sh logs

# Step 5: Make some code changes...
# Step 6: Quick rebuild
./spotify-build.sh build
./spotify-build.sh install

# Step 7: Check logs for any errors
# (Logs are already showing in the other terminal)

# Done!
```

---

## 🔄 Development Loop (Fastest)

```bash
# Terminal 1: Watch logs
./spotify-build.sh logs

# Terminal 2: Edit code, then:
./spotify-build.sh build && ./spotify-build.sh install

# Repeat step 2 with each code change
# Very fast - only rebuilds what changed!
```

---

## 📝 Script Details

### `spotify-build.sh` (Master)
- **Best for:** Most users
- **Features:** Simple commands, color output, error handling
- **Speed:** Medium to slow (includes clean builds)

### `build_and_install.sh` (Traditional)
- **Best for:** Custom workflows
- **Features:** Interactive prompts, detailed logging
- **Speed:** Medium to slow

### `build_only.sh` + `quick_install.sh` (Fast Iteration)
- **Best for:** Rapid development
- **Features:** Skip unnecessary steps
- **Speed:** Fast! ⚡

---

## 🚨 Important Notes

1. **App Package:** `com.music.stream.neptune`
2. **Build Variant:** `debug` (default)
3. **APK Location:** `app/build/outputs/apk/debug/app-debug.apk`
4. **Main Activity:** `.MainActivity`

---

## 📞 Support Commands

```bash
# View all devices
adb devices

# Check app is installed
adb shell pm list packages | grep com.music.stream.neptune

# Force stop app
adb shell am force-stop com.music.stream.neptune

# Clear app data
adb shell pm clear com.music.stream.neptune

# Start fresh
adb shell pm clear com.music.stream.neptune
./spotify-build.sh launch
```

---

## 🎉 You're All Set!

Run this command to start:
```bash
./spotify-build.sh
```

Or if scripts need setup first:
```bash
bash setup_build_scripts.sh
./spotify-build.sh
```

---

## 📚 Documentation Files

- **`QUICK_START.md`** - Quick command reference
- **`BUILD_INSTALL_README.md`** - Detailed documentation
- **`BUILD_SCRIPTS_INFO.md`** - Script comparison and workflows
- **`BUILD_SCRIPTS_COMPLETE.md`** - This comprehensive guide

---

**Happy coding! 🚀**

For questions or issues, check the troubleshooting section above or refer to the detailed documentation files.
