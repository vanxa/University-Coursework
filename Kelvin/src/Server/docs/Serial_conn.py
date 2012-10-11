#!/usr/bin/env python
"""
This is the main Serial Communications file.

Establishes serial communication with the Base Station and
initializes the Sensor_thread.py, which takes care of retrieving sensor data 
and storing it to database.

The module has preconfigured values for baudrate and timeout, but can be
explicitly specified by the user.

The module defines the data transmission mechanism using base64 implementation
"""
import time
from random import randint
from serial import serialutil, Serial
import sys
from scan import scan
from sensor_thread import Sensor
from kelvin_protocol import kelvin
from struct import *
from base64 import b64encode, b64decode
from Server_config import *

class Serial_conn:
    """
    Serial communication class. Provides connection and message transmission functionalities.
    """
    def __init__(self):
        """
        Initializes the SComms module.
        """
        self.supports_base64 = False
        LOGMSG ("Server starting")
        self.start()
            
    def get_port(self):
        """
        Specified the port number to be opened. Scans all available ports, and prompts the user to select a port number to be opened
        @rtype: number
        @return: the port number to be opened
        """
        ports = self.scan_for_ports()
        while(ports == []):
            LOGMSG("Base station is not connected to any serial port. Please connect base station before proceeding. Press ENTER to try again, or type EXIT to quit")
            msg = input()
            if(msg==''):
                ports = self.scan_for_ports()
            elif(msg=='exit'):
                exit()
            elif(msg=='break'):
                break
            else:
                LOGMSG('Invalid command!Please try again')
        
        msg = "Serial connections detected at the following ports:"
        i = 0
        while(i<len(ports)):
            msg += " COMM%d" %ports[i]
            i += 1
        LOGMSG(msg)

        self.port = int(input("Please select a PORT number: "))
        while((self.port<1) | (self.port>10)):
            self.port = int(input("Invalid PORT number! Please enter a valid PORT number: "))
        return self.port
        
    def get_baud(self, msg="Please enter the baudrate of the connection, or press ENTER to skip this step (the baudrate will be set to default value 38400): "):
        """
        Prompts the user for a baudrate value. If none selected, a default value is chosen.
        @type msg: string
        @param msg: the prompt message to be displayed to the user. 
        @rtype: number
        @return: the baudrate value
        """
        self.baud = (input(msg))
        if(self.baud==''):
            self.baud = 38400
        return int(self.baud)

    def get_timeout(self, msg="Please enter the connection timeout (in seconds), or press ENTER to skip this step (the timeout will be set to default value 1): "):
        """
        Prompts the user for a timeout value. If none selected, a default value is chosen.
        @type msg: string
        @param msg: the prompt message to be displayed to the user. 
        @rtype: number
        @return: the timeout value
        """
        self._timeout = (input(msg))
        if(self._timeout==''):
            self._timeout = 1
        return int(self._timeout)

    
    def close(self,status=0):
        """
        Terminates the serial communication and closes the the sensor_thread instance
        @type status: number
        @param status: the close status value
        """
        try:
                self.sensor.close()
        except:
        	if(self.serial != None and self.serial.isOpen()):
				self.serial.close()
            
        
    def start(self):
        """
        Starts the SComms module. Prompts the user for a port number, baudrate and timeout values, and connects to the Base Station using these values.
        """
        self.port = self.get_port()
        self.baud = self.get_baud()
        self._timeout = self.get_timeout()
        # Establish serial port connection
        self.connect(self.port-1, self.baud, self._timeout)
        # Connect to base station
        self.base_st_connect()
    
    def base_st_connect(self):
        """
        Sends a confirmation message to Base Station to acknowledge connection. If it receives and ACK from Base Station, starts up the Sensor_thread.
        """
        self.tried = 0
        while(self.tried<=5):
            LOGMSG("Checking if base station is responding...")
            self.tried += 1
            msg = pack('8sBBB','SLIPCC11',kelvin.DISPATCH,kelvin.CMD | kelvin.CONN_REQ, 0)
            self.write(msg)
            time.sleep(2)
            rsp = self.read()
            if(rsp != b''):
                break
                    
        if(self.tried > 5):
            LOGMSG("Base station currently unreachable. Please try later.")
            LOGMSG("The server will now close...")
            self.close()
            
        else:    
            LOGMSG("Connected to base station.")
            
            #Start a thread which will handle all sensor data extraction
            self.sensor = Sensor(self)
            self.sensor.start()

    def connect(self, port , baud = 38400 , _timeout = 1):
        """
        Tries to connected to specified port, using given baudrate and timeout values
        @type port: number
        @param port: the port to be opened
        @type baud: number
        @param baud: the specified baudrate
        @type _timeout: number
        @param _timeout: the specified timeout value
        """
        LOGMSG("Connecting to base station ...")    
        try:
            self.serial = Serial(port,baud, timeout = _timeout)
        except serialutil.SerialException:
            LOGMSG ("Could not open port %d." %self.port)
            LOGMSG ("Retrying to reconnect in 5 seconds..." )
            time.sleep(5)
            LOGMSG("Reconnecting...")
            try:
                self.serial = Serial(port,baud, timeout = _timeout)
            except serialutil.SerialException:
                LOGMSG("Could not open port %d." %self.port)
                self.port = self.get_port("Maybe the PORT number is wrong? Please enter PORT number again: ");
                LOGMSG("Reconnecting...")
                try:
                    self.serial = Serial(port,baud, timeout= _timeout)
                except serialutil.SerialException:
                    LOGMSG ("Could not open port %d. " %self.port)
                    LOGMSG("Please check you configurations and PORT connections!")
                    self.close(1)

        LOGMSG("Serial connection established on port %d. Using baudrate %d and timeout %d" %(self.port, self.baud, self._timeout))


    def scan_for_ports(self):
        """
        Scans the system for open ports
        @rtype: array
        @return: an array of available ports
        """
        result=[]
        ports = scan()
        if (len(ports) >0):
            i = 1
            while(i<len(ports)):
                result.append(ports[i][0]+1)
                i+=1
        return result

