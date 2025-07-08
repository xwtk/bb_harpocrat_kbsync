# HarpocratKBS
Syncs Physical Keyboard Layout to the one set using Harpocrat/BlackBerry Keyboard application on BlackBerry Passport devices running xwtk.Harpocrat software.

# How to use?
1. Compile this application using `./gradlew assembleRelease`
2. Sign the generated APK using your platform key since it utilizes Android Private APIs
3. Place into /system/priv-app/HarpocratKBS/HarpocratKBS.apk
4. Create a com.hardware.xwtk.harpocrat.kbsync.xml file at /system/etc/permissions/ and allow all the permissions listed in the AndroidManifest.xml

# Pre-requisites
1. Your ROM platform key
2. Harpocrat/BlackBerry Keyboard application integrated into your ROM (com.blackberry.keyboard)

Compatible only for BlackBerry Passport devices.
