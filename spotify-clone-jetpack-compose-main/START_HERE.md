# 📋 Build Scripts - Quick Reference Card

## 🎯 TL;DR - Just Do This

```bash
# First time only
bash setup_build_scripts.sh

# Then run this
./spotify-build.sh

# Done! App is running on your emulator.
```

---

## 📦 What You Got

### ✨ Master Script (Use This!)
- **`spotify-build.sh`** - One command to rule them all

### 🔧 Individual Scripts
- `build_and_install.sh` - Full custom workflow
- `build_only.sh` - Build APK
- `quick_install.sh` - Install APK (fast)
- `uninstall_app.sh` - Remove app
- `launch_app.sh` - Start app
- `view_logs.sh` - View logs

### 📚 Documentation
- `BUILD_SCRIPTS_COMPLETE.md` - Full guide ⭐
- `BUILD_SCRIPTS_INFO.md` - Script comparison
- `BUILD_INSTALL_README.md` - Detailed docs
- `QUICK_START.md` - Quick reference

---

## 🚀 Usage

### Option 1: Master Script (Easiest)
```bash
./spotify-build.sh              # Full build & install
./spotify-build.sh build        # Build only
./spotify-build.sh install      # Install only
./spotify-build.sh logs         # View logs
```

### Option 2: Individual Scripts
```bash
./build_only.sh                 # Build
./quick_install.sh              # Install (fast!)
./view_logs.sh                  # Logs
```

---

## ⚡ Fastest Development Loop

**Terminal 1:**
```bash
./spotify-build.sh logs
```

**Terminal 2:**
```bash
# Make code changes...
./spotify-build.sh build && ./spotify-build.sh install

# Repeat as needed
```

---

## 📊 Command Reference

| Command | Purpose | Speed |
|---------|---------|-------|
| `./spotify-build.sh` | Full build & install | Medium |
| `./spotify-build.sh build` | Build APK | Medium |
| `./spotify-build.sh install` | Install APK | Fast ⚡ |
| `./spotify-build.sh logs` | View logs | N/A |
| `./spotify-build.sh uninstall` | Remove app | Fast ⚡ |

---

## ✅ Checklist Before Starting

- [ ] Emulator is running (or device connected)
- [ ] Java is installed: `java -version`
- [ ] ADB works: `adb devices`

---

## 🆘 Quick Troubleshooting

**Scripts won't run?**
```bash
bash setup_build_scripts.sh
```

**No devices found?**
```bash
adb devices  # Check if emulator is running
# Start emulator from Android Studio
```

**Build fails?**
```bash
./gradlew assembleDebug    # Manual build
# Check error messages
```

---

## 📝 Example Workflow

```bash
# 1. Initial setup
bash setup_build_scripts.sh

# 2. Build & install
./spotify-build.sh

# 3. App is now running!

# 4. For subsequent changes:
# Make code changes...
./spotify-build.sh build
./spotify-build.sh install

# 5. Watch logs in another terminal
./spotify-build.sh logs
```

---

## 🎬 Visual Quick Reference

```
Want to...              Command                     Speed
─────────────────────────────────────────────────────────
Build & install app     ./spotify-build.sh          🐌
Build & install app     ./build_and_install.sh      🐌
Build APK               ./spotify-build.sh build    🐌
Install APK             ./spotify-build.sh install  ⚡
Uninstall app           ./spotify-build.sh uninstall ⚡
Launch app              ./spotify-build.sh launch   ⚡
View logs               ./spotify-build.sh logs     ⚡
```

---

## 📚 Read Full Documentation

For complete details about:
- Advanced options
- All available scripts
- Detailed workflows
- Troubleshooting
- Performance tips

👉 Read: **`BUILD_SCRIPTS_COMPLETE.md`** (in the project root)

---

**Start Here:**
```bash
./spotify-build.sh
```

**Need More Info?**
```bash
./spotify-build.sh --help
```

**Quick Setup:**
```bash
bash setup_build_scripts.sh
./spotify-build.sh
```

---

**That's it! 🎵**