################################################## Serial functions ############################################
    def _process_line(self, line):
        """
        Process line and return.
           - Checks if the other end responds in Base64 and changes operating mode.
           - Removes trailing \r\n        
        """
        if line is not None and len(line) > 0:
            line = line.rstrip(b'\r\n')
            if line[:1] == b'!':
                self.supports_base64 = True
                try:
                    line = b''.join([b64decode(part) for part in line.split(b'!')[1:]])
                except:
                    DEBUG("Could not decode!")

        return line
    
    def read( self ):
        """
        Calls _process_line to read incoming messages
        """
        return self._process_line(self.serial.readline())

    def write( self, string ):
        """
        Only responds in Base64 if a previously received line was Base64.
        Adds \r\n
        """
        if self.supports_base64:
            string = b'!' + b64encode(string)
        self.serial.write( string + b'\r\n' )

    def __iter__(self):
        self._current_iter = iter(self.serial)
        return self

    def __next__(self):
        return self._process_line(self._current_iter.__next__())

    def set_color(self, col):
        self.writeline(b"s "+str(col))

    def flash(self):
        i = 0
        for i in range(100):
            self.set_color(randint(0,7))
            time.sleep(randint(1,7)/20)

################################### DEPRECATED ############################################
    def pause(self):
        """
        Pauses the sensor_thread
            - Deprecated
        """
        if(self.sensor != None):
            if(self.sensor.closed != 1):
                if(self.sensor.paused != 1):
                    self.sensor.paused = 1
                    DEBUG("Flow paused")
                else:
                    DEBUG("Already paused!")

    def resume(self):
        """
        Resumes the sensor_thread
            - Deprecated
        """
        if(self.sensor != None):
            if(self.sensor.closed != 1):
                if(self.sensor.paused == 1):
                    self.sensor.paused = 0
                    DEBUG("Flow resumed")
                else:
                    DEBUG("Flow not paused!")
###########################################################################################
