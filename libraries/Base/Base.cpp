/*
 *    Base.cpp - Library for setting basic nodemcu/ESP8266 code.
 *   Created by David Hopson, May 26, 2018.
 *   Released into the public domain.
 *   
*/
#include <Base.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPUpdateServer.h>
#include <ESP8266mDNS.h>
#include <ArduinoOTA.h>
#include <TimeLib.h>
#include <NtpClientLib.h>
#include <dht.h>
#include <LiquidCrystal_I2C.h>



String SOFTWARE_VERSION;
char* ssid;
char* password;

// Device
String devicename;
const char* DEVICENAME = "ESP8266_";
char st_devicename[sizeof DEVICENAME+32];

// Pins
uint8_t pin_led2; //on-board led
uint8_t pin_led16; //on-board led
dht DHT;

//const PROGMEM int SDA = D1; //4; RELAY
//const PROGMEM int SCL = D0; // 13 = D7 was D2
//SDA = D1; //4; RELAY
//SCL = D0; // 13 = D7 was D2
#define DHT11_PIN 2 

// Server Declarations
MDNSResponder mdns;
//ESP8266WebServer server(80);
ESP8266HTTPUpdateServer httpUpdater;
WiFiClient client;

// JSON settings
const int JSONBUFFERSZ = 700;
StaticJsonBuffer<JSONBUFFERSZ> jsonBufferBase;
JsonObject& json = jsonBufferBase.createObject();

StaticJsonBuffer<JSONBUFFERSZ> jsonBufferBase2;
JsonObject& lightJson = jsonBufferBase2.createObject();

// Syslog settings
WiFiUDP udp;
IPAddress syslogServer(192, 168, 86, 11);
const PROGMEM int syslogPort = 514;

// User account
char* www_username;
char* www_password;
LiquidCrystal_I2C lcd(0x3F, 16, 2);

String api;

////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////
void Base::setupApi() {

api = "\nAPI:\n" 
      "/            : Tests server, returns OK\n"
      "/help        : Prints out API\n"
      "/version     : Displays the version of code\n"
      "/time        : Get current time\n"    
      "/boottime    : Returns date time of boot time\n"
      "/heap        : Returns amount of free heap\n"
      "/rssi        : Prints out Received Signal Strength Indicator\n"
      "/device      : Displays the JSON information of device\n"
      "/restart     : Restarts device\n"
      "/toggleLed16 : toggles Led 16\n"
      "/led16       : get status of led16\n";
}

////////////////////////////////////////////////////////////////
// setupCreds
////////////////////////////////////////////////////////////////
void Base::setupCreds(char* www_user, char* www_pass) {
	www_username = www_user;
	www_password = www_pass;
}

////////////////////////////////////////////////////////////////
// setupPins
////////////////////////////////////////////////////////////////
void Base::setupPins(uint8_t pin1, uint8_t pin2) {
   pin_led2 = pin1;
   pin_led16 = pin2;
}	

////////////////////////////////////////////////////////////////
// setupVersion
////////////////////////////////////////////////////////////////
void Base::setupVersion(String ver) {
   SOFTWARE_VERSION = ver;
}

////////////////////////////////////////////////////////////////
// hostname
////////////////////////////////////////////////////////////////
String Base::hostname() {
   return(json["esp.Devicename"]);
}	

////////////////////////////////////////////////////////////////
// ipaddress
////////////////////////////////////////////////////////////////
String Base::ipaddress() {
   return(json["esp.IPaddress"]);
}	

////////////////////////////////////////////////////////////////
// setupOTA
////////////////////////////////////////////////////////////////
void Base::setupOTA() {
  Serial.println(F("Setting up OTA"));
  ArduinoOTA.setPort(8266);

  // Hostname defaults to esp8266-[ChipID]
  ArduinoOTA.setHostname(st_devicename);

  // No authentication by default
  //ArduinoOTA.setPassword("admin");

  // Password can be set with it's md5 value as well
  ArduinoOTA.setPasswordHash("36a9e9afd50b5a48589bb4ffb15c5497");

  ArduinoOTA.onStart([]() {
    String type;
    if (ArduinoOTA.getCommand() == U_FLASH)
      type = "sketch";
    else // U_SPIFFS
      type = "filesystem";

    // NOTE: if updating SPIFFS this would be the place to unmount SPIFFS using SPIFFS.end()
    Serial.println("Start updating " + type);
  });
  
  ArduinoOTA.onEnd([]() {
    Serial.printf("\nEnd");
  });
  
  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  });
  
  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("Error[%u]: ", error);
    if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
    else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
    else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
    else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
    else if (error == OTA_END_ERROR) Serial.println("End Failed");
  });
  
  ArduinoOTA.begin();
  Serial.println(F("....OTA Ready"));
}

