// Garagedoor controler and
// ESP8266-12E ArduCAM Mini Camera Server
// with Temperature sensor
//
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


#include <Base.h>
#include <ArduinoOTA.h>
#include <Ticker.h>
#include <FS.h> // FOR SPIFFS
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <Wire.h>
#include <ArduCAM.h>
#include <SPI.h>
#include "memorysaver.h"
#if !(defined ESP8266 )
#error Please select the ArduCAM ESP8266 UNO board in the Tools/Board
#endif

//This demo can only work on OV2640_MINI_2MP or ARDUCAM_SHIELD_V2 platform.
#if !(defined (OV2640_MINI_2MP)||(defined (ARDUCAM_SHIELD_V2) && defined (OV2640_CAM)))
#error Please select the hardware platform and camera module in the ../libraries/ArduCAM/memorysaver.h file
#endif

// set GPIO16 as the slave select :
const int CS = 16;
// Garage door settings
const PROGMEM int DEVICE_ACTIVATE_PIN = D8; //4; RELAY
const PROGMEM int LIGHT_ACTIVATE_PIN = 3; //opener light switch 
const PROGMEM int DEVICE_OPEN_SENSOR_PIN = D3; // 13 = D7 was D2
unsigned long deviceActivateStart;
int Status = -1;
bool SendUpdate = false;

// if the video is chopped or distored, try using a lower value for the buffer
// lower values will have fewer frames per second while streaming
static const size_t bufferSize = 4096; // 4096; //2048; //1024;

static const int fileSpaceOffset = 700000;
String currentState = "unknown";
static const int wifiType = 0; // 0:Station  1:AP
const String SOFTWARE_VERSION = "v1.133";
const PROGMEM uint8_t pin_led2 = 2; //on-board led
const PROGMEM uint8_t pin_led16 = 16; //on-board led
char* OTA_USERNAME = "pi" ;
// OTA and Wifi values.  Set for environment, included build script gets values from password store
char* OTA_PASSWORD = _OTA_PASSWORD_;
char* SSID = _SSID_;
char* WIFIPASSWORD = _WIFIPASSWORD_;
const IPAddress updateServer(192,168,86,53);
const PROGMEM int updatePort = 39500;
unsigned long timeMills;
Ticker ticker;
Base base;
const String fName = "res.txt";

int fileTotalKB = 0;
int fileUsedKB = 0; int fileCount = 1;
String errMsg = "";
int imgMode = 1; // 0: stream  1: capture
int resolution = 8;
// resolutions:
// 0 = 160x120
// 1 = 176x144
// 2 = 320x240
// 3 = 352x288
// 4 = 640x480
// 5 = 800x600
// 6 = 1024x768
// 7 = 1280x1024
// 8 = 1600x1200

ESP8266WebServer server(80);

ArduCAM myCAM(OV2640, CS);

////////////////////////////////////////////////////////////////
// deviceStatus
////////////////////////////////////////////////////////////////
String deviceStatus(int OpenSensor) {
  //Serial.println("in deviceStatus");  
  String state = "AJAR";

  if (digitalRead(OpenSensor) == HIGH)
     state = "open";
  else
     state = "closed";

  if (currentState != state) {
     base.sendUdpSyslog(state, "Door transitioned from:", currentState);
  }
  currentState = state;
  return state;
}

////////////////////////////////////////////////////////////////
// getDeviceStatus
////////////////////////////////////////////////////////////////
void getDeviceStatus() {
  if (base.validateCreds())
     base.server.send(200, "text/plain", deviceStatus(DEVICE_OPEN_SENSOR_PIN));
}

////////////////////////////////////////////////////////////////
// transition
////////////////////////////////////////////////////////////////
String transition(String cstate) {
  if (cstate = "closed")
     return "opening";
  if (cstate = "open")
     return "closing";
  return "unknown";
}

