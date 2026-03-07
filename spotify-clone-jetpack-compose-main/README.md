running the app
sh ./build_and_install.sh


Run this after device is connected:

 adb reverse tcp:9000 tcp:9000
adb reverse --list

Then restart app:

adb shell am force-stop com.music.stream.neptune
adb shell monkey -p com.music.stream.neptune -c android.intent.category.LAUNCHER 1



testing hls urls
https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8