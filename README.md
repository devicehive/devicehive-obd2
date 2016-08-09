**DeviceHive OBD2 Gateway for OBD2 compatible cars**
--------------------------------

DeviceHive OBD2 Gateway collect vehicle's self-diagnostic information and send it cloud. This gateway works with Anroid devices and Bluetooth ELM327 adaptors.

![](obd2-architecture.png?raw=true)

**Usaga**
------------------
- Pair your ELM327 adaptor with you devices using system Bluetooth settings
- Install and run this app
- Enter DeviceHive server credentials and choose your ELM327 from list of paired devices
- Click on `Start service` and app will log OBD2 data to cloud

**Commands**
------------------
There are few commands which can be executed from DeviceHive server:
- `GetTroubleCodes` - read OBD2 trouble codes and return them in the command result. No parameters require.
- `RunCommand` - run arbitrary OBD2 command and get the result. `mode` and `pid` parameters should be specified in command parameters in a single byte hex string with leading zero. Example `{"mode":"01","pid":"0C"}` ('Engine RPM' PID).

**Legacy**
------------------
This project is based on:
- DeivceHive Android BLE gateway - https://github.com/devicehive/android-ble
- OBD-II Java API - https://github.com/pires/obd-java-api

**DeviceHive license**
------------------

DeviceHive is developed by DataArt Apps and distributed under Open Source MIT license. This basically means you can do whatever you want with the software as long as the copyright notice is included. This also means you don't have to contribute the end product or modified sources back to Open Source, but if you feel like sharing, you are highly encouraged to do so!

