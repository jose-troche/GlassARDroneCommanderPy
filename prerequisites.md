# Prerequisites

## Launchy Installation

* In your computer, install [Android Studio](http://developer.android.com/sdk/installing/studio.html) 
or [Android Developer Tools (ADT)] (http://developer.android.com/sdk/installing/bundle.html).
From now on, the instructions are for Android Studio, but the steps are similar for ADT.
* Check out the [Launchy source code](https://github.com/kaze0/launchy) from github and import as 
project in Android Studio. There is a few ways to do that, just one of them is detailed below.
  * In Android Studio go to menu `VCS > Checkout from Version Control > GitHub`
  
  ![Checkout from Version Control screenshot]
  (http://jose-troche.github.io/GlassARDroneCommanderPy/img/CheckOutFromVersionControl.png)
  * Fill the Git Repository URL with https://github.com/kaze0/launchy. The other fields will be 
  automatically populated. Clone the repository.

  ![Clone Repo screenshot](http://jose-troche.github.io/GlassARDroneCommanderPy/img/clonerepo.png)
  * Answer **Yes** when asked if you would like to create a project from the checked out sources.
  * Click **Next** to all steps of the wizard dialog and **Finish** in the last step.
* In Google Glass turn on debug mode by going to `Settings > Device Info > Turn on debug`.
* Connect Google Glass to your computer via USB cable.
* In Android Studio, go to menu `Run > Edit Configurations...` and make sure that **USB device** 
is selected as **Target Device**
* Now run the application: `Run > Run 'launchy'`. That should install and attempt to run the application.
* Disconnect Glass from the computer.
* In Glass go to `Settings`. A dialog will give you the option of picking GlassHome or Launchy for this action.
Select Launchy and check the box to always use Launchy.
* Now every time you go to `Settings` Launchy will allow you to launch other installed Android applications 
(or the regular Settings).

##Bluetooth Connection Set Up
Set up a Bluetooth serial port connection between your computer and Google Glass:
* In Mac, go to System Preferences > Bluetooth > Advanced... > `+`, to add a new serial port connection with
  the following properties:
   * Check only the first checkbox (On)
   * Name: Bluetooth-SerialPort
   * Type: Modem

![System Preferences screenshot]
(http://jose-troche.github.io/GlassARDroneCommanderPy/img/SystemPreferences.png)
![Bluetooth screenshot](http://jose-troche.github.io/GlassARDroneCommanderPy/img/Bluetooth.png)
* In Glass, go to Settings > Bluetooth card (should state "Now discoverable")
![Bluetooth Glass screenshot]
(http://jose-troche.github.io/GlassARDroneCommanderPy/img/BluetoothGlass.png)
* In Mac, click the Bluetooth icon in the top menu bar and select `Set Up Bluetooth Device...`
![Bluetooth Top Menu Bar screenshot]
(http://jose-troche.github.io/GlassARDroneCommanderPy/img/BluetoothTopMenuBar.png)
* Pair Mac Bluetooth with Google Glass
