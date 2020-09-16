# GpioManager_App
A mobile app that is used to remotely manage GPIO pins on the DragonBoard 410c - from everywhere! 

You can read more about this project in [this blog post](https://lukakralj.com/gpio-manager/).

## Aim
The purpose of this project was to set up a basis for a remote control of various components that are connected to GPIO pins on the DragonBoard (DB). The app enables to turn those components on and off with a single tap, or it can display if a sensor-like component is triggered or not. This app (together with the [server](https://github.com/lukakralj/GpioManager_Server)) can be a good starting point to create an IoT hub that can allow the control of many devices from your phone.

![Components screen shows all components that are currently registered.](./screenshots/small/components_screen.png?raw=true "Components screen shows all components that are currently registered.")

## How it works
The app connects to the server that is running on the DB (see [server repository](https://github.com/lukakralj/GpioManager_Server)) through an ngrok tunnel. For this you simply need to enter the correct URL address in the URL configuration screen.

![Server URL can be configured at runtime if it changes.](./screenshots/small/configure_url.png?raw=true "Server URL can be configured at runtime if it changes.")

User needs to log in in order to use the app, which prevents anyone else from messing with your DB's GPIOs. After that you can see the connected components, edit them, and also register new ones.

You can login from different devices as many times as you want. The information about components will be updated in real time on all of the devices for all the users logged in as soon as the changes occur.

![The components information is updated in real time.](./screenshots/small/retrieving_data.png?raw=true "The components information is updated in real time.")

## Requirements
The target API is 26, however, for the purposes of testing I tried using a phone with an API version 24 and everything worked fine.
Should you wish to change the minimum Sdk version, change it in App's build.gradle file and re-sync the project.

## Installation
The app is most easily installed through Android Studio.
1. Download this repo.
2. Open repo folder in Android Studio.
3. Turn on Developer options on your phone (see instructions [here](https://developer.android.com/studio/debug/dev-options)).
4. In Developer options tab in phone settings, enable USB-debugging and Allow installation via USB cable.
5. Connect your phone with a USB cable. 
6. On your phone, tap the notification that pops up. USB Preferences screen should open; on this screen enable File transfer option (instead of just charging your device).
7. Click the Run button (or Shift+F10) in Android Studio.
8. Select your phone from the list and click OK.
9. On your phone, allow installation if prompted.
10. After the installation the app will automatically open.

### Disclaimer
The UI was only tested on Xiaomi A2 Lite, so on your phone some buttons and other elements might shift off the screen. To fix this you will only need to manipulate the XML files in the `res/layout` folder.

The app normally works fine. Some minor glitches can occur if the app is inactive for a longer time and then accessed from the Recent apps. These sorts of glitches disappear if the app is restarted.

### Feedback
Whether you liked the project or not, I would be very thankful for any feedback, suggestions or comments on the project.

*Feel free to [email](mailto:luka.kralj.cs@gmail.com) me!*
