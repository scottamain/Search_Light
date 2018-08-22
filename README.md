# Search_Light


I originally created this app for Android 2.0 (Eclair) at a time when all available flashlight apps were device-specific because there was no framework API to control the camera LED. That is, until Android 2.0 (API level 5).

So this is a simple app that turns on the LED for a flashlight and is compatible with any device running Android 2.0 or higher.

I also added a viewfinder using the camera so you can see into tight spots that might be blocked by your phone as you shine the light.

**I no longer support this app** because I foolishly lost my signing key and so have been unable to upload a new APK to Google Play. But all devices since Android 4.0 now include a built-in flashlight, so there's been no need for me to support it anyway. The app has also been removed from Google Play because I also failed to re-agree to the store's new terms of service. ¯\_(ツ)_/¯

This repo is merely a copy of the last snapshot available from the original [Google Code project](https://code.google.com/archive/p/search-light/source/default/commits), which is actually an unreleased version of the Search Light app. So this code isn't quite stable and I don't know how well it actually works.

But if you're interested, you're free to use the code here. It's not pretty and it includes some code kludges that were required for some device compatibility (looking at you OG Droid!).


**About the license**

I was working at Google when I wrote this app, which explains the copyright notice in each file, and the choice of Apache 2.0 as the license. And if you have read this code, I'll say in defense of myself and Google, I'm not a SWE. :)