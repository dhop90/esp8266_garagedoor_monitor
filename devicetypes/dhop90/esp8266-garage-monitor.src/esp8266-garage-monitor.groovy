/**
 *  RPi Garage Monitor Device Type with Camera and Temperature Sensor
 *
 *  Copyright 2017 David Hopson <dhop90@gmail.com.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  
 *  Fork from:
 *  Modified from Richard L. Lynch RPI Garage Monitor
 *  Combined with http://thingsthataresmart.wiki/index.php?title=URI_Switch
 */

import groovy.json.JsonSlurper

metadata {
    definition (name: "ESP8266 Garage Monitor", namespace: "dhop90", author: "David Hopson") {
        capability "Door Control"
        capability "Polling"
        capability "Refresh"
        capability "Image Capture"
        capability "Contact Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        command "subscribe"
        command "toggle16"
        command "toggleLight"
        command "restart"
        command "loadpicture"
        command "clear"
    }

    simulator {
    }

    preferences {
      section("Nodemcu (ESP8266) Information"){
		input "control_ip", "text", title: "IP Address", required: true
		input "control_port", "text", title: "Port Number", required: true
        input "username", "string", title:"Username", description: "username", required: true, displayDuringSetup: true
        input "password", "password", title:"Password", description: "Password", required: true, displayDuringSetup: true
		input "on_path", "text", title: "On Path (/clk?id=left)", required: true
	  }
    }
    tiles {
        standardTile("image", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true) {
        	state "default", label: "", action: "", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
    	}

    	carouselTile("cameraDetails", "device.image", width: 3, height: 3) { }

        standardTile("take", "device.image", width: 1, height: 1, canChangeBackground: true) {
            state "taking", label:'${name}', icon:"st.camera.dropcam", nextState: "camera", backgroundColor: "#e59e10"
            state "camera", label:'${name}', action:"take", icon:"st.camera.dropcam", nextState: "taking", backgroundColor: "#ffffff", defaultState: true
    	}  
        standardTile("clear", "device.clear", width: 1, height: 1, canChangeBackground: true) {
            state "clearing", label:'${name}', icon:"st.camera.take-photo", nextState: "clear", backgroundColor: "#e59e10"
            state "clear", label:'${name}', action:"clear", icon:"st.camera.take-photo", nextState: "clearing", backgroundColor: "#ffffff", defaultState: true
    	}         
        standardTile("load", "device.load", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        	state "load", label: "Load", action: "loadpicture", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
        	state "loading", label:'Loading', action: "", icon: "st.camera.dropcam", backgroundColor: "#00A0DC"
        	state "image", label: "Load", action: "loadpicture", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
    	}     
        standardTile("doorControl", "device.doorControl", width: 1, height: 1, canChangeBackground: true) {
        	state "open", label:'${name}', action:"doorControl.close", icon:"st.doors.garage.garage-open", nextState: "closing", backgroundColor: "#b82121"
            state "closing", label:'${name}', icon:"st.doors.garage.garage-closing", nextState: "closed", backgroundColor: "#e59e10"
            state "closed", label:'${name}', action:"doorControl.open", icon:"st.doors.garage.garage-closed", nextState: "opening", backgroundColor: "#ffffff", defaultState: true
            state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#e59e10", nextState: "open", action: "doorControl.close"
    	}  
        standardTile("refresh", "device.refresh", width: 1, height: 1, canChangeBackground: true) {
        	state "Idle", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon", nextState: "Active", backgroundColor: "#ffffff", defaultState: true
            state "Active", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon", nextState: "Idle", backgroundColor: "#cccccc", defaultState: false
    	}            
        standardTile("restart", "device.restart", canChangeBackground: true) {
            state "Idle", label:'restart', action:"restart", backgroundColor: "#ffffff", nextState: "Active", defaultState:true, icon: "st.samsung.da.RC_ic_power"
            state "Active", label:'restart', backgroundColor: "#cccccc", nextState: "Idle", defaultState:false, icon: "st.samsung.da.RC_ic_power"
        } 
        valueTile("CurrentTime", "device.CurrentTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'currentTime\n${currentValue}', defaultState: true
        } 
        valueTile("Devicename", "device.Devicename", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "Devicename", label:'Devicename\n${currentValue}', defaultState: true
        }
        valueTile("IPaddress", "device.IPaddress", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'IPaddress\n${currentValue}', defaultState: true
        }  
        valueTile("MACaddress", "device.MACaddress", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'MACaddress\n${currentValue}', defaultState: true
        }  
        valueTile("UpTime", "device.UpTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'upTime\n${currentValue}', defaultState: true
        } 
        valueTile("BootTime", "device.BootTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'bootTime\n${currentValue}', defaultState: true
        }  
        valueTile("ChipId", "device.ChipId", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'ChipId\n${currentValue}', defaultState: true
        }  
        valueTile("FlashChipId", "device.FlashChipId", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'FlashChipId\n${currentValue}', defaultState: true
        }  
        valueTile("FlashChipSize", "device.FlashChipSize", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'FlashChipSize\n${currentValue}', defaultState: true
        } 
        valueTile("RealFlashChipSize", "device.RealFlashChipSize", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'RealFlashChipSize\n${currentValue}', defaultState: true
        }         
        valueTile("Freeheap", "device.Freeheap", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'Freeheap\n${currentValue}', defaultState: true
        }  
        valueTile("CoreVersion", "device.CoreVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'CoreVersion\n${currentValue}', defaultState: true
        }  
        valueTile("SdkVersion", "device.SdkVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SdkVersion\n${currentValue}', defaultState: true
        } 
        valueTile("CpuFreqMHz", "device.CpuFreqMHz", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'CpuFreqMHz\n${currentValue}', defaultState: true
        }        
        valueTile("SketchSize", "device.SketchSize", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchSize\n${currentValue}', defaultState: true
        }
        
        valueTile("TotalBytes", "device.TotalBytes", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'TotalBytes\n${currentValue}', defaultState: true
        }
        valueTile("UsedBytes", "device.UsedBytes", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'UsedBytes\n${currentValue}', defaultState: true
        }
        valueTile("RemainingBytes", "device.RemainingBytes", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'RemainingBytes\n${currentValue}', defaultState: true
        }
        
        
        valueTile("SdkVersion", "device.SdkVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SdkVersion\n${currentValue}', defaultState: true
        } 
        valueTile("SketchVersion", "device.SketchVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchVersion\n${currentValue}', defaultState: true
        }
        valueTile("SketchName", "device.SketchName", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchName\n${currentValue}', defaultState: true
        }
        valueTile("ResetReason", "device.ResetReason", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'ResetReason\n${currentValue}', defaultState: true
        }
        valueTile("FlashChipSpeed", "device.FlashChipSpeed", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'FlashChipSpeed\n${currentValue}', defaultState: true
        }   
        valueTile("Rssi", "device.Rssi", width: 1, height: 1, canChangeBackground: false) {
        	state("Rssi", label:'rssi\n${currentValue}%', 
            backgroundColors:[
                [value: 8, color: "#153591"],
                [value: 30, color: "#1e9cbb"],
                [value: 50, color: "#90d2a7"],
                [value: 90, color: "#44b621"],
            ])
    	}        
        valueTile("temperature",  "device.temperature",  width: 1, height: 1, canChangeBackground: true) {
        	state("temperature", label:'${currentValue}°', icon: "st.Weather.weather2", 
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ])
    	}        
        valueTile("humidity",  "device.humidity",  width: 1, height: 1, canChangeBackground: true) {
        	state("humidity", label:'humidity ${currentValue}%', 
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ])
    	}      
        standardTile("Led", "device.Led",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'LED', action: "toggle16", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'LED', action: "toggle16", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		} 
        standardTile("light", "device.light",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'Light', action: "toggleLight", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Light', action: "toggleLight", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		} 
        
        main "doorControl"
        details ([
        "doorControl", 
        "refresh",
        "Led",

        "temperature",
        "humidity",
        "Rssi",
        
        "restart",
        "take",
        "clear",
    //    "image",  
        
        "cameraDetails",
        
        "Devicename", 
        
        "IPaddress",
        "MACaddress",        
        "CurrentTime",
        "BootTime",
        "UpTime",
        "CpuFreqMHz",
        "Freeheap", 
        "ChipId",
        "FlashChipId",
        "FlashChipSize",
        "RealFlashChipSize",
        "FlashChipSpeed",
        "TotalBytes",
        "UsedBytes",
        "RemainingBytes",
        "CoreVersion",
        "SdkVersion",
        "SketchName",       
        "SketchVersion",
        "SketchSize",
        "ResetReason",
        
        ])
    }
}

