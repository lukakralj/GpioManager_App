# GpioManager_App
A mobile app that is used to remotely manage GPIO pins on the DragonBoard 410c - from everywhere!

# Aim
The purpose of this project was to set up a basis for a remote control of various components that are connected to GPIO pins on the DragonBoard (DB). The app enables to turn those components on and off with a single tap, or it can display if a sensor-like component is triggered or not.

# How it works
The app connects to the server that is running on the DB (see [server repository](https://github.com/lukakralj/GpioManager_Server)) through an ngrok tunnel. For this you simply need to enter the correct URL address in the URL configuration screen.

User needs to log in in order to use the app, which prevents anyone else from messing with your DB's GPIOs. After that you can see the connected components, edit them, and also register new ones.

# Requirements
The target API is 26, however, for the purposes of testing I tried using a phone with an API version 24 and everything worked fine.
Should you wish to change the minimum Sdk version, change it in App's build.gradle file and re-sync the project.

# Installation
The app is most easily installed through Android Studio.
1. Download this repo.
2. Open repo folder in Android Studio.
3. Turn on Developer options on your phone (see instructions [here](https://developer.android.com/studio/debug/dev-options)).
4. In Developer options tab in phone settings, enable USB-debugging and Allow installation via USB cable.
5. Connect your phone with a USB cable. 
6. On your phone tap the notification that pops up - USB Preferences - and enable File transfer.
7. Click the Run button (or Shift+F10) in Android Studio.
8. Select your phone and click OK.
9. On your phone, allow installation if prompted.
10. After the installation the app will automatically open.

# Disclaimer
The UI was only tested on Xiaomi A2 Lite, so on your phone some buttons and other elements might shift off the screen. To fix this you will only need to manipulate the XML files in the `res/layout` folder.
