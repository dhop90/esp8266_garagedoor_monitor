RPi Garage Monitor Device Type with Camera and Temperature Sensor

Combinded with an ESP8266 microcontroller, this divice driver is capable of monitoring and controlling a garage door, reporting temperature, humity and wifi signal strength as well as taking a video of garage door state.

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

![](../../dhop90/rpi_garage_monitor/raw/master/ESP8266_pinout.jpg)
![](../../dhop90/rpi_garage_monitor/raw/master/nodemcu_pins.png) 
![](../../dhop90/rpi_garage_monitor/raw/master/cam-pinout.jpg)  
![](../../dhop90/rpi_garage_monitor/raw/master/ESP8266_pinout.jpg)