def initialize() {
    log.debug "In initialize"
	runEvery1Minute(GetDevice)
    runEvery1Minute(refresh)
    runEvery1Minute(poll)
    zero()
}

def parse(String description) { 
    log.error "000000000000 In parse 0000000000000"
    def map = stringToMap(description)
    log.info "map = ${map}"
    
    def parsedEvent = parseDiscoveryMessage(description)
    if (parsedEvent['body']) {
        def Tempcmd = new String(parsedEvent['body'].decodeBase64())
        log.debug "Tempcmd = ${Tempcmd}"
        if ( Tempcmd == "Clear_Finished\n" ) {
            log.debug "Clear WORKED"
            sendEvent(name: "clear", isStateChange: "true", value: "clear")
        }
    }
    //process images
    if (map.tempImageKey) {
        log.error "received image"
        try {
            log.error "storing image"
            storeTemporaryImage(map.tempImageKey, getPictureName())
            sendEvent(name: "refresh", isStateChange: "true", value: "Idle", descriptionText: "Refresh set to Idle")
            sendEvent(name: "image", isStateChange: "true", value: map.tempImageKey)
            sendEvent(name: "take", isStateChange: "true", value: "camera", descriptionText: "finished taking photo")
        } catch (Exception e) {
            log.error e
        }
        //return
    } else if (map.error) {
        //log.error "Error: ${map.error}"
        log.error "Error: no image in map"
        sendEvent(name: "take", isStateChange: "true", value: "camera")
        sendEvent(name: "refresh", isStateChange: "true", value: "Idle", descriptionText: "Refresh set to Idle4")
        //return
    }
    
    def msg = parseLanMessage(description)   
        
    //process door state
    if (parsedEvent['body'] != null && parsedEvent['body'].size() > 4 && parsedEvent['body'].size() < 20) {
        def cmd = new String(parsedEvent['body'].decodeBase64())
        def size = parsedEvent['body'].size()    
        
        log.warn "------------ ${device} door is ${cmd} : size = ${size} ------------"
       
        sendEvent(name: "doorControl", value: cmd, isStateChange: true, descriptiontext: "door1 is ${cmd}")   
        sendEvent(name: "restart", isStateChange: "true", value: "Idle")
        sendEvent(name: "take", isStateChange: "true", value: "camera")

        //def evt1 = createEvent(name: "${device}.doorControl", value: cmd, isStateChange: true)
        //def evt2 = createEvent(name: "device.doorControl", value: cmd, isStateChange: true)
        //def evt3 = createEvent(name: "doorControl", value: cmd, isStateChange: true)
        //return [evt1, evt2, evt3]
        return
    } 
    
    //process device info
    if (parsedEvent['body'] != null && parsedEvent['body'].size() > 20) {
        log.warn "----------###### processing device response #######-----------"
                     
        def json = new groovy.json.JsonSlurper().parseText(msg.body)

        if (!json) {
          sendEvent(name: "refresh", isStateChange: "true", value: "Idle")
          return 
        }  
        
        json.keySet().each {
           def key = it.minus("esp.")
           log.info "${key} : ${json[it].toString()}"
           sendEvent(name: key, value: "${json[it].toString()}", isStateChange: "true")
        }
        
        //sendEvent(name: "refresh", isStateChange: "true", value: "Idle")
        //sendEvent(name: "restart", isStateChange: "true", value: "Idle")
    }   
    log.info "exiting parse"
    sendEvent(name: "refresh", isStateChange: "true", value: "Idle")
    //sendEvent(name: "restart", isStateChange: "true", value: "Idle")
    sendEvent(name: "take", isStateChange: "true", value: "camera")
}