////////////////////////////////////////////////////////////////
// setupJSON
////////////////////////////////////////////////////////////////
void Base::setupJSON() {
  Base:setup_JSON(json);
}

void temp() {
   int chk = DHT.read11(DHT11_PIN);
   json["esp.Temperature"] = (int)round(1.8*DHT.temperature+32); 
   json["esp.Humidity"] = (int)DHT.humidity;
}

////////////////////////////////////////////////////////////////
// setup_JSON
////////////////////////////////////////////////////////////////
void Base::setup_JSON(JsonObject& json) {
  Serial.println(F("setup JSON"));
  json["esp.Devicename"] = st_devicename;
  json["esp.IPaddress"] = WiFi.localIP().toString();
  json["esp.MACaddress"] = WiFi.macAddress();
  json["esp.BootTime"] = NTP.getTimeDateString();
  json["esp.ChipId"] = ESP.getChipId();
  json["esp.FlashChipId"] = ESP.getFlashChipId();
  json["esp.FlashChipSize"] = ESP.getFlashChipSize();
  json["esp.RealFlashChipSize"] = ESP.getFlashChipRealSize();
  json["esp.FlashChipSpeed"] = ESP.getFlashChipSpeed();
  json["esp.Freeheap"] = ESP.getFreeHeap();
  json["esp.CoreVersion"] = ESP.getCoreVersion();
  json["esp.SdkVersion"] = ESP.getSdkVersion();
  json["esp.CpuFreqMHz"] = ESP.getCpuFreqMHz();
  json["esp.SketchSize"] = ESP.getSketchSize();
  json["esp.SketchVersion"] = SOFTWARE_VERSION;
  json["esp.SketchName"] = "CHANGENAME";
  json["esp.ResetReason"] = ESP.getResetReason();
  json["esp.Rssi"] = 2 * (WiFi.RSSI() + 100);
  Serial.println(F("....JSON created"));
  //lcd.begin(16,2);
  //lcd.init();
  //lcd.backlight();
  temp();
}

////////////////////////////////////////////////////////////////
// setupNTP
////////////////////////////////////////////////////////////////
void Base::setupNTP() {
  NTP.begin("pool.ntp.org", -6, true);
  NTP.setInterval(60);
}

////////////////////////////////////////////////////////////////
// setupWifi
////////////////////////////////////////////////////////////////
void Base::setupWifi(char* s, char* p) {
  ssid = s;
  password = p;
  Serial.println(F("Setting up Wifi"));
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  Serial.println(F("waiting to connect"));
  while (WiFi.waitForConnectResult() != WL_CONNECTED) {
    Serial.print("Connection Failed! Rebooting...");
    delay(5000);
    ESP.restart();
  }
  Serial.println(F("....Wifi Connected"));
}

////////////////////////////////////////////////////////////////
// printJSON
////////////////////////////////////////////////////////////////
void Base::printJSON() {
  Base:print_JSON(json);
}

////////////////////////////////////////////////////////////////
// print_JSON
////////////////////////////////////////////////////////////////
void Base::print_JSON(JsonObject& json) {
  json.prettyPrintTo(Serial);
}	

////////////////////////////////////////////////////////////////
// setDevicename
////////////////////////////////////////////////////////////////
void Base::setDevicename() {
  Serial.println(F("Setting Devicename"));
  String strMAC(WiFi.macAddress());
  strMAC.replace(":", "");

  devicename = DEVICENAME+strMAC;
  devicename.toLowerCase();

  devicename.toCharArray(st_devicename, sizeof(devicename)+sizeof(strMAC));
  Serial.println(F("....Devicename set"));
}