////////////////////////////////////////////////////////////////
// activateDevice
////////////////////////////////////////////////////////////////
void activateDevice() {
  if (!base.validateCreds())
         return;
  else {
     Serial.println("In activateDevice");  
     deviceActivateStart = millis();

     digitalWrite(DEVICE_ACTIVATE_PIN, HIGH);
     delay(1000);
     base.server.send(200, "text/plain", "OK\n");

     digitalWrite(DEVICE_ACTIVATE_PIN, LOW);
     delay(1000);

     String device_status = deviceStatus(DEVICE_OPEN_SENSOR_PIN);

     base.server.send(200, "text/plain", transition(currentState));
     base.sendUdpSyslog(device_status, "Activated - current state:", device_status);
  }
  //SendUpdate = true;
}
////////////////////////////////////////////////////////////////
// activateLight
////////////////////////////////////////////////////////////////
void activateLight() {
  if (!base.validateCreds())
         return;
  else {
     Serial.println("In activateLight");
     deviceActivateStart = millis();

     digitalWrite(LIGHT_ACTIVATE_PIN, HIGH);
     delay(1000);
     base.server.send(200, "text/plain", "OK\n");

     digitalWrite(LIGHT_ACTIVATE_PIN, LOW);
     delay(1000);

     //String device_status = deviceStatus(DEVICE_OPEN_SENSOR_PIN);

     base.server.send(200, "text/plain", "activated");
     //base.sendUdpSyslog(device_status, "Activated - current state:", device_status);
  }
  //SendUpdate = true;
}


////////////////////////////////////////////////////////////////
// CheckDoorStatus
////////////////////////////////////////////////////////////////
void CheckDoorStatus()
{
  int newStatus = digitalRead(DEVICE_OPEN_SENSOR_PIN);
  if (newStatus == Status)
  {
    //new status is the same as the current status, return
    return;
  }
  else
  {
    Status = newStatus;
    SendUpdate = true;
  }
  //base.printLCD();
}

////////////////////////////////////////////////////////////////
// SendStatusUpdate
////////////////////////////////////////////////////////////////
void SendStatusUpdate()
{
  // Connect to the homebridge server
  //client.flush();
  //client.stop();
  if (base.client.connect(updateServer, updatePort))
  {
    Serial.println(F("--------------------------"));
    Serial.println(F("connected to update server"));
    String message = "";
    Serial.print(F("Status = "));
    Serial.println(Status);
    if (Status == 0)
    {
       message = "closed";
       Serial.println(F("Door is closed"));
       base.jsonDoor("closed");
    }
    else if (Status == 1)
    {
      message = "open";
      Serial.println(F("Door is open"));
      base.jsonDoor("open");
    }

    base.client.println(F("POST / HTTP/1.1"));
    base.client.print(F("HOST: "));
    base.client.print(updateServer);
    base.client.print(F(":"));
    base.client.println(updatePort);
    base.client.println("Connection: close");
    base.client.println(F("CONTENT-TYPE: text/html"));
    base.client.print(F("CONTENT-LENGTH: "));
    base.client.println(message.length());
    base.client.println();
    base.client.println(message);

    while (base.client.connected() && !base.client.available()) delay(1); //waits for data
    while (base.client.connected() || base.client.available()) { //connected or data available
      char c = base.client.read();
      Serial.print(c);
    }

    Serial.println();
    Serial.println(F("disconnecting."));
    Serial.println(F("=================="));
    Serial.println();
    base.client.flush();
    base.client.stop();
  }
  else
  {
    Serial.println("connection to update server failed");
    Serial.println();
  }
}

// setupTicker
////////////////////////////////////////////////////////////////
void setupTicker() {
  ticker.attach(5, CheckDoorStatus);
}

/////////////////////////////////////////////////
//   Updates Properties file with resolution  ///
/////////////////////////////////////////////////
void updateDataFile()
{

  File f = SPIFFS.open(fName, "w");
  if (!f) {
    Serial.println("prop file open failed");
  }
  else
  {
    Serial.println("====== Writing to prop file =========");

    f.println(resolution);
    Serial.println("Data file updated");
    f.close();
  }

}