def refresh() {
    log.debug "Executing 'refresh'"
    refresh_action() 
    GetDevice()
    take()
}

def poll() {
    log.debug "Executing 'poll' from ${device.deviceNetworkId} "
    subscribeAction(getDataValue("ssdpPath"))
    def path = getDataValue("ssdpPath")
    zero()
    sendHubCommand(setFeature("/device"))
    take()
}

def GetDevice() {
    sendHubCommand(setFeature("/device"))
    //refresh()
}    

def zero() {
   sendEvent(name: "Devicename", value: "",isStateChange: "true")
   sendEvent(name: "currentTime", value: "",isStateChange: "true")
   sendEvent(name: "upTime", value: "",isStateChange: "true")
   sendEvent(name: "IPaddress", value: "",isStateChange: "true")
   sendEvent(name: "MACaddress", value: "",isStateChange: "true")
   sendEvent(name: "bootTime", value: "",isStateChange: "true")
   sendEvent(name: "ChipId", value: "",isStateChange: "true")
   sendEvent(name: "FlashChipSize", value: "",isStateChange: "true")
   sendEvent(name: "RealFlashChipSize", value: "",isStateChange: "true")
   sendEvent(name: "Freeheap", value: "",isStateChange: "true")
   sendEvent(name: "FlashChipSpeed", value: "",isStateChange: "true")
   sendEvent(name: "CoreVersion", value: "",isStateChange: "true")
   sendEvent(name: "SdkVersion", value: "",isStateChange: "true")
   sendEvent(name: "CpuFreqMHz", value: "",isStateChange: "true")
   sendEvent(name: "SketchSize", value: "",isStateChange: "true")
   sendEvent(name: "SketchVersion", value: "",isStateChange: "true")
   sendEvent(name: "SketchName", value: "",isStateChange: "true")
   sendEvent(name: "rssi", value: "",isStateChange: "true")
   sendEvent(name: "temperature", value: "",isStateChange: "true")
   sendEvent(name: "humidity", value: "",isStateChange: "true")
   sendEvent(name: "led", value: "off",isStateChange: "true")
   sendEvent(name: "refresh", value: "idle",isStateChange: "true")
   sendEvent(name: "restart", value: "idle",isStateChange: "true")  
}

