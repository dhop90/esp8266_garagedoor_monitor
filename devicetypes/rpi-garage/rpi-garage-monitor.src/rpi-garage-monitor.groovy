/**
 *  RPi Garage Monitor Device Type
 *
 *  Copyright 2015 Richard L. Lynch <rich@richlynch.com>
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
    definition (name: "RPi Garage Monitor", namespace: "rpi_garage", author: "David Hopson") {
        capability "Door Control"
        capability "Polling"
        capability "Refresh"
        capability "Image Capture"
        command "subscribe"
        command "toggle16"
        command "toggleLight"
        command "restart"
        command "take"
        command "loadpicture"
    }

    simulator {
    }

    preferences {
      section("RPi Information"){
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
       
    	standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        	state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
        	state "taking", label:'Taking', action: "", icon: "st.camera.dropcam", backgroundColor: "#00A0DC"
        	state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
    	}
        
        standardTile("load", "device.load", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        	state "load", label: "Load", action: "loadpicture", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
        	state "loading", label:'Loading', action: "", icon: "st.camera.dropcam", backgroundColor: "#00A0DC"
        	state "image", label: "Load", action: "loadpicture", icon: "st.camera.dropcam", backgroundColor: "#FFFFFF", nextState:"taking"
    	}
        
        standardTile("esp.Door", "device.esp.Door", width: 1, height: 1, canChangeIcon: false ) {
            state "open", label: '${name}', action: "close", icon: "st.doors.garage.garage-open", backgroundColor: "#b82121", nextState: "closing"
            state "closing", label: '${name}', icon: "st.doors.garage.garage-closing", backgroundColor: "#e59e10", nextState: "closed"
            state "closed", label: '${name}', action: "open", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff", nextState: "opening"
            state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#e59e10", nextState: "open"
        }        
        standardTile("refresh", "device.refresh", width: 1, height: 1, canChangeBackground: true) {
        	state "Idle", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon", nextState: "Active", backgroundColor: "#ffffff", defaultState: true
            state "Active", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon", nextState: "Idle", backgroundColor: "#cccccc", defaultState: false
    	}            
        standardTile("restart", "device.switch", canChangeBackground: true) {
            state "Idle", label:'restart', action:"restart", backgroundColor: "#ffffff", nextState: "Active", defaultState:true, icon: "st.samsung.da.RC_ic_power"
            state "Active", label:'restart', action:"restart", backgroundColor: "#cccccc", nextState: "Idle", defaultState:false, icon: "st.samsung.da.RC_ic_power"
        } 
        valueTile("esp.currentTime", "device.esp.CurrentTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'currentTime\n${currentValue}', defaultState: true
        } 
        valueTile("esp.Devicename", "device.esp.Devicename", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'Devicename\n${currentValue}', defaultState: true
        }
        valueTile("esp.IPaddress", "device.esp.IPaddress", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'IPaddress\n${currentValue}', defaultState: true
        }  
        valueTile("esp.MACaddress", "device.esp.MACaddress", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'MACaddress\n${currentValue}', defaultState: true
        }  
        valueTile("esp.upTime", "device.esp.UpTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'upTime\n${currentValue}', defaultState: true
        } 
        valueTile("esp.bootTime", "device.esp.BootTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'bootTime\n${currentValue}', defaultState: true
        }  
        valueTile("esp.ChipId", "device.esp.ChipId", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'ChipId\n${currentValue}', defaultState: true
        }  
        valueTile("esp.FlashChipId", "device.esp.FlashChipId", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'FlashChipId\n${currentValue}', defaultState: true
        }  
        valueTile("esp.FlashChipSize", "device.esp.FlashChipSize", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'FlashChipSize\n${currentValue}', defaultState: true
        } 
        valueTile("esp.RealFlashChipSize", "device.esp.RealFlashChipSize", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'RealFlashChipSize\n${currentValue}', defaultState: true
        }         
        valueTile("esp.Freeheap", "device.esp.Freeheap", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'Freeheap\n${currentValue}', defaultState: true
        }  
        valueTile("esp.CoreVersion", "device.esp.CoreVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'CoreVersion\n${currentValue}', defaultState: true
        }  
        valueTile("esp.SdkVersion", "device.esp.SdkVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SdkVersion\n${currentValue}', defaultState: true
        } 
        valueTile("esp.CpuFreqMHz", "device.esp.CpuFreqMHz", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'CpuFreqMHz\n${currentValue}', defaultState: true
        }        
        valueTile("esp.SketchSize", "device.esp.SketchSize", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchSize\n${currentValue}', defaultState: true
        }
        valueTile("esp.SdkVersion", "device.esp.SdkVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SdkVersion\n${currentValue}', defaultState: true
        } 
        valueTile("esp.SketchVersion", "device.esp.SketchVersion", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchVersion\n${currentValue}', defaultState: true
        }
        valueTile("esp.SketchName", "device.esp.SketchName", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchName\n${currentValue}', defaultState: true
        }
        valueTile("esp.ResetReason", "device.esp.ResetReason", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'ResetReason\n${currentValue}', defaultState: true
        }
        valueTile("esp.FlashChipSpeed", "device.esp.FlashChipSpeed", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'FlashChipSpeed\n${currentValue}', defaultState: true
        }        
        valueTile("esp.rssi", "device.esp.Rssi", width: 1, height: 1, canChangeBackground: false) {
        	state("temperature", label:'rssi\n${currentValue}%', 
            backgroundColors:[
                [value: 8, color: "#153591"],
                [value: 30, color: "#1e9cbb"],
                [value: 50, color: "#90d2a7"],
                [value: 90, color: "#44b621"],
            ])
    	}        
        valueTile("esp.temperature",  "device.esp.Temperature",  width: 1, height: 1, canChangeBackground: true) {
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
        valueTile("esp.humidity",  "device.esp.Humidity",  width: 1, height: 1, canChangeBackground: true) {
        	state("temperature", label:'humidity ${currentValue}%', 
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
        standardTile("esp.Led", "device.esp.Led",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'LED', action: "toggle16", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'LED', action: "toggle16", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		} 
        standardTile("light", "device.light",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'Light', action: "toggleLight", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Light', action: "toggleLight", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		} 
        
        main "esp.Door"
        details ([
        "esp.Door", 
        "refresh",
        "light", 
     
        "esp.temperature",
        "esp.humidity",
        "esp.rssi",
        
        "cameraDetails",
        
        "esp.Led",
        "restart",
        "image",
        
        "esp.currentTime",
        "esp.Devicename", 
        "esp.IPaddress",
        "esp.MACaddress",
        "esp.CpuFreqMHz",
        "esp.Freeheap", 
        "esp.UpTime",
        "esp.bootTime",
        "esp.ChipId",
        "esp.FlashChipId",
        "esp.FlashChipSize",
        "esp.RealFlashChipSize",        
        "esp.FlashChipSpeed",
        "esp.CoreVersion",
        "esp.SdkVersion",
        "esp.SketchName",       
        "esp.SketchVersion",
        "esp.SketchSize",
        "esp.ResetReason",
        ])
    }
}

def initialize() {
    log.debug "In initialize"
	runEvery1Minute(GetDevice)
    zero()
}

def parse(String description) {    
    def map = stringToMap(description)
    //log.info "map = ${map}"
    
    def parsedEvent = parseDiscoveryMessage(description)
    
    //process images
    if (map.tempImageKey) {
        log.error "received image"
        try {
            log.error "storing image"
            storeTemporaryImage(map.tempImageKey, getPictureName())
            sendEvent(name: "refresh", isStateChange: "true", value: "Idle", descriptionText: "Refresh set to Idle4")
            return null
        } catch (Exception e) {
            log.error e
        }
        
    } else if (map.error) {
        log.error "Error: ${map.error}"
    }
    
    def msg = parseLanMessage(description)   
        
    //process door state
    if (parsedEvent['body'] != null && parsedEvent['body'].size() > 4 && parsedEvent['body'].size() < 20) {
        def cmd = new String(parsedEvent['body'].decodeBase64())
        def size = parsedEvent['body'].size()    
        log.warn "------------ door is ${cmd} ------------"
       
        sendEvent(name: "esp.Door", value: cmd, isStateChange: true, descriptiontext: "door is ${cmd}")
        sendEvent(name: "refresh", isStateChange: "true", value: "Idle")
        sendEvent(name: "restart", isStateChange: "true", value: "Idle")
        //runIn(30,take)
        //take()
    } 
    
    //process device info
    if (parsedEvent['body'] != null && parsedEvent['body'].size() > 20) {
        log.warn "processing device response"
                     
        def json = new groovy.json.JsonSlurper().parseText(msg.body)
        log.warn "json = ${msg.body}"
        if (!json) {
          sendEvent(name: "refresh", isStateChange: "true", value: "Idle")
          return 
        }  
    
        log.debug "process each json key" 
        json.keySet().each {
           sendEvent(name: it, value: json[it], isStateChange: "true")
        }
        sendEvent(name: "refresh", isStateChange: "true", value: "Idle")
        sendEvent(name: "restart", isStateChange: "true", value: "Idle")
    }   
}

def refresh() {
    log.debug "Executing 'refresh'"
    refresh_action()    
    take()
}

def poll() {
    log.debug "Executing 'poll' from ${device.deviceNetworkId} "
    subscribeAction(getDataValue("ssdpPath"))
    def path = getDataValue("ssdpPath")
    zero()
    sendHubCommand(setFeature("/device"))
}

def GetDevice() {
    sendHubCommand(setFeature("/device"))
}    

def zero() {
   sendEvent(name: "esp.Devicename", value: "",isStateChange: "true")
   sendEvent(name: "esp.currentTime", value: "",isStateChange: "true")
   sendEvent(name: "esp.upTime", value: "",isStateChange: "true")
   sendEvent(name: "esp.IPaddress", value: "",isStateChange: "true")
   sendEvent(name: "esp.MACaddress", value: "",isStateChange: "true")
   sendEvent(name: "esp.bootTime", value: "",isStateChange: "true")
   sendEvent(name: "esp.ChipId", value: "",isStateChange: "true")
   sendEvent(name: "esp.FlashChipSize", value: "",isStateChange: "true")
   sendEvent(name: "esp.RealFlashChipSize", value: "",isStateChange: "true")
   sendEvent(name: "esp.Freeheap", value: "",isStateChange: "true")
   sendEvent(name: "esp.FlashChipSpeed", value: "",isStateChange: "true")
   sendEvent(name: "esp.CoreVersion", value: "",isStateChange: "true")
   sendEvent(name: "esp.SdkVersion", value: "",isStateChange: "true")
   sendEvent(name: "esp.CpuFreqMHz", value: "",isStateChange: "true")
   sendEvent(name: "esp.SketchSize", value: "",isStateChange: "true")
   sendEvent(name: "esp.SketchVersion", value: "",isStateChange: "true")
   sendEvent(name: "esp.SketchName", value: "",isStateChange: "true")
   sendEvent(name: "esp.rssi", value: "",isStateChange: "true")
   sendEvent(name: "esp.temperature", value: "",isStateChange: "true")
   sendEvent(name: "esp.humidity", value: "",isStateChange: "true")
   sendEvent(name: "esp.led", value: "off",isStateChange: "true")
   sendEvent(name: "refresh", value: "idle",isStateChange: "true")
   sendEvent(name: "restart", value: "idle",isStateChange: "true")
   
}

def clear() {
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
	action()
}

def close() {
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
	//sendHubCommand(setFeature("/status"))             
    sendHubCommand(setFeature("/device")) 
}  

def action() {
    sendHubCommand(setFeature(on_path))
    take()
    //runIn(15,take)
}  

def subscribe() {
    subscribeAction(getDataValue("ssdpPath"))
}

def Toggle(device) {
    log.info "Toggle: ${device}"
    sendHubCommand(setFeature(device))
    //sendHubCommand(setFeature("/device")) 
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
    //log.debug "Encoding credentials"
    def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private getHeader(userpass){
    //log.debug "state.control_ip:port = ${control_ip}:${control_port}"
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
    //def ip = getDataValue("ip")
    def ip = control_ip
    //def port = getDataValue("port")
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
    //log.warn "In parseDiscoveryMessage"
    def device = [:]
    def parts = description.split(',')
    //log.warn "parts = ${parts}"
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

    //def newIP = getDataValue("ip")
    def newIP = control_ip
    //log.debug "newIP = ${newIP}"
    //def newPort = getDataValue("port")
    def newPort = control_port
    //log.debug "newPort = ${newPort}"
    
    if (!newIP || !newPort) {
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    } else {
    	//def ip = convertHexToIP(getDataValue("ip"))
        def ip = control_ip
        //def port = convertHexToInt(getDataValue("port"))
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
