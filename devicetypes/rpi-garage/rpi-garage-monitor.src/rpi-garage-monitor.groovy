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
        capability "Temperature Measurement"
        capability "Thermostat"
        command "subscribe"
        command "toggle16"
        command "toggle2"
        command "toggleAll"
        command "restart"
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
        standardTile("door", "device.door", width: 1, height: 1, canChangeIcon: false ) {
            state "open", label: '${name}', action: "close", icon: "st.doors.garage.garage-open", backgroundColor: "#b82121", nextState: "closing"
            state "closing", label: '${name}', icon: "st.doors.garage.garage-closing", backgroundColor: "#e59e10", nextState: "closed"
            state "closed", label: '${name}', action: "open", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff", nextState: "opening"
            state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#e59e10", nextState: "open"

        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("restart", "device.switch", canChangeBackground: true) {
            state "default", label:'restart', action:"restart", backgroundColor: "#ffffff"
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
        valueTile("esp.bootTime", "device.esp.bootTime", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
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
        valueTile("esp.SketchName", "device.esp.SketchName", width: 3, height: 1, decoration: "flat", canChangeBackground: false) {
            state "on", label:'SketchName\n${currentValue}', defaultState: true
        }
        valueTile("esp.rssi", "device.esp.rssi", width: 1, height: 1, canChangeBackground: false) {
        	state("temperature", label:'signal\n${currentValue}', 
            backgroundColors:[
                [value: -20, color: "#153591"],
                [value: -30, color: "#1e9cbb"],
                [value: -40, color: "#90d2a7"],
                [value: -50, color: "#44b621"],
                [value: -60, color: "#f1d801"],
                [value: -70, color: "#d04e00"],
                [value: -80, color: "#bc2323"]
            ])
    	}  
        
        valueTile("esp.temperature",  "device.esp.temperature",  width: 1, height: 1, canChangeBackground: true) {
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
        
        valueTile("esp.humidity",  "device.esp.humidity",  width: 1, height: 1, canChangeBackground: true) {
        	state("temperature", label:'${currentValue}%', 
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
        
        standardTile("esp.led16", "device.esp.led16",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'LED 16', action: "toggle16", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'LED 16', action: "toggle16", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		} 
        standardTile("esp.led2", "device.esp.led2",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'LED 2', action: "toggle2", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'LED 2', action: "toggle2", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		}
        standardTile("esp.led", "device.esp.led",   width: 1, height: 1, canChangeBackground: true) {
			state "off", label: 'ALL LED', action: "toggleAll", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'ALL LED', action: "toggleAll", icon: "st.Lighting.light11", backgroundColor: "#79b821", nextState: "off"
		}         
        
        main "door"
        details (["door", 
        "refresh",
        "esp.temperature",
        "esp.humidity",
        "esp.rssi",
        "restart",
        "esp.led16",
        "esp.led2",
        "esp.led",
        "esp.Devicename", 
        "esp.IPaddress",
        "esp.MACaddress",
        "esp.bootTime",
        "esp.ChipId",
        "esp.FlashChipId",
        "esp.FlashChipSize",
        "esp.RealFlashChipSize",
        "esp.Freeheap",
        "esp.CoreVersion",
        "esp.SdkVersion",
        "esp.CpuFreqMHz",
        "esp.SketchSize",
        "esp.SketchVersion",
        "esp.SketchName"
        ])
    }
}

def open() {
    log.debug "******* in open routine *******"
	action()
}

def close() {
    log.debug "******* in close routine *******"
	action()
}

def setFeature(query) {
    def userpass = encodeCredentials(username, password)
    def headers = getHeader(userpass)
    log.debug "headers = ${headers}"
    //def dni = setDeviceNetworkId("${control_ip}","${control_port}")

     
    def action = new physicalgraph.device.HubAction(
		method: "GET",
		path: query,
        headers: headers,
        dni
	)
    
    log.info "setFeature query: ${query}\naction =\n${action}"
	return action
}

def refresh_action() {
        log.debug "******* in fresh_action routine *******"
        //def userpass = encodeCredentials(username, password)
        //def last = device.currentValue("door")
        //log.debug("On lastState = '$last'")
        
        //def headers = getHeader(userpass)
/*
		def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/status",
                //path: "/st?id=left",
                headers: headers
			    )
*/        
         
	     sendHubCommand(setFeature("/status"))
         //log.debug "fresh_action result = ${result}"
/*        
         def getdevice = new physicalgraph.device.HubAction(
				method: "GET",
				path: "/device",
                headers: headers
			    )
*/                
	     sendHubCommand(setFeature("/device"))
         //log.debug "fresh_action getdevice = ${getdevice}"
 
}  

def action() {
        log.debug "******* in action routine *******"
        def userpass = encodeCredentials(username, password)
        def last = device.currentValue("door")
        log.debug("On lastState = '$last'")
        
        def headers = getHeader(userpass)

		def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "${on_path}",
                headers: headers
			    )
	    sendHubCommand(result)
        log.debug "action result = ${result}"
    
		log.debug "Executing on" 
            //refresh()
}  

private encodeCredentials(username, password){
  log.debug "Encoding credentials"
    def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    //log.debug "ASCII credentials are ${userpassascii}"
    //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass){
  log.debug "Getting headers"
  log.debug "state.control_ip:port = ${control_ip}:${control_port}"
    def headers = [:]
    headers.put("HOST", "${control_ip}:${control_port}")
    headers.put("Authorization", userpass)
    log.debug "Headers are ${headers}"
    return headers
}
// parse events into attributes

def parse(String description) {
    log.info "######### In parse #########"
    //log.info "description = ${description}"
    
    def parent = getDevice()
    def childdevice = getChildDevices()
    def msg = parseLanMessage(description)
    
    
    //log.debug "msg = ${msg}"
    //log.info "parent = ${parent}"
    //log.info "childdevice = ${childdevice}"
    
    def usn = getDataValue('ssdpUSN')
    //log.debug "usn = ${usn}"    
    def ssdpUsn = getDataValue('ssdpUSN')
    //log.info "device = ${device}"
    //log.debug "Parsing garage DT DNI=${device.deviceNetworkId} ssdpUsn=${ssdpUsn} description='${description}'"

    def parsedEvent = parseDiscoveryMessage(description)
    //log.debug "parsedEvent = ${parsedEvent}"
    //log.debug "parent = ${parent}"
    def headers = new String(parsedEvent['headers'].decodeBase64())
    def body = parsedEvent['body']
    //log.info "headers = ${headers}"
    //log.debug "body = ${body}"

    if (parsedEvent['body'] != null && parsedEvent['body'].size() > 4 && parsedEvent['body'].size() < 20) {
        def cmd = new String(parsedEvent['body'].decodeBase64())
        log.info "cmd = ${cmd}"
        def size = parsedEvent['body'].size()    
        log.debug "Processing command ${cmd}" 
        log.info "sending command:  ${cmd} to sendEvent door"
        sendEvent(name: 'door', value: cmd)
        return null
    } else {
        def json = new groovy.json.JsonSlurper().parseText(msg.body)
    
        log.warn "json = ${json}"
   
        if (!json) {
           sendEvent(name: "refresh", isStateChange: "true", value: "Idle", descriptionText: "Refresh was activated")
           return null
        }  
    
        sendEvent(name: "refresh", isStateChange: "true", value: "Idle", descriptionText: "Refresh was activated")
    
        log.debug "process each json key" 
        json.keySet().each {
           log.debug "key = ${it} val = ${json[it]}"
           sendEvent(name: it, value: json[it])
        }
    }   
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
    log.debug "getHostAddress:ip = ${ip}"

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

def getRequest(path) {
    log.debug "Sending request for ${path} from ${device.deviceNetworkId}"

    new physicalgraph.device.HubAction(
        'method': 'GET',
        'path': path,
        'headers': [
            'HOST': getHostAddress(),
        ], device.deviceNetworkId)
}

def poll() {
    log.debug "Executing 'poll' from ${device.deviceNetworkId} "

    subscribeAction(getDataValue("ssdpPath"))
    def path = getDataValue("ssdpPath")
    log.debug "******** path = ${ssdpPath} **********"
}

def refresh() {
    log.debug "Executing 'refresh'"

    def path = getDataValue("ssdpPath")
    //getRequest(path)
    //New
    //fresh_action()
    //New
    refresh_action()
    log.info "calling subscribeAction - getDataValue with ssdpPath: ${path}"
    subscribeAction(getDataValue("ssdpPath"))
}

def subscribe() {
    log.debug "Subscribe requested"
    subscribeAction(getDataValue("ssdpPath"))
}


def Toggle(device) {
    sendHubCommand(setFeature(device))
}

def toggle16() {
	log.info "Executing toggle16"
    Toggle("/toggleLed16")
}

def toggle2() {
	log.info "Executing toggle2"
    Toggle("/toggleLed2")
}

def toggleAll() {
    Toggle("/toggle")
}    

def restart() {
    Toggle("/restart")
}    

private def parseDiscoveryMessage(String description) {
    log.warn "In parseDiscoveryMessage"
    def device = [:]
    def parts = description.split(',')
    log.warn "parts = ${parts}"
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
            log.warn "devicetype = ${valueString}"
        } else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                log.warn "mac = ${valueString}"
            }
        } else if (part.startsWith('ip:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                log.warn "ip = ${valueString}"
            }    
        } else if (part.startsWith('port:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                log.warn "port = ${valueString}"
            }     
        } else if (part.startsWith('requestId:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
                log.warn "requestId = ${valueString}"
            }     
        } else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ip = valueString
                log.warn "networkAddress = ${valueString}"
            }
        } else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.port = valueString
                log.warn "deviceAddress = ${valueString}"
            }
        } else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ssdpPath = valueString
                log.warn "ssdpPath = ${valueString}"
            }
        } else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpUSN = valueString
                log.warn "ssdpUSN = ${valueString}"
            }
        } else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpTerm = valueString
                log.warn "ssdpTerm = ${valueString}"
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                device.headers = valueString
                def headers = new String(device.headers.decodeBase64())
                log.warn "headers = ${device.headers}"
                log.warn 
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                device.body = valueString
                log.warn "body = ${valueString}"
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
    log.debug "newIP = ${newIP}"
    //def newPort = getDataValue("port")
    def newPort = control_port
    log.debug "newPort = ${newPort}"
    
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