///////////////////////////////////////////
//    Saves captured image to memory     //
///////////////////////////////////////////
void myCAMSaveToSPIFFS() {

  // as file space is used, capturing images will get slower. At a certain point, the images will become distored
  // or they will not save at all due to lack of space. To avoid this we set a limit and allow some free space to remain
  if ((fileTotalKB - fileUsedKB) < fileSpaceOffset)
  {
    String maxStr = "====== Maximum Data Storage Reached =========";
    Serial.println(maxStr);
    errMsg = maxStr;
    return;
  }

  String str;
  byte buf[256];
  static int i = 0;

  static int n = 0;
  uint8_t temp, temp_last;

  //  File file;
  //Flush the FIFO
  myCAM.flush_fifo();
  //Clear the capture done flag
  myCAM.clear_fifo_flag();
  //Start capture
  Serial.println("Starting File Capture!");
  delay(12500);
  myCAM.start_capture();

  while (!myCAM.get_bit(ARDUCHIP_TRIG , CAP_DONE_MASK));
  Serial.println("File Capture Done!");

  //fileCount++;

  //str = "/pics/" + String(fileCount)  + ".jpg";
  str = "/pics/capture.jpg";

  File f = SPIFFS.open(str, "w");
  Serial.print("Opening file to write:");
  if (!f) {
    Serial.println("prop file open failed");
  }
  else
  {
    Serial.println(str);
  }


  i = 0;
  myCAM.CS_LOW();
  myCAM.set_fifo_burst();
#if !(defined (ARDUCAM_SHIELD_V2) && defined (OV2640_CAM))
  SPI.transfer(0xFF);
#endif
  //Read JPEG data from FIFO
  while ( (temp != 0xD9) | (temp_last != 0xFF)) {
    temp_last = temp;
    temp = SPI.transfer(0x00);

    //Write image data to buffer if not full
    if ( i < 256)
      buf[i++] = temp;
    else {
      //Write 256 bytes image data to file
      myCAM.CS_HIGH();
      f.write(buf , 256);
      i = 0;
      buf[i++] = temp;
      myCAM.CS_LOW();
      myCAM.set_fifo_burst();
    }
    //delay(0);
  }

  //Write the remain bytes in the buffer
  if (i > 0) {
    myCAM.CS_HIGH();
    f.write(buf, i);
  }
  //Close the file
  f.close();
  Serial.println("CAM Save Done!");
  Serial.println("###########################################");
}


//////////////////////////////////////////////////////////////////////////////////////////////////
//    Retrieves a stored jpg file and sends to browser based on url: http://IPaddress/1.jpg     //
//////////////////////////////////////////////////////////////////////////////////////////////////
bool loadFromSpiffs(String path) {
  Serial.println("In loadFromSpiffs");	
  String dataType = "text/plain";
  if (path.endsWith("/")) path += "index.htm";

  if (path.endsWith(".src")) path = path.substring(0, path.lastIndexOf("."));
  //  else if (path.endsWith(".htm")) dataType = "text/html";
  //  else if (path.endsWith(".css")) dataType = "text/css";
  //  else if (path.endsWith(".js")) dataType = "application/javascript";
  //  else if (path.endsWith(".png")) dataType = "image/png";
  //  else if (path.endsWith(".gif")) dataType = "image/gif";
  else if (path.endsWith(".jpg")) dataType = "image/jpeg";
  //  else if (path.endsWith(".ico")) dataType = "image/x-icon";
  //  else if (path.endsWith(".xml")) dataType = "text/xml";
  //  else if (path.endsWith(".pdf")) dataType = "application/pdf";
  //  else if (path.endsWith(".zip")) dataType = "application/zip";
  Serial.println("opening file");
  File dataFile = SPIFFS.open(path.c_str(), "r");
  if (base.server.hasArg("download")) dataType = "application/octet-stream";

  if (base.server.streamFile(dataFile, dataType) != dataFile.size()) {
  }
  Serial.println("closing file");
  dataFile.close();
  return true;
}

