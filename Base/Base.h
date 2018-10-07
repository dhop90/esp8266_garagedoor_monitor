/*
 *   Base.h - Library for setting basic nodemcu/ESP8266 code.
 *   Created by David Hopson, May 26, 2018.
 *   Released into the public domain.
 *   
 */

#ifndef Base_h
#define Base_h

#include "Arduino.h"
#include <ArduinoJson.h>
#include <ESP8266WebServer.h>

////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////


class Base
{

public:	
	void handleClient();
	void setupCreds(char * user, char * pass);
	void setupWebServer();
	void setDevicename();
	String hostname();
	String ipaddress();
	void setupMDNS();
	void setupWifi(char * ssid, char * pass);
	void setupStats();
	void setupNTP();
	void setupOTA();
	void setupJSON();
	void setupVersion(String ver);
	void setupPins(uint8_t pin1, uint8_t pin2);
	void setupApi();
	void sendToSyslog();
	void sendUdpSyslog(String msg1, String msg2, String msg3);
	bool validateCreds();
	void get_led(uint8_t pin, JsonObject& lightJson);
	void led2();
	void led16();
	void allLedOn();
	void allLedOff();
	void toggleLed2();
	void toggleLed16();
	void printLCD();
	void device();
	void jsonDoor(String state);
	char* www_username;
	char* www_password;
	ESP8266WebServer server;
	WiFiClient client;
	String api;

private:
	void sendJSONToSyslog();
	void setup_JSON(JsonObject& json);
	void print_JSON(JsonObject& json);
	void printHeap ();
	void printJSON();
	void getVersion(String ver);
	void handleNotFound();
	void heap();
	void rssi();
	void toggle();
	void gettime();
	void set_led(uint8_t pin, bool value);
	void ledOn();
	void ledOff();
	void bootTime();
	void sendVersion();
	void restartESP();
	void help();
	void serverOk();
};		

#endif
