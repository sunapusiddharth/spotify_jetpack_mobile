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



for auth 0 
User-level (better for secrets):
~/.gradle/gradle.properties

AUTH0_CLIENT_ID=your_auth0_client_id
AUTH0_DOMAIN=your-tenant.us.auth0.com
AUTH0_SCHEME=com.music.stream.neptune.auth0

AUTH0_DOMAIN should be just the domain, not https:// (for example: dev-abc123.us.auth0.com).
AUTH0_CLIENT_ID is your Auth0 Application Client ID (this is what you called access key id).

Also set callback URL in Auth0 dashboard:
com.music.stream.neptune.auth0://<AUTH0_DOMAIN>/android/com.music.stream.neptune/callback