/////////////////////////////////////////////////////////////
//  sets the HTML used for the resoultions drop down       //
/////////////////////////////////////////////////////////////
String getDropDown()
{
  String webString = "";
  webString += "<select name=\"rez\">\n";
  webString += "   <option value=\"0\" ";
  if (resolution == 0)
    webString += " selected=\"seleted\" ";
  webString += ">160x120</option>\n";

  webString += "   <option value=\"1\" ";
  if (resolution == 1)
    webString += " selected=\"seleted\" ";
  webString += ">176x144</option>\n";

  webString += "   <option value=\"2\" ";
  if (resolution == 2)
    webString += " selected=\"seleted\" ";
  webString += ">320x240</option>\n";

  webString += "   <option value=\"3\" ";
  if (resolution == 3)
    webString += " selected=\"seleted\" ";
  webString += ">352x288</option>\n";

  webString += "   <option value=\"4\" ";
  if (resolution == 4)
    webString += " selected=\"seleted\" ";
  webString += ">640x480</option>\n";

  webString += "   <option value=\"5\" ";
  if (resolution == 5)
    webString += " selected=\"seleted\" ";
  webString += ">800x600</option>\n";

  webString += "   <option value=\"6\" ";
  if (resolution == 6)
    webString += " selected=\"seleted\" ";
  webString += ">1024x768</option>\n";

  webString += "   <option value=\"7\" ";
  if (resolution == 7)
    webString += " selected=\"seleted\" ";
  webString += ">1280x1024</option>\n";

  webString += "   <option value=\"8\" ";
  if (resolution == 8)
    webString += " selected=\"seleted\" ";
  webString += ">1600x1200</option>\n";

  webString += "  </select>\n";

  return webString;
}


////////////////////////////////////
// capture initialization         //
////////////////////////////////////
void start_capture() {
  myCAM.clear_fifo_flag();
  myCAM.start_capture();
}

/////////////////////////////////////////////
// capture still image and send to client  //
/////////////////////////////////////////////
void camCapture(ArduCAM myCAM) {

  WiFiClient client = base.server.client();

  size_t len = myCAM.read_fifo_length();
  if (len >= 0x07ffff) {
    Serial.println("Over size.");
    return;
  } else if (len == 0 ) {
    Serial.println("Size is 0.");
    return;
  }

  myCAM.CS_LOW();
  myCAM.set_fifo_burst();
#if !(defined (ARDUCAM_SHIELD_V2) && defined (OV2640_CAM))
  SPI.transfer(0xFF);
#endif

  if (!client.connected()) {
	 Serial.println("client not connected");
	 return;
  }
  String response = "HTTP/1.1 200 OK\r\n";
  response += "Content-Type: image/jpeg\r\n";
  response += "Content-Length: " + String(len) + "\r\n\r\n";
  server.sendContent(response);

  static uint8_t buffer[bufferSize] = {0xFF};

  while (len) {
    size_t will_copy = (len < bufferSize) ? len : bufferSize;
    SPI.transferBytes(&buffer[0], &buffer[0], will_copy);
    if (!client.connected()) break;
    client.write(&buffer[0], will_copy);
    len -= will_copy;
  }

  myCAM.CS_HIGH();
}


/////////////////////////////////////////////
// initiate capture and record time used   //
/////////////////////////////////////////////
void serverCapture() {
    Serial.println("In serverCapture");
    if (!base.validateCreds()) {
       Serial.println("Creds failed, returning");
       return;
    }

    start_capture();
    Serial.println("CAM Capturing");

    int total_time = 0;

    total_time = millis();

    while (!myCAM.get_bit(ARDUCHIP_TRIG, CAP_DONE_MASK));

    total_time = millis() - total_time;
    Serial.print("capture total_time used (in miliseconds):");
    Serial.println(total_time, DEC);

    total_time = 0;

    Serial.println("CAM Capture Done!");
    total_time = millis();
    camCapture(myCAM);
    total_time = millis() - total_time;
    Serial.print("send total_time used (in miliseconds):");
    Serial.println(total_time, DEC);
    Serial.println("CAM send Done!");
 
}

