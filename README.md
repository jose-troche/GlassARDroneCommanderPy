Glass Quadcopter Commander
==========================

Google Glass application to fly an AR.Drone quadcopter.

![Glass and AR.Drone](http://jose-troche.github.io/GlassQuadcopterCommanderPy/img/glass-ardrone.png)
## Prerequisites
* In your computer:
    * Install [Android Studio](http://developer.android.com/sdk/installing/studio.html) or
      [Android Developer Tools (ADT)](http://developer.android.com/sdk/installing/bundle.html).
    * Download the [python ARDrone library](https://github.com/venthur/python-ardrone) into a 
      directory called `ardrone_py` and
      make it available to Python by setting PYTHONPATH. Inside that directory type: `touch __init__.py`
    * Set up a Bluetooth serial port:
      * In Mac, got to Settings > BlueTooth > Advanced Settings and create a new connection port
      * In Glass, go to Settings > Bluetooth card (Should read "Now discoverable"). 
        In Mac, go to Bluetooth devices and pair it with google glass

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