////////////////////////////////////////////////////////////////
// setupMDNS
////////////////////////////////////////////////////////////////
void Base::setupMDNS() {
  Serial.println(F("Setting up MDNS responder"));
  mdns.enableArduino(8266,false);

  if (mdns.begin(st_devicename,WiFi.localIP(),120))
     Serial.println(F("MDNS Responder has Started!"));
  mdns.update();
  httpUpdater.setup(&server);
  server.begin();
  mdns.addService("http","tcp",80);

  Serial.println(F("....MDNS responder started"));
}

////////////////////////////////////////////////////////////////
// sendToSyslog
////////////////////////////////////////////////////////////////
void Base::sendToSyslog() {
  Serial.println(F("sending JSON to syslog..."));
  udp.beginPacket(syslogServer, syslogPort);
  udp.write(st_devicename);
  udp.write(" ");
  udp.write("ESP8266");
  udp.write(" ");
  json.printTo(udp);
  udp.endPacket();
}
////////////////////////////////////////////////////////////////
// validateCreds
//////////////////////////////////////////////////////////////////
bool Base::validateCreds() {
  if (server.authenticate(www_username, www_password)) {
      //Serial.println("validated");
      return true;
  } else {
      //Serial.println("NOT validated");
      return false;
  }    
}


////////////////////////////////////////////////////////////////
// serverOk
////////////////////////////////////////////////////////////////
void Base::serverOk() {
  if (Base::validateCreds()) {
     server.send(200,"text/plain","Server Ok...\n");
     Serial.println(F("Server Ok..."));
     Base::sendUdpSyslog("ESP8266","Server:","Ok");
  }  
}

////////////////////////////////////////////////////////////////
// heap
////////////////////////////////////////////////////////////////
void Base::heap() {
  if (Base::validateCreds()) {
      server.send(200, "text/plain", "Current free heap: "+String(ESP.getFreeHeap())+"\n");
  }
}

////////////////////////////////////////////////////////////////
// restartESP
////////////////////////////////////////////////////////////////
void Base::restartESP() {
  if (Base::validateCreds()) {
    server.send(200,"text/plain","restarting...\n");
    Serial.println("restarting...");
    delay(5000);
    ESP.restart();
  }
}

////////////////////////////////////////////////////////////////
// toggle
////////////////////////////////////////////////////////////////
void Base::toggle() {
  if (Base::validateCreds()) {
    server.send(200,"text/plain","Toggle Activated...\n");
    Serial.print(F("Toggle Activated..."));
    digitalWrite(pin_led2,!digitalRead(pin_led2));
    digitalWrite(pin_led16,!digitalRead(pin_led16));
  }
}

////////////////////////////////////////////////////////////////
// gettime
////////////////////////////////////////////////////////////////
void Base::gettime() {
  if (Base::validateCreds()) {
    String currentTime = NTP.getTimeDateString();
    server.send(200,"text/plain","Current time is: "+currentTime+"\n");
    Serial.print(F("Current time is: "));
    Serial.println(currentTime);
    json["esp.CurrentTime"] = currentTime;
  }
}

////////////////////////////////////////////////////////////////
// led
//
////////////////////////////////////////////////////////////////
void Base::get_led(uint8_t pin, JsonObject& lightJson) {
   if (!server.authenticate(www_username, www_password)) {
         return server.requestAuthentication();
   }
   bool status = digitalRead(pin);
   Serial.println(status);
   String value = "off";
   if (!status) value = "on";
   lightJson["status"] = value;
   lightJson["name"] = pin;
   server.setContentLength(json.measurePrettyLength());
   server.send(200,"text/json","");
   WiFiClient deviceClient = server.client();
   lightJson.prettyPrintTo(deviceClient);
   lightJson.prettyPrintTo(Serial);
}	

String ledValue(uint8_t pin) {
   bool status = digitalRead(pin);
   String value = "off";
   if (!status) value = "on";
   Serial.println("ledValue = "+value);
   return(value);
}	

void Base::led2() {
   Base::get_led(pin_led2, lightJson);
}	   
void Base::led16() {
   Base::get_led(pin_led16, lightJson);
}	   
void Base::toggleLed2() {
   Serial.print("In toggleLed - pin:");
   Serial.println(2);	   
   digitalWrite(pin_led2,!digitalRead(pin_led2));
}	
void Base::toggleLed16() {
   Serial.print("In toggleLed - pin:");
   Serial.println(16);	   
   digitalWrite(pin_led16,!digitalRead(pin_led16));
}	