/////////////////////////////
// stream video to client  //
/////////////////////////////
void serverStream() {
  if (!base.validateCreds()) {	
    return;
  }  
    WiFiClient client = base.server.client();

    String response = "HTTP/1.1 200 OK\r\n";
    response += "Content-Type: multipart/x-mixed-replace; boundary=frame\r\n\r\n";
    base.server.sendContent(response);

    while (client.connected()) {

      start_capture();

      while (!myCAM.get_bit(ARDUCHIP_TRIG, CAP_DONE_MASK));

      size_t len = myCAM.read_fifo_length();
      if (len >= 0x07ffff) {
        Serial.println("Over size.");
        continue;
      } else if (len == 0 ) {
        Serial.println("Size is 0.");
        continue;
      }

      myCAM.CS_LOW();
      myCAM.set_fifo_burst();
#if !(defined (ARDUCAM_SHIELD_V2) && defined (OV2640_CAM))
    SPI.transfer(0xFF);
#endif
      if (!client.connected()) break;
      response = "--frame\r\n";
      response += "Content-Type: image/jpeg\r\n\r\n";
      base.server.sendContent(response);

      static uint8_t buffer[bufferSize] = {0xFF};

      while (len) {
        size_t will_copy = (len < bufferSize) ? len : bufferSize;
        SPI.transferBytes(&buffer[0], &buffer[0], will_copy);
        if (!client.connected()) break;
        client.write(&buffer[0], will_copy);
        len -= will_copy;
      }
      myCAM.CS_HIGH();

      if (!client.connected()) break;
    }
}

