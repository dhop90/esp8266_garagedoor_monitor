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

metadata {
    definition (name: "RPi Garage Monitor", namespace: "rpi_garage", author: "David Hopson") {
        capability "Door Control"
        capability "Polling"
        capability "Refresh"
        command "subscribe"
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
        standardTile("door", "device.door", width: 2, height: 2, canChangeIcon: false) {
            state "open", label: '${name}', action: "close", icon: "st.doors.garage.garage-open", backgroundColor: "#b82121", nextState: "closing"
            state "closing", label: '${name}', icon: "st.doors.garage.garage-closing", backgroundColor: "#e59e10", nextState: "closed"
            state "closed", label: '${name}', action: "open", icon: "st.doors.garage.garage-closed", backgroundColor: "#ffffff", nextState: "opening"
            state "opening", label: '${name}', icon: "st.doors.garage.garage-opening", backgroundColor: "#e59e10", nextState: "open"

        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "door"
        details (["door", "refresh"])
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
            log.debug result
    
			log.debug "Executing on" 
            refresh()
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
    def usn = getDataValue('ssdpUSN')
    log.debug "Parsing garage DT ${device.deviceNetworkId} ${usn} '${description}'"

    def parsedEvent = parseDiscoveryMessage(description)

    if (parsedEvent['body'] != null) {
        def xmlText = new String(parsedEvent.body.decodeBase64())
        def xmlTop = new XmlSlurper().parseText(xmlText)
        def cmd = xmlTop.cmd[0]
        def targetUsn = xmlTop.usn[0].toString()

        log.debug "Processing command ${cmd} for ${targetUsn}"

        parent.getAllChildDevices().each { child ->
            def childUsn = child.getDataValue("ssdpUSN").toString()
            if (childUsn == targetUsn) {
                if (cmd == 'poll') {
                    log.debug "Instructing child ${child.device.label} to poll"
                    child.poll()
                } else if (cmd == 'status-open') {
                    def value = 'open'
                    log.debug "Updating ${child.device.label} to ${value} sending to door"
                    child.sendEvent(name: 'door', value: value)
                } else if (cmd == 'status-closed') {
                    def value = 'closed'
                    log.debug "Updating ${child.device.label} to ${value} sending to door"
                    child.sendEvent(name: 'door', value: value)
                }
            }
        }
    }
    null
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            //log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    //convert IP/port
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
}

def refresh() {
    log.debug "Executing 'refresh'"

    //def path = getDataValue("ssdpPath")
    //getRequest(path)
    subscribeAction(getDataValue("ssdpPath"))
}

def subscribe() {
    log.debug "Subscribe requested"
    subscribeAction(getDataValue("ssdpPath"))
}

private def parseDiscoveryMessage(String description) {
    def device = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
        } else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
            }
        } else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ip = valueString
            }
        } else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.port = valueString
            }
        } else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ssdpPath = valueString
            }
        } else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpUSN = valueString
            }
        } else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpTerm = valueString
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                device.headers = valueString
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                device.body = valueString
            }
        }
    }

    device
}

private subscribeAction(path, callbackPath="") {
    def address = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
    def parts = device.deviceNetworkId.split(":")
    def ip = convertHexToIP(getDataValue("ip"))
    def port = convertHexToInt(getDataValue("port"))
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