void set_pin(uint8_t pin, bool value) {
   digitalWrite(pin,value);
}	

////////////////////////////////////////////////////////////////
// led
////////////////////////////////////////////////////////////////
void Base::set_led(uint8_t pin, bool value) {
  if (Base::validateCreds()) {
    server.send(200,"text/plain","LED Activated...\n");
    Serial.println(F("LED Activated..."));
    set_pin(pin,value);
  }
}

////////////////////////////////////////////////////////////////
// ledOn
////////////////////////////////////////////////////////////////
void Base::allLedOn() {
  set_led(pin_led2,false);
  set_led(pin_led16,false);
}

////////////////////////////////////////////////////////////////
// ledOff
////////////////////////////////////////////////////////////////
void Base::allLedOff() {
  set_led(pin_led2, true);
  set_led(pin_led16, true);
}

////////////////////////////////////////////////////////////////
// help
////////////////////////////////////////////////////////////////
void Base::help(){
  if (Base::validateCreds()) {
    server.send(200,"text/plain",api);
    Serial.println(api);
  }
}

////////////////////////////////////////////////////////////////
// rssi
////////////////////////////////////////////////////////////////
void Base::rssi() {
  if (Base::validateCreds()) {
    long rssi_signal = WiFi.RSSI();
    server.send(200,"text/plain","Received Signal Strength Indicator :"+String(rssi_signal)+"\n");
  }  
}

////////////////////////////////////////////////////////////////
// getVersion
////////////////////////////////////////////////////////////////
void Base::getVersion(String ver) {
  if (Base::validateCreds()) {
    server.send(200, "text/plain", "Version: "+ver+"\n");
    sendUdpSyslog("ESP8266", "Version:", ver);
  }
}

////////////////////////////////////////////////////////////////
// sendVersion
////////////////////////////////////////////////////////////////
void Base::sendVersion() {
  Base::getVersion(SOFTWARE_VERSION);
}

////////////////////////////////////////////////////////////////
// bootTime
////////////////////////////////////////////////////////////////
void Base::bootTime() {
  if (Base::validateCreds()) {
    String boottime = json["esp.BootTime"];
    server.send(200, "text/plain", "Boot time: "+boottime+"\n");
  }
}

////////////////////////////////////////////////////////////////
// handleNotFound
////////////////////////////////////////////////////////////////
void Base::handleNotFound(){
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET)?"GET":"POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i=0; i<server.args(); i++){
     message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
				        }
  server.send(404, "text/plain", message);
}

////////////////////////////////////////////////////////////////
// setupWebServer
////////////////////////////////////////////////////////////////
void Base::setupWebServer() {
  Serial.println(F("Setting up Web server"));
  //server.on("/", std::bind(&Base::serverOk, this));
  server.on("/device", HTTP_GET, std::bind(&Base::device, this));
  server.on("/heap", HTTP_GET, std::bind(&Base::heap, this));
  server.on("/restart", HTTP_GET, std::bind(&Base::restartESP, this));
  server.on("/toggle", HTTP_GET, std::bind(&Base::toggle, this));
  //server.on("/allLedOff", HTTP_GET, std::bind(&Base::allLedOff, this));
  //server.on("/allLedOn", HTTP_GET, std::bind(&Base::allLedOn, this));
  server.on("/help", HTTP_GET, std::bind(&Base::help, this));
  server.on("/rssi", HTTP_GET, std::bind(&Base::rssi, this));
  server.on("/time", HTTP_GET, std::bind(&Base::gettime, this));
  server.on("/version", HTTP_GET, std::bind(&Base::sendVersion, this));
  server.on("/boottime", HTTP_GET, std::bind(&Base::bootTime, this));
  //server.on("/led2", HTTP_GET, std::bind(&Base::led2, this));
  server.on("/led16", HTTP_GET, std::bind(&Base::led16, this));
  //server.on("/toggleLed2", HTTP_GET, std::bind(&Base::toggleLed2, this));
  server.on("/toggleLed16", HTTP_GET, std::bind(&Base::toggleLed16, this));
  //server.onNotFound(std::bind(&Base::handleNotFound, this));
  server.begin();
  Serial.println(F("....Web server started"));
}

////////////////////////////////////////////////////////////////
// handleClient
////////////////////////////////////////////////////////////////
void Base::handleClient() {
  server.handleClient();
}

////////////////////////////////////////////////////////////////
// sendUdpSyslog
////////////////////////////////////////////////////////////////
void Base::sendUdpSyslog(String msg1, String msg2, String msg3) {
  Serial.println("In sendUdpSyslog");
  unsigned int msg1_len = msg1.length();
  byte* m1 = (byte*)malloc(msg1_len);
  memcpy(m1, (char*) msg1.c_str(), msg1_len);
		  
  unsigned int msg2_len = msg2.length();
  byte* m2 = (byte*)malloc(msg2_len);
  memcpy(m2, (char*) msg2.c_str(), msg2_len);

  unsigned int msg3_len = msg3.length();
  byte* m3 = (byte*)malloc(msg3_len);
  memcpy(m3, (char*) msg3.c_str(), msg3_len);

  udp.beginPacket(syslogServer, syslogPort);
  udp.write(st_devicename);
  udp.write(" ");
  udp.write(m1, msg1_len);  //msg1
  udp.write(" ");
  udp.write(m2, msg2_len);  //msg2
  udp.write(" ");
  udp.write(m3, msg3_len);  //msg3
  udp.endPacket();
  free(m1);
  free(m2);
  free(m3);
}

////////////////////////////////////////////////////////////////
// sendJSONToSyslog
////////////////////////////////////////////////////////////////
void Base::sendJSONToSyslog() {
  Serial.println(F("sending JSON to syslog..."));
  udp.beginPacket(syslogServer, syslogPort);
  udp.write(st_devicename);
  udp.write(" ");
  udp.write("ESP8266");
  udp.write(" ");
  json.printTo(udp);
  udp.endPacket();
}

void clear() {
  lcd.setCursor(0,0);
  lcd.print("               ");	  
  lcd.setCursor(0,1);
  lcd.print("               ");	  
}

void Base::printLCD () {
   clear();
   //temp();
   int espTemp = json.get<int>("esp.Temperature");
   int espHum = json.get<int>("esp.Humidity");
   int espRssi = json.get<int>("esp.Rssi");
   //Serial.println("T=" + String(espTemp) + " H=" + espHum + " R=" + espRssi);
   //Serial.println("T=" + String(json.get<int>("esp.temperature")) + " H=" + String(json.get<int>("esp.humidity")) + " R=" + String(json.get<int>("esp.rssi")));

   lcd.setCursor(0, 0);
   lcd.print("T=" + String(espTemp)+" ");
   lcd.print("H=" + String(espHum) + "% ");
   lcd.print("R=" + String(espRssi) + "%");

   lcd.setCursor(0, 1);
   lcd.print("L2=" + json.get<String>("esp.led2"));
   lcd.print(" L16=" + json.get<String>("esp.led16"));
}

void Base::jsonDoor(String state) {
  json["esp.Door"] = state;
}

////////////////////////////////////////////////////////////////
// device
////////////////////////////////////////////////////////////////
void Base::device() {

  if (!server.authenticate(www_username, www_password)) {
       return server.requestAuthentication();
  }
  Serial.println("getting updated device info");
  // update json dictionary with updated free heap and current signal strength
  json["esp.Freeheap"] = ESP.getFreeHeap();
  json["esp.Rssi"] = 2 * (WiFi.RSSI() + 100);
  //json["esp.led2"] = ledValue(pin_led2);
  json["esp.Led"] = ledValue(pin_led16);	  
  json["esp.CurrentTime"] = NTP.getTimeDateString();
  json["esp.UpTime"] = NTP.getUptimeString();
  temp();
  // return json to client per request
  server.setContentLength(json.measurePrettyLength());
  server.send(200,"text/json","");
  WiFiClient deviceClient = server.client();

  json.prettyPrintTo(deviceClient); 
  json.prettyPrintTo(Serial);
  Serial.println(" ");
  sendJSONToSyslog();
  Serial.println("finished device processing");
  //Base:printLCD();
}