////////////////////////////////////////////////////////////////
//  Default web handler used when client access web server    //
////////////////////////////////////////////////////////////////
void handleNotFound() {
  if (!base.validateCreds()) {
    return;
  }  
  Serial.println("----- HandleNotFound -----");
  String ipStr = WiFi.localIP().toString();
  if (wifiType == 1) // AP mode = 1
  {
    ipStr = WiFi.softAPIP().toString();
  }


  Serial.print("server uri: " ); Serial.println(base.server.uri());
  // if url contains request for stored image: http://IPaddress/pics/capture.jpg
  if (base.server.uri().indexOf(".jpg") != -1 )
  {
    loadFromSpiffs(base.server.uri());
    return;
  }

  //// default HTML
  String message = "<html><head>\n";
  message += "</head><body>\n";
  message += "<form action=\"http://" + ipStr + "/submit\" method=\"POST\">";
  message += "<h1>ESP8266-12E Arducam Mini</h1>\n";
  if (errMsg != "")
  message += "<div style=\"color:red\">" + errMsg + "</div>";


  if (imgMode == 0) // stream mode
  {
    message += "<div><h2>Video Streaming</h2></div> \n";
    message += "<div><img id=\"ArduCam\" src=\"http://" + ipStr + "/stream\" ></div>\n";
    imgMode = 1; // set back to capture mode so it doesn't get stuck in streaming mode

  }
  else
  {
    message += "<div>";
    message += getDropDown();

    message += " <input type=\"radio\" id=\"strm\" name=\"imgMode\" value=\"stream\" ";

    if (imgMode == 0)
      message += " checked ";
    message += "> Stream \n";

    message += " <input type=\"radio\" id=\"capt\" name=\"imgMode\" value=\"capture\"  ";
    if (imgMode == 1)
      message += " checked ";
    message += "> Capture \n";

    message += "&nbsp; <input type='submit' value='Submit'  >\n";
    message += " &nbsp;  <a style=\"font-size:12px; font-weight:bold;\" href=\"http://" + ipStr + "\">Refresh</a>";
    message += " &nbsp; &nbsp; <a style=\"font-size:12px; font-weight:bold;\" onclick=\"return confirm('Are you sure? This will delete all stored images.')\" ";
    message += " href=\"http://" + ipStr + "/clear\">Clear Data</a>\n";

    message += "</div>\n";


    ///////////////////////////////
    FSInfo fs_info;
    SPIFFS.info(fs_info);

    fileTotalKB = (int)fs_info.totalBytes;
    fileUsedKB = (int)fs_info.usedBytes;


    if (fileCount > 0)
    {
      int percentUsed = ((float)fileUsedKB / (float)(fileTotalKB - fileSpaceOffset)) * 100;
      String colorStr = "green";
      if (percentUsed > 90)
        colorStr = "red";

      message += "<div style=\"width:450px; background-color:darkgray; padding:1px;\">";
      message += "<div style=\"position:absolute; color:white; padding-top:2px; font-size:11px;\"> &nbsp; space used: " + String(percentUsed) + "%</div>";
      message += "<div style=\"width:" + String(percentUsed) + "%; height:16px; background-color: " + colorStr + ";\"></div></div>\n";

    }

    message += "<table><tr>";
    int colCnt = 0;
    for (int i = 1; i <= fileCount; i++)
    {
      //message += "<td><a href=\"/pics/" + String(i) + ".jpg\">" + i + ".jpg</a></td>\n";
      message += "<td><a href=\"/pics/" + String("capture") + ".jpg\">" + String("capture") + ".jpg</a></td>\n";
      //message += "<td><a href=\"/pics/" + String("capture") + ".jpg\">" + "</a></td>\n";

      colCnt++;
      if (colCnt >= 10) //  columns
      {
        message += "</tr><tr>";
        colCnt = 0;
      }
    }
    message += "</tr></table>\n";




      //useful for debugging max data storage
    message += "<table><tr><td>Total Bytes: </td><td style=\"text-align:right;\">";
    message += fileTotalKB;
    message += "</td></tr><tr><td>Used Bytes: </td><td style=\"text-align:right;\">";
    message += fileUsedKB;
    message += "</td></tr><tr><td>Remaing Bytes: </td><td style=\"text-align:right;\">";
    message += (fileTotalKB - fileUsedKB);
    message += "</td></tr></table>\n";
    float flashChipSize = (float)ESP.getFlashChipSize() / 1024.0 / 1024.0;
    message += "<br>chipSize: ";
    message += flashChipSize;

    message += "<div><img id=\"ArduCam\" src=\"http://" + ipStr + "/capture\" ></div>\n";


  }

  message += "</form> \n";
  message += "</body></html>\n";

  base.server.send(200, "text/html", message);
  
}

///////////////////////////////////////////////////////////////////////////////////
//    used when form is submitted and at setup to set the camera resolution      //
///////////////////////////////////////////////////////////////////////////////////
void setCamResolution(int reso)
{
  switch (reso)
  {
    case 0:
      myCAM.OV2640_set_JPEG_size(OV2640_160x120);
      resolution = 0;
      break;

    case 1:
      myCAM.OV2640_set_JPEG_size(OV2640_176x144);
      resolution = 1;
      break;

    case 2:
      myCAM.OV2640_set_JPEG_size(OV2640_320x240);
      resolution = 2;
      break;

    case 3:
      myCAM.OV2640_set_JPEG_size(OV2640_352x288);
      resolution = 3;
      break;

    case 4:
      myCAM.OV2640_set_JPEG_size(OV2640_640x480);
      resolution = 4;
      break;

    case 5:
      myCAM.OV2640_set_JPEG_size(OV2640_800x600);
      resolution = 5;
      break;

    case 6:
      myCAM.OV2640_set_JPEG_size(OV2640_1024x768);
      resolution = 6;
      break;

    case 7:
      myCAM.OV2640_set_JPEG_size(OV2640_1280x1024);
      resolution = 7;
      break;

    case 8:
      myCAM.OV2640_set_JPEG_size(OV2640_1600x1200);
      resolution = 8;
      break;

  }
}

