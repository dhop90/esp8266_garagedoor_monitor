RPi Garage Monitor Device Type with Camera and Temperature Sensor

Combinded with an ESP8266 microcontroller, this divice driver is capable of monitoring and controlling a garage door, reporting temperature, humity and wifi signal strength as well as taking a video of garage door state.

Camera implementation was take from https://github.com/dmainmon/ArduCAM-mini-ESP8266-12E-Camera-Server

Copy the contents of the Base and ArduCAM into the Arduino Libraries folder. On a windows computer this is usually "Documents/Arduino/Libraries"

Copy the GaragedoorCamTempSensor to the Arduino/Sketches directory.

scripts directory contains a build script to be able to compile from the command line.

```
./scripts/build: usages: hostname sketch [compile|push|network|serial|uploadonly]
compile: only compiles the sketch
   push: compiles and commits changes to repo
network: compiles and tries to upload sketch via network
 serial: compiles and tries to upload sketch via serial
uploadonly: does not compiles Sketch, just up load it by network or serial
```

```
// Garagedoor controler and
// ESP8266-12E ArduCAM Mini Camera Server
// with Temperature sensor

// This program demonstrates using an ArduCAM Mini 2MP camera with an ESP8266-12E module.
// An OV2640 2MP ArduCAM Mini was used with this program.
//
// The program has a web interface that will allow:
//  - storing and viewing of captured jpg files
//
// Captured jpeg files are stored on the ESP8266-12E built in memory.
//
// The capture and streaming features can be reached directly via the browser by using the format:
//    http://IPaddress/capture - for still capture
//
// The captured stills can be accessed as an HTML image:
//    <img src="http://IPaddress/capture">
//
//  Wiring diagram to connect ArduCAM to ESP8266-12E
//
//     ArduCAM mini   ->    ESP8266-12E
//         CS         ->        D0
//        MOSI        ->        D7
//        MISC        ->        D6
//         SCK        ->        D5
//         GND        ->       GND
//         VCC        ->       3V3
//         SDA        ->        D2
//         SCL        ->        D1
```
![](https://github.com/dhop90/rpi_garage_monitor/blob/master/ESP8266_pinout.jpg)  
![](https://github.com/dhop90/rpi_garage_monitor/blob/master/nodemcu_pins.png)  
![](https://github.com/dhop90/rpi_garage_monitor/blob/master/cam-pinout.jpg)    
![](https://github.com/dhop90/rpi_garage_monitor/blob/master/IMG_6650.PNG) 
![](https://github.com/dhop90/rpi_garage_monitor/blob/master/IMG_6651.PNG) 

