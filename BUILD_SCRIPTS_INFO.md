# 📦 Build & Install Scripts Overview

## 🚀 Getting Started (Choose One)

### Option A: Use the Master Script (EASIEST) ⭐
```bash
bash spotify-build.sh
```
**Benefits:** Simple, everything in one script, interactive
**Best for:** Most users

### Option B: Use Individual Scripts
```bash
./build_and_install.sh
```
**Benefits:** More control over each step
**Best for:** Advanced users, CI/CD pipelines

---

## Master Script: `spotify-build.sh`

The most important and easiest script to use.

### Basic Commands

```bash
# Full build & install (default)
./spotify-build.sh

# Or explicitly:
./spotify-build.sh all

# Just build
./spotify-build.sh build

# Just install
./spotify-build.sh install

# Uninstall
./spotify-build.sh uninstall

# Launch app
./spotify-build.sh launch

# View live logs
./spotify-build.sh logs

# Clean everything
./spotify-build.sh clean
```

### Advanced Options

```bash
# Skip clean during build (faster)
./spotify-build.sh build --skip-clean

# Quick install after code change
./spotify-build.sh build --skip-clean && ./spotify-build.sh install

# Build release APK
./spotify-build.sh build --release

# Get help
./spotify-build.sh --help
```

---

## Individual Scripts

If you prefer fine-grained control:

### 1. `build_and_install.sh` - Full Workflow
**What it does:**
- Checks for ADB and devices
- Clean build (optional prompt)
- Builds APK
- Uninstalls previous version
- Installs new APK
- Verifies installation
- Launches app (optional prompt)

**Usage:**
```bash
./build_and_install.sh
```

**When to use:** First time setup, clean rebuilds

---

### 2. `build_only.sh` - Just Build
**What it does:** Build APK without installing

**Usage:**
```bash
./build_only.sh
```

**Follow up with:**
```bash
./quick_install.sh
```

**When to use:** CI/CD, you just want the APK

---

### 3. `quick_install.sh` - No Build, Just Install
**What it does:** Install pre-built APK

**Usage:**
```bash
./quick_install.sh
```

**When to use:** After `build_only.sh`, rapid iteration

**Speed:** ⚡ Very fast (skips build)

---

### 4. `uninstall_app.sh` - Remove App
**What it does:** Cleanly uninstall from device

**Usage:**
```bash
./uninstall_app.sh
```

---

### 5. `launch_app.sh` - Start App
**What it does:** Start app without rebuilding

**Usage:**
```bash
./launch_app.sh
```

---

### 6. `view_logs.sh` - View Logs
**What it does:** Show real-time logcat output

**Usage:**
```bash
./view_logs.sh
```

**Exit:** Ctrl+C

---

### 7. `init_build_scripts.sh` - Setup
**What it does:** Make all scripts executable

**Usage:**
```bash
bash init_build_scripts.sh
```

**Run once at the start**

---

## 🎯 Recommended Workflows

### Workflow 1: First Time (Master Script)
```bash
cd /path/to/spotify-clone-jetpack-compose-main
bash spotify-build.sh
```

### Workflow 2: Rapid Development (Master Script)
**Terminal 1 (Watch Logs):**
```bash
./spotify-build.sh logs
```

**Terminal 2 (Build & Install):**
```bash
# Make code changes...
./spotify-build.sh build --skip-clean
./spotify-build.sh install
# Repeat as needed
```

### Workflow 3: Rapid Development (Individual Scripts)
**Terminal 1 (Watch Logs):**
```bash
./view_logs.sh
```

**Terminal 2 (Build & Install):**
```bash
# Make code changes...
./build_only.sh
./quick_install.sh
# Repeat as needed
```

### Workflow 4: Clean Install
```bash
./spotify-build.sh uninstall
./spotify-build.sh all
```

---

## 📊 Script Comparison

| Script | Speed | Setup | Control | Use Case |
|--------|-------|-------|---------|----------|
| `spotify-build.sh all` | Medium | Easy ✓ | High | Most users |
| `spotify-build.sh build + install` | Varies | Easy ✓ | High | Fine control |
| `build_and_install.sh` | Medium | Easy ✓ | Medium | Traditional flow |
| `build_only.sh` + `quick_install.sh` | Fast ⚡ | Hardcode | Low | Power users |

---

## ⚡ Performance Tips

### Fastest Iteration Cycle
```bash
# Initial setup
./spotify-build.sh

# Development loop (very fast)
./spotify-build.sh build --skip-clean
./spotify-build.sh install

# Or even shorter with master script:
./spotify-build.sh build --skip-clean && ./spotify-build.sh install
```

**Why fast?** Skipping clean build saves significant time

### Always Monitor Logs
Keep logs running in a separate terminal:
```bash
./spotify-build.sh logs
```

### Keep Emulator Running
Don't close the emulator between builds - redeploying to a running emulator is much faster

---

## 🔧 Troubleshooting

### "No devices found"
```bash
# Check if emulator is running
adb devices

# If not, start it from Android Studio or:
emulator -avd Your_AVD_Name
```

### "Permission denied" on scripts
```bash
bash spotify-build.sh
# OR
chmod +x *.sh
./spotify-build.sh
```

### Build fails
```bash
# Check Java
java -version  # Should be JDK 11+

# Manual build
./gradlew assembleDebug

# Check Gradle
./gradlew --version
```

### Installation fails
```bash
./spotify-build.sh uninstall
./spotify-build.sh install
```

---

## 📋 File Structure

```
spotify-clone-jetpack-compose-main/
├── spotify-build.sh              ⭐ Master script (use this!)
├── build_and_install.sh          Traditional full workflow
├── build_only.sh                 Build without installing
├── quick_install.sh              Install without building
├── uninstall_app.sh              Remove app
├── launch_app.sh                 Start app
├── view_logs.sh                  View logs
├── init_build_scripts.sh          Make scripts executable
├── QUICK_START.md                Quick reference
├── BUILD_INSTALL_README.md       Detailed documentation
└── BUILD_SCRIPTS_INFO.md         This file
```

---

## 💡 Key Takeaways

1. **For most users:** Use `./spotify-build.sh`
2. **For rapid development:** Use `build_only.sh + quick_install.sh` (very fast)
3. **For debugging:** Always have `view_logs.sh` running
4. **For clean install:** Use `./spotify-build.sh uninstall && ./spotify-build.sh all`
5. **Keep emulator running:** Between builds for faster deployment

---

## 🎵 You're Ready!

```bash
./spotify-build.sh
```

That's all you need to get started!

---

**For more detailed documentation:** See `BUILD_INSTALL_README.md` and `QUICK_START.md`