def clear() {
    log.debug "Clearing data storage"
    sendHubCommand(setFeature("/clear"))
}

def take() {
    takepicture()
    loadpicture()
}

def takepicture() {
    log.debug "In takepicture"
    sendHubCommand(setFeature("/submit"))
}
   
def loadpicture() {
    log.debug "In loadpicture"
    sendEvent(name: "take", isStateChange: "true", value: "taking")
    def userpass = encodeCredentials(username, password)
    def headers = getHeader(userpass)
    def path = "/pics/capture.jpg"

    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: path,
        headers: headers,
        dni
    )

    hubAction.options = [outputMsgToS3:true]
    
    log.debug "----- loadpicture from ${path} ----- "

    return hubAction
}

def open() {
//def on() {
	action()
}

def close() {
//def off() {
	action()
}

def setFeature(query) {
    def userpass = encodeCredentials(username, password)
    def headers = getHeader(userpass)
     
    def action = new physicalgraph.device.HubAction(
		method: "GET",
		path: query,
        headers: headers,
        dni
	)
    log.info "setFeature: ${query}"
    //log.info "setFeature query: ${query}\naction =\n${action}"
	return action
}

def refresh_action() {
    log.debug "******* in fresh_action routine *******"  
	sendHubCommand(setFeature("/status"))             
    //sendHubCommand(setFeature("/device")) 
}  

def action() {
    sendHubCommand(setFeature(on_path))
    take()
}  

def subscribe() {
    subscribeAction(getDataValue("ssdpPath"))
}

def Toggle(device) {
    log.info "Toggle: ${device}"
    sendHubCommand(setFeature(device))
}

def toggleLight() {
	Toggle("/light")
}

def toggle16() {
    Toggle("/toggleLed16")   
}

def toggle2() {
    Toggle("/toggleLed2")
}

def toggleAll() {
    Toggle("/toggle")
}    

def restart() {
    sendEvent(name: "restart", isStateChange: "true", value: "Active", descriptionText: "Restart was activated")
    Toggle("/restart")   
}    

private encodeCredentials(username, password){
    def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private getHeader(userpass){
    def headers = [:]
    headers.put("HOST", "${control_ip}:${control_port}")
    headers.put("Authorization", userpass)
    return headers
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def ip = control_ip
    def port = control_port
    log.debug "getHostAddress:ip = ${ip}, port = ${port}"

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    ip = convertHexToIP(ip)
    port = convertHexToInt(port)
    log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

private def parseDiscoveryMessage(String description) {
    def device = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
            //log.warn "devicetype = ${valueString}"
        } else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                //log.warn "mac = ${valueString}"
            }
        } else if (part.startsWith('ip:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                //log.warn "ip = ${valueString}"
            }    
        } else if (part.startsWith('port:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                //log.warn "port = ${valueString}"
            }     
        } else if (part.startsWith('requestId:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                //log.warn "requestId = ${valueString}"
            }     
        } else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ip = valueString
                //log.warn "networkAddress = ${valueString}"
            }
        } else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.port = valueString
                //log.warn "deviceAddress = ${valueString}"
            }
        } else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ssdpPath = valueString
                //log.warn "ssdpPath = ${valueString}"
            }
        } else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpUSN = valueString
                //log.warn "ssdpUSN = ${valueString}"
            }
        } else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpTerm = valueString
                //log.warn "ssdpTerm = ${valueString}"
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                device.headers = valueString
                def headers = new String(device.headers.decodeBase64())
                //log.warn "headers = ${device.headers}"
                log.warn 
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                device.body = valueString
                //log.warn "body = ${valueString}"
            }
        }
    }
    
    device
}

private subscribeAction(path, callbackPath="") {
    def address = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
    def parts = device.deviceNetworkId.split(":")

    def newIP = control_ip
    def newPort = control_port
    
    if (!newIP || !newPort) {
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    } else {
        def ip = control_ip
        def port = control_port
    	ip = ip + ":" + port
    	def result = new physicalgraph.device.HubAction(
        	method: "SUBSCRIBE",
        	path: path,
        	headers: [
            	HOST: ip,
            	CALLBACK: "<http://${address}/notify$callbackPath>",
            	NT: "upnp:event",
            	TIMEOUT: "Second-3600"])
    	result
    }
}

private getPictureName() {
    return java.util.UUID.randomUUID().toString().replaceAll('-', '')
}
