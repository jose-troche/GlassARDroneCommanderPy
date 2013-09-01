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
      * Add the parent directory of `ardrone_py` to the PYTHONPATH environment variable.
    * Set up a Bluetooth serial port connection between computer and Google Glass:
      * In Mac, go to Settings > Bluetooth > Advanced... > +, to add a new serial connection port with
        the following properties:
         * Check only the first checkbox (On)
         * Name: Bluetooth-SerialPort
         * Type: Modem
      * In Glass, go to Settings > Bluetooth card (should state "Now discoverable"). 
      * In Mac, click the Bluetooth icon in the top menu bar and select `Set Up Bluetooth Device...`.
      * Pair Mac Bluetooth with Google Glass.

* In Google Glass:
    * Install (deploy) [Launchy](https://github.com/kaze0/launchy), so you can easily launch other
      installed Android applications from the Glass Settings.

If you need more specific details read [prerequisites](prerequisites.md).

## Configuration
* Clone this repo
* With Android Studio or Android Developer Tools, compile and deploy this application to
Google Glass
* In your computer, listen on the Bluetooth serial port and pipe it to ardrone_commmander.py:

```
    cd GlassQuadcopterCommander
    adb install -r out/production/GlassQuadcopterCommander/GlassQuadcopterCommander.apk
    cat < /dev/tty.Bluetooth-SerialPort | ./ardrone_commander.py
```
* In Google Glass:
    * Launch Quadcopter Commander (via Launchy)
    * Connect to your computer's Bluetooth serial port
    * Pilot your ARDrone!
