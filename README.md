Glass Quadcopter Commander
==========================

Google Glass application to fly an ARDrone quadcopter.

![Glass and AR.Drone](http://jose-troche.github.io/GlassARDroneCommanderPy/img/glass-ardrone.png)
## Prerequisites
* In your computer:
    * Install [Android Studio](http://developer.android.com/sdk/installing/studio.html) or
      [Android Developer Tools (ADT)](http://developer.android.com/sdk/installing/bundle.html).
    * Install the [python ARDrone library](https://github.com/venthur/python-ardrone):
      * `git clone https://github.com/venthur/python-ardrone`
      * `mv python-ardrone ardrone_py`
      * `cd ardrone_py`
      * `touch __init__.py`
      * Add the parent directory of `ardrone_py` to the PYTHONPATH environment variable 
        (e.g. add `export PYTHONPATH=$HOME/<ardrone_py_parent_directory>` in `$HOME/.bash_profile`)

* In Google Glass:
    * Install (deploy) [Launchy](https://github.com/kaze0/launchy), so you can easily launch
      custom Android applications

* Set up a Bluetooth serial port connection between your computer and Google Glass:
   * In Mac, go to System Preferences > Bluetooth > Advanced... > `+`, to add a new serial port connection with
     the following properties:
      * Check only the first checkbox (On)
      * Name: Bluetooth-SerialPort
      * Type: Modem
   * In Glass, go to Settings > Bluetooth card (should state "Now discoverable")
   * In Mac, click the Bluetooth icon in the top menu bar and select `Set Up Bluetooth Device...`
   * Pair Mac Bluetooth with Google Glass

For more specific details and screenshots read [detailed prerequisites](prerequisites.md).

## Configuration
* Clone this repo
* With Android Studio or Android Developer Tools, compile and deploy this application to
Google Glass
* In your computer, listen on the Bluetooth serial port and pipe it to `ardrone_commander.py`:

```
    cd GlassARDroneCommanderPy
    adb install -r out/production/GlassARDroneCommanderPy/GlassARDroneCommanderPy.apk
    cat < /dev/tty.Bluetooth-SerialPort | ./ardrone_commander.py
```
* In Google Glass:
    * Tap on the Settings card and the select/launch `Quadcopter Commander`
    * In `Quadcopter Commander` tap on the menu option to connect to your computer's Bluetooth serial port
    * Take off and pilot your ARDrone!