///////////////////////////////////////////////////////
//   deletes all files in the /pics directory        //
///////////////////////////////////////////////////////
void clearData()
{
  //if (!base.validateCreds()) {
  //   return;
  //}   
    errMsg = "======  Data Storage Cleared =========";
    Dir dir = SPIFFS.openDir("/pics");
    while (dir.next()) {
      SPIFFS.remove(dir.fileName());
    }

    fileCount = 0;
    fileTotalKB = 0;
    fileUsedKB = 0;

    //handleNotFound();  
}


/////////////////////////////////////
//  handles form submission        //
/////////////////////////////////////
void handleSubmit()
{
  Serial.println( "--- Form Submited ---");
  if (!base.validateCreds()) {
    Serial.println("Creds failed, returning");  
    return;
  }  
    errMsg = "";
    Serial.println( "Server args " + base.server.args());

    if (base.server.args() > 0 ) {
      for ( uint8_t i = 0; i < base.server.args(); i++ ) {

        // can be useful to determine the values from a form post.
        Serial.println( "Server arg " + base.server.arg(i));
        Serial.println( "Server argName " + base.server.argName(i) );
        if (base.server.argName(i) == "imgMode")
        {
          Serial.println(" Image Mode: " + base.server.arg(i));

          if (base.server.arg(i) == "stream")
          {
            imgMode = 0;
          }
          else
          {
            imgMode = 1; // capture mode
          }
        }

        if (base.server.argName(i) == "rez") {

          if (base.server.arg(i).toInt() != resolution)
          {

            setCamResolution(base.server.arg(i).toInt());

            updateDataFile();

            //// IMPORTANT: removing or reducing the delay can result in dark images
            delay(1500); // add a delay to allow the white balance to adjust to new resolution
          }
        }
      }
      
    }

    if (imgMode == 1) // capture
    {
      Serial.println("call myCAMSaveToSPIFFS");
      myCAMSaveToSPIFFS();
    }

    Serial.println( "--- Form Submited Finished ---");
    //handleNotFound();
    
}


void setupLocalWebServer() {
  Serial.println(F("Setting up local Web server"));
  base.server.on("/status", HTTP_GET, getDeviceStatus);
  base.server.on("/activate", HTTP_GET, activateDevice);
  //base.server.on("/light", HTTP_GET, activateLight);
  base.server.on("/clk", activateDevice);
  base.server.on("/capture", HTTP_GET, serverCapture);
 //base.server.on("/stream", HTTP_GET, serverStream);
  base.server.on("/submit", handleSubmit);
  base.server.on("/clear", clearData);
  base.server.on("/status", HTTP_GET, getDeviceStatus);
  base.server.onNotFound(handleNotFound);
  base.api = base.api+"/status      : Returns status of garage door\n"
                      "/activate    : Triggers garage door\n"
                      "/clk         : Triggers garage door\n"
		      "/capture     : take snapshot\n"
		      "/stream	    : stream video\n"
		      "/submit      : submit\n"
		      "/clear       : clear\n";
  base.server.begin();
  Serial.println(F("....Web server started"));
}



void setupSPIFFS() {
  Serial.println("Setting up SPIFFS");
  SPIFFS.begin();
  delay(10);

  //SPIFFS.format(); // uncomment to completely clear data including properties file

  // check for properties file
  File f = SPIFFS.open(fName, "r");

  if (!f) {
    // no file exists so lets format and create a properties file
    Serial.println("Please wait 30 secs for SPIFFS to be formatted");

    SPIFFS.format();

    Serial.println("Spiffs formatted");

    f = SPIFFS.open(fName, "w");
    if (!f) {
      Serial.println("properties file open failed");
    }
    else
    {
      // write the defaults to the properties file
      Serial.println("====== Writing to properties file =========");

      f.println(resolution);

      f.close();
    }

  }
  else
  {
    // if the properties file exists on startup,  read it and set the defaults
    Serial.println("Properties file exists. Reading.");

    while (f.available()) {

      // read line by line from the file
      String str = f.readStringUntil('\n');

      Serial.println(str);

      resolution = str.toInt();

    }

    f.close();
  }

#if defined(__SAM3X8E__)
  Wire1.begin();
#else
  Wire.begin();
#endif
  Serial.println("....PIFFS Ready");
}


void checkCAM() {
   //Check if the ArduCAM SPI bus is OK
  Serial.println("Setting up CAM");
  uint8_t temp; 
  uint8_t vid, pid;
  myCAM.write_reg(ARDUCHIP_TEST1, 0x55);
  temp = myCAM.read_reg(ARDUCHIP_TEST1);
  if (temp != 0x55) {
    Serial.println("SPI1 interface Error!");
  }

  //Check if the camera module type is OV2640
  myCAM.wrSensorReg8_8(0xff, 0x01);
  myCAM.rdSensorReg8_8(OV2640_CHIPID_HIGH, &vid);
  myCAM.rdSensorReg8_8(OV2640_CHIPID_LOW, &pid);
  if ((vid != 0x26 ) && (( pid != 0x41 ) || ( pid != 0x42 )))
    Serial.println("Can't find OV2640 module! pid: " + String(pid));
  else
    Serial.println("OV2640 detected.");


  //Change to JPEG capture mode and initialize the OV2640 module
  myCAM.set_format(JPEG);
  myCAM.InitCAM();

  setCamResolution(resolution);

  myCAM.clear_fifo_flag();
  Serial.println("....CAM ready");
}
////////////////////////////////
//    main setup function     //
////////////////////////////////

void setup() {
  Serial.begin(115200);
  Serial.println("ArduCAM Start!");
  Serial.println(F("\nBooting........."));
  pinMode(DEVICE_ACTIVATE_PIN, OUTPUT);
  pinMode(LIGHT_ACTIVATE_PIN, OUTPUT);
  pinMode(DEVICE_OPEN_SENSOR_PIN, INPUT_PULLUP);
  digitalWrite(DEVICE_ACTIVATE_PIN, LOW);
  digitalWrite(LIGHT_ACTIVATE_PIN, LOW);
  //pinMode(DEVICE_ACTIVATE_PIN, OUTPUT);
  //pinMode(LIGHT_ACTIVATE_PIN, OUTPUT);
  setupSPIFFS();
  // set the CS as an output:
  pinMode(CS, OUTPUT);
  // initialize SPI:
  SPI.begin();
  SPI.setFrequency(4000000); //4MHz
  checkCAM();
 
  base.setupVersion(SOFTWARE_VERSION);
  base.setupPins(pin_led2, pin_led16);
  base.setDevicename();
  base.setupWifi(SSID,WIFIPASSWORD);
  base.setupMDNS();
  base.setupOTA();
  base.setupNTP();
  base.setupApi();
  base.setupWebServer();
  setupLocalWebServer();
  setupTicker();  
  timeMills = millis();
  base.setupCreds(OTA_USERNAME,OTA_PASSWORD);
  base.setupJSON();
  base.sendToSyslog();
  base.device();

  Dir dir = SPIFFS.openDir("/pics");
  while (dir.next()) {
    fileCount++;
  }

  FSInfo fs_info;
  SPIFFS.info(fs_info);

  fileTotalKB = (int)fs_info.totalBytes;
  fileUsedKB = (int)fs_info.usedBytes;
  Serial.println(F("Ready......."));
}

/////////////////////////////
//    Main loop function   //
/////////////////////////////
void loop() {
  if (SendUpdate)
  {
     Serial.println("calling SendStatusUpdate");
     SendStatusUpdate();
     SendUpdate = false;
  }
  base.server.handleClient();
  ArduinoOTA.handle();
}

