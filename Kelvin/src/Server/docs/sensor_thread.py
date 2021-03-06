from threading import *
from serial import Serial
import time
import sys
from struct import *
from DBE import DBE
from queue import PriorityQueue
from RepeatTimer import RepeatTimer
from kelvin_protocol import kelvin
from XMLGenerator import XMLGenerator
from Server_config import *

class Sensor(Thread):
    """
    The Data Retrieval Module (DRM). 
    
    Its role is to request sensor and statistical data from connected devices, and store that information in the database
    
        - Uses a timeout to periodically request data from Base Station
    """
    DEBUG_MODE = 0
    """
    Operational mode. 
    
    If debug mode is switched, the program will skip all procedures and will execute the custom code located after the control statement:
            
    if(not DEBUG_MODE):
    ...
    else:
    """
    #Thresholds
    maxtemp = 0
    mintemp = 0


    # Tuple containing board addresses
    boards = {}
    closed = 0
    paused = 0
    text = ''

    #polling intervals
    sleep = 2
    stat_interval = 6 # multiply with timeout to get time, set to 0 to disable
    reset_interval = 0#12
    timeout = 4*sleep
    _MIN_TIMEOUT = 0
    _MAX_TIMEOUT = 120
    _DEFAULT_TIMEOUT = 3

    _BCAST = b'\xFF\xFF\xFF\xFF\xFF\xFF\xFF\xFF'
    """
    Hex address of the broadcast address
    """
    _SLIP_ADDR = 'SLIPCC'
    """
    Default device address. It should be followed by the device's hardware address, repeated twice.
    """

    def __init__(self, serial):
        self.closed = 0
        self.serial = serial
        self.stat_counter = 0
        self.reset_counter = 0
        LOGMSG("Sensor thread started..")
        time.sleep(1)
        Thread.__init__ ( self )

    def run(self):
        LOGMSG("Sensor thread running..")
        self.timer = RepeatTimer(self.timeout,self.send_rcv_msg) # For now, send only temp requests
        self.timer.start()

    def close(self):
        LOGMSG("Sensor thread closing...")
        self.timer.cancel()
        time.sleep(3)
        self.send_reset()
        try:
           sys.exit(0)
        except SystemExit:
            pass
        
    def send_reset(self):
        msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH,kelvin.CMD | kelvin.RESET, 0)
        self.serial.write(msg)

    def sht_temp_scale( self , data ):
        """
        Calibrate temperature data.
        @type data: number
        @param data: the temperature reading
        @return: the calibrated temperature reading
        """
        #(value,) = unpack( '>h' , data )
        value = data[0] << 8 | data[1]
        #Formula for calculating the temperature from the SHT at 3.3v
        scaled = (value/100.0 - 39.65)
        DEBUG( str(value) + ' (raw) -> Scaled!! ' + str(scaled) )
        return scaled

    def sht_hum_scale(self, data):
        """
        Calibrate humidity data.
        @type data: number
        @param data: the humidity reading
        @return: the calibrated humidity reading
        """
        value = data[0] << 8 | data[1]
        scaled = (value*0.0367) -2.0468 + ((value*value)*(-0.000001595))
        DEBUG(str(value) + '(raw) -> scaled!! ' + str(scaled))
        return scaled

    def light_scale( self , data ):
        """
        Calibrate light data.
        @type data: number
        @param data: the light reading
        @return: the calibrated light reading
        """
        #Equation to convert to LUX is ADC val * 5
        return data*5

    def pressure_scale( self , data ):
        """
        Calibrate pressure data.
        @type data: number
        @param data: the pressure reading
        @return: the calibrated pressurereading
        """
        #Equation to convert to kPa
        return (((data*11)/2125)+0.095)/0.009

    def read_data(self) :
        """
        Read incoming packages and process them.
        
        Uses base64 encoding and decoding to read data.
            - Checks for address validity and tries to amend corrupted addresses.
            - If unable to amend address, discards packet
            - Reads and stores data in database
            - If the data comes from a temperature sensors, sends a LED colour change command to the device
        """
        self.text = ''
        for y in self.serial:
            if( len(y) < 11 ):
                continue
            
            length = y[10]
            address = ''
            dispatch = ''
            cmd = 0
            datalen = 0
            if(length == 0):
                fmt = ('8sBBB')
                try:
                    (address , dispatch , cmd , datalen) = unpack( fmt  , y )
                except:
                    LOGMSG(y)
                    continue
            
            elif(length == 5):
                try:
                    (address, dispatch, cmd, datalen, rtt, hops) = unpack('>8sBBBLB',y)
                except:
                    LOGMSG(y)
                    continue
            else:
                fmt = ('8sBBB%ds' % ( length ));
                try:
                    (address , dispatch , cmd , datalen , data ) = unpack( fmt  , y )
                except:
                    LOGMSG(y)
                    continue
                        
            similar = self.sim(address[:6].decode('utf-8'),'SLIPCC')
            if(similar == 6 and address[6] == address[7]):
                addr = self.format_address(address)
            elif((similar < 6 and similar > 3) and address[6] == address[7]):
                addr = self.format_address(b'SLIPCC'+address[6:8])                    
            else:
                addr = ''

            if(addr != ''):
                tm = round(time.time())                                
                if( cmd == kelvin.TEMP ):
                    scaled = self.sht_temp_scale(data)
                    LOGMSG("Node %02X gives temp %d\n" % (address[7] , scaled) )
                    self.add_to_list(addr,tm)
                    if(scaled <self.maxtemp and scaled > self.mintemp):
                        LOGMSG("Measured temperature is within norms")
                        msg = pack('8sBBBB',address,kelvin.DISPATCH,kelvin.CMD | kelvin.LED,1,5)
                        self.serial.write(msg)
                    elif(scaled < self.mintemp):
                        LOGMSG("Measured temperature is below norms")
                        msg = pack('8sBBBB',address,kelvin.DISPATCH, kelvin.CMD | kelvin.LED, 1,3)
                        self.serial.write(msg)
                    elif(scaled > self.maxtemp):
                        LOGMSG("Measured temperature is above norms")
                        msg = pack('8sBBBB',address,kelvin.DISPATCH, kelvin.CMD | kelvin.LED, 1,6)
                        self.serial.write(msg)
                    self.req_queue.put((1,self.db.set_measurements,('temp',addr,self.sht_temp_scale(data),tm)))
                elif( cmd == kelvin.HUM):
                    scaled = self.sht_hum_scale(data)
                    LOGMSG("Node %02X gives humidity %02X\n" % (address[7] , scaled) )
                    self.add_to_list(addr,tm)
                    self.req_queue.put((1,self.db.set_measurements,('humidity',addr,self.sht_hum_scale(data),tm)))
                elif(cmd == kelvin.PRSR):
                    LOGMSG("Node %02X gives pressure %02X\n" % (address[7],data[0]))
                    self.req_queue.put((1,self.db.set_measurements,('pressure',addr,self.pressure_scale(data[0]),tm)))
                elif(cmd == kelvin.LGHT):
                    LOGMSG("Node %02X gives light %02X\n" % (address[7],data[0]))
                    self.req_queue.put((1,self.db.set_measurements,('light',addr,self.light_scale(data[0]),tm)))
                elif(cmd == kelvin.PING):
                    #if(rtt < 10000):
                    stat_string = "%d: Got PING stats for node %02X: RTT is: %d; HOPS is: %d" % (time.time(), address[7],rtt,hops)
                    LOGMSG(stat_string)
                    try:
                        file = open('network_stats.log', 'a')
                        file.write(stat_string + "\n")
                        file.close()
                    except:
                        pass
                    #self.req_queue.put((1,self.db.set_stat_data,(addr, rtt,hops,tm)))
    
    def set_timeout(self, _timeout):
        """
        Sets time period between consecutive sends
        @type _timeout: number
        @param _timeout: the timeout value to be set
        """
        if(self._MIN_TIMEOUT <= _timeout and _timeout <= self._MAX_TIMEOUT):
            self.timeout = _timeout
            self.timer = RepeatTimer(self.timeout,self.send_msg)
            self.timer.start()
        else:
            DEBUG('Invalid value!')

    def reset_timeout(self):
        """
        Resets the timeout value to its default value
        """
        self.timeout = self._DEFAULT_TIMEOUT

    def send_rcv_msg(self):
        """
        Sends requests and reads responses
        
            - Uses a small sleep interval between each transmission to prevent race conditions
            - checks for active devices        
        """
        if (not self.DEBUG_MODE):
            try:
                if self.stat_interval != 0:
                    if self.stat_counter > self.stat_interval:
                        time.sleep(self.sleep)
                        msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH, kelvin.CMD | kelvin.PING,0)
                        self.serial.write(msg)
                    
                        self.stat_counter = 0

                        return # reading sensor data should not interfere with RTT!
                    else:
                        self.stat_counter += 1
                        
                msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH,kelvin.CMD | kelvin.TEMP, 0)
                self.serial.write(msg)
                time.sleep(self.sleep)
                msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH,kelvin.CMD | kelvin.HUM, 0)
                self.serial.write(msg)
                time.sleep(self.sleep)
                msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH,kelvin.CMD | kelvin.PRSR, 0)
                self.serial.write(msg)
                time.sleep(self.sleep)
                msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH,kelvin.CMD | kelvin.LGHT, 0)
                self.serial.write(msg)
            except ValueError:
                self.close()
                
            self.read_data()
            #self.print_buffer()
            self.check_board_status()

            if self.reset_interval != 0:
                if self.reset_counter > self.reset_interval:
                    time.sleep(self.sleep)
                    self.send_reset()
                    time.sleep(self.sleep)
                    self.reset_counter = 0
                else:
                    self.reset_counter += 1
            
        else:
            # Add new code to be debugged here
            msg = pack('8sBBB',self._BCAST,kelvin.DISPATCH, kelvin.CMD | kelvin.PING,0)
            self.serial.write(msg)
            self.read_data()
            #self.send_reset()
            #time.sleep(self.sleep)

    def set_db_data(self,_req_queue,_db):
        self.req_queue = _req_queue
        self.db = _db

    def format_address(self,addr):
        """
        Converts the address into more readible form. Used when storing data in database
        """
        _str = ''
        for x in addr:
            if( x <= 0xF ):
                _str += '0'
            _str += hex(x) + ':'

        return _str[:-1].replace("0x","")


    def add_to_list(self,addr,tm):
        self.boards[addr] = tm

    def remove_from_list(self,addr):
        del self.boards[addr]

    def check_board_status(self):
        """
        Performs a check on all devices in the active list. 
            - If a device has not responded for the past 4*timeout iterations, it is deemed inactive and is removed from the inactive list
        """
        inactive = []
        for x in self.boards:
            tm = time.time()
            if(tm - self.boards.get(x) > 4*self.timeout):
                self.req_queue.put((2,self.db.set_status,(x,0)))
                LOGMSG('Board with address '+x +' has become inactive!')
                inactive.append(x)
                
            else:
                self.req_queue.put((2,self.db.set_status,(x,1)))
        for x in inactive:
            self.remove_from_list(x)

    def sim(self,str1,str2):
        """
        Checks for similarity between two strings. 
        @return: similarity measure
        """
        buff1 = str.lower(str1)
        buff2 = str.lower(str2)
        if(len(str1) != len(str2)):
            return 0
        count = 0
        i = 0
        while(i<len(str1)):
            if(buff1[i] == buff2[i]):
                count += 1
            i+=1
        return count

    def set_user_settings(self,_maxtemp,_mintemp):
        if(_mintemp != ''):
            self.mintemp = _mintemp
        if(_max_temp != ''):
            self.maxtemp = _maxtemp
        self.req_queue.put((1,self.db.set_user_settings,(_maxtemp,_mintemp)))

    def get_user_settings(self):
        xml = XMLGenerator()
        xml.add_user_setting('mintemp',self.mintemp)
        xml.add_user_setting('maxtemp',self.maxtemp)
        return xml.print_xml()
                                               
    ################ DEBUG #######################

    def print_buffer( self ):
        """
        Print serial buffer. Used for debugging purposes
        """
        for y in self.serial:
            DEBUG( y )
