#!/usr/bin/env python

import sqlite3
from threading import Thread
import time
from queue import PriorityQueue
from XMLGenerator import XMLGenerator
from Server_config import *

class DBE(Thread):
        """
        The database thread, used for storing sensor data and device information
            - Uses sqlite3 module for providing sql engine
            - Executed as a separate thread
            - Uses an XMLGenerator to create xml documents from queries
        """
        tables = ('temp','light','humidity','pressure','stats')
        """
        a list of tables to be stored in the database
        """

        def __init__(self, _req=None,_res=None):
                LOGMSG("Database initialized")
                self.req_queue = _req
                self.res_queue = _res
                self.closed = 0
                Thread.__init__(self)

        def run(self):
                """
                Runs the database instance
                    - Opens a database object: if file does not exist, creates a new database
                    - Creates a cursor to the database
                    - Checks the database for inconsistencies
                    - Serves requests until terminated
                """
                self.dbconn = sqlite3.connect( 'sensor_data.db' )
                self.cursor = self.dbconn.cursor()
                self.check_db()
                self.serve()

        def commit( self ):
                """
                Commits changes to the database
                """
                self.dbconn.commit()
                
        def rollback(self):
                """
                Rolls back any changes done to the database after the last commit
                """
                self.dbconn.rollback()
                

        def close(self):
                """
                Closes the thread
                """
                self.shutdown()
                self.closed = 1   
        		
        def serve(self):
                """
                Serves until terminated.
                    - Waits for incoming requests
                    - Processes requests 
                    - Outputs responses to requests if available
                """
                if(self.req_queue == None):
                        return
                else:
                        while(not self.closed or not self.req_queue.empty()):
                                req = self.req_queue.get()
                                priority = req[0]
                                cmd = req[1]
                                if(len(req) == 3):
                                        args = req[2]
                                        res = cmd(*args)
                                else:
                                        res = cmd()
                                if(res != None):
                                        self.res_queue.put((priority,res))
                        LOGMSG ("Database closing")

        

        def shutdown(self):
                """
                Shuts down the database.
                """
                self.commit()
                self.cursor.close()
                self.dbconn.close()
                LOGMSG("Database shutting down...")

################################################## SET ###############################################################

        
        def set_measurements(self, table_name, chip_addr, value, _time ):
                """
                Stores sensor information in a related table in the database.
                    - Checks if the sensor devices exists in database: new devices are added 
                    - Checks for data redundancy: if such redundancy detected, discards packet
                    
                @type table_name: string
                @param table_name: the table in which to store the data.
                @type chip_addr: string
                @param chip_addr: the address of the sensor device
                @type value: number
                @param value: the sensor reading
                @type _time: number
                @param _time: the time at which the reading was received
                """
                if(self.check_chip(chip_addr) != 1):
                        LOGMSG('Chip not in database! Adding...')
                        self.add_chip(chip_addr)
                try:
                        if(self.check_duplicate_data(table_name,chip_addr,value,_time) == 0):
                                if(self.check_table_name(table_name)==1):
                                        self.cursor.execute("insert into "+table_name+" values( ? , ? , ?)", (chip_addr,value,_time))
                                        self.commit()
                                       
                        else:
                                LOGMSG("Duplicate data, discarding...")
                except sqlite3.OperationalError:
                        LOGMSG('Table not found!')
                                                

        def set_status(self, chip_addr, value):
                """
                Sets the status of the sensor device
                    - 1 for active
                    - 0 for inactive
                @type chip_addr: string
                @param chip_addr: the address of the sensor device
                @type value: number
                @param value: the status value of the device
                """
                self.cursor.execute('update chip set status=? where address=?',(value,chip_addr))


        def set_user_settings(self, name, max_temp='', min_temp=''):
                """
                Sets user-defined temperature thresholds
                @type name: string
                @param name: the name for the settings instance
                @type max_temp: number
                @param max_temp: maximum temperature threshold
                @type min_temp: number
                @param min_temp: minimum temperature threshold
                """
                if(max_temp != '' and min_temp != ''):
                        self.cursor.execute('update user_settings set max_temp=?,min_temp=? where name=?',(max_temp,min_temp,name))
                elif(max_temp == '' and min_temp != ''):
                        self.cursor.execute('update user_settings set min_temp=? where name=?',(min_temp,name))
                elif(max_temp != '' and min_temp==''):
                        self.cursor.execute('update user_settings set max_temp=? where name=?',(max_temp,name))
                self.commit()

        def set_stat_data(self,addr, rtt,hops,tm):
                """
                Adds RTT  data for statistics
                    - Currently not in use
                @type addr: string
                @param addr: the address of the device
                @type rtt: number
                @param rtt: the RTT data
                @type hops: number
                @param hops: number of hops
                @type tm: number
                @param tm: the timestamp of the received packet containing the rtt and hops data                
                """
                if(self.check_chip(addr) != 1):
                        LOGMSG('Chip not in database! Adding...')
                        self.add_chip(addr)
                try:
                        self.cursor.execute("insert into stats values(?, ? , ? , ?)", (addr,rtt,hops,tm))
                        self.commit()
                        
                except sqlite3.OperationalError:
                        LOGMSG('Table not found!')
        
######################################################################################################################

################################################# ADD ################################################################

        def add_chip( self , address, comment='' ):
                """
                Adds new sensor device to the database
                    - Checks if device already exists
                @type address: string
                @param address: the address of the device
                @type comment: string
                @param comment: user comments regarding the chip
                """
                if(self.check_chip(address) == 1):
                        LOGMSG('Chip already stored in the database!')
                else:
                        self.cursor.execute('select max(id) from chip')
                        query = self.cursor.fetchone()
                        if(query[0] == None):
                        	res = 0
                        else:
                                res = query[0]
                        self.cursor.execute("""insert into chip values ( ? , ? , 1 , ? , null, null, 0)""" , ( (res+1)  , address , comment))
                        self.commit()

        

        def add_to_group(self, addr, group_id,x,y):
                """
                Adds a device with address addr to group group_id
                @type addr: string
                @param addr: the device address
                @type group_id: number
                @param group_id: the id of the group
                @type x: number
                @param x: the x-coordinate of the device
                @type y: number
                @param y: the y-coordinate of the device
                """
                if(self.group_exists(group_id) == 0):
                        LOGMSG('Group '+str(group_id)+' has not been created! Creating and adding the chip to the group...')
                        self.create_group(group_id)
                
                self.cursor.execute('update chip set group_id=? where chip.address=?',(group_id,addr))
                self.cursor.execute('update chip set x=? , y=? where chip.address=?',(x,y,addr))
                self.commit()


        
######################################################################################################################
                
################################################# GET ################################################################

        def get_active_chips(self,group_id=None):
                """
                Retrieves all sensor devices which are active (their status is 1).
                @type group_id: number.
                @param group_id: the group id. If none specified, the query will execute on all devices in the database
                @rtype: array
                @return: an array of active devices
                """
                query = []
                if(group_id == None):
                        query = self.cursor.execute('select address from chip where status=1').fetchall()
                else:
                        query = self.cursor.execute('select address from chip where status=1 and group_id=?',(group_id)).fetchall()
                result = []
                for i in query :
                    result.append(i[0])
                return result
    

        def get_chip_coordinates(self, addr):
                """
                Retrieves the (x,y)-coordinates of a specified device
                @type addr: string
                @param addr: the address of the device
                @rtype: array
                @return: the (x,y)-coordinates of the devices
                """
                self.cursor.execute('select chip.x, chip.y from chip where chip.address=?',(addr,))
                res = self.cursor.fetchone()
                return (res[0], res[1])


        def get_chips_xml(self,group_id):
                """
                Queries for all devices belonging to a specified group and returns an xml file
                @type group_id: number
                @param group_id: the group id
                @rtype: string
                @return: the xml-parsed result of the query
                """
                xml = XMLGenerator()
                query = []
                query = self.cursor.execute('select * from chip where group_id=?',(group_id,)).fetchall()
                group_name = self.get_group_name(group_id)
                if(group_name!=''):
                        xml.add_group(group_id,group_name)
                        for x in query:
                                if(str(x[6]) == group_id):
                                        xml.add_new_board(x[1],'test',x[2],x[4],x[5])
                        
                return xml.print_xml()

        def get_chips(self, group_id = None):
                """
                Queries for all devices belonging to a specified group
                @type group_id: number
                @param group_id: the group id. If no group specified, the query will execute on all groups
                @rtype: array
                @return: an array containing the addresses of all stored devices
                """
                query = []
                if(group_id == None):
                        query = self.cursor.execute('select address from chip').fetchall()
                else:
                        query = self.cursor.execute('select address from chip where group_id=?',(group_id,)).fetchall()
                res = []
                for x in query:
                        res.append(x[0])
                return res
                
                
        def get_avg_temps(self):
                """
                Retrieves the averaged temperature measurements per device
                @rtype: array
                @return: an array of all device and their measured average temperature
                """
                query = self.cursor.execute('select chip_addr, avg(value) from temp group by chip_addr').fetchall()
                return query

        def get_unassigned_chips(self):
                """
                Returns a list of all devices that have not been assigned to a group
                @rtype: array
                @return: an xml file containing the addresses of devices which belong to group 0 ('unassigned')
                """
                query = self.cursor.execute('select distinct address from chip where group_id=0').fetchall()
                res = []
                i = 0
                xml = XMLGenerator()
                while(i<len(query)):
                        xml.add_unassigned_board(str(query[i][0]))
                        i+=1
                return xml.print_xml()

        def get_measurements(self,table,start,end,step,group_id=None,boards=[]):
                """
                Returns an xml file containing the averaged sensor readings from a specified group or device for a given period of time
                    - If no devices specified, queries all devices that have provided sensor readings
                    - if no group specified, queries all groups
                @type table: string
                @param table: the type of sensor measurements to be queried
                @type start: number
                @param start: the start time of the period which is queried
                @type end: number
                @param end: the end time of the period which is queried
                @type step: number
                @param step: the number of steps at which to average the data. 
                @type group_id : number
                @param group_id: the group which will be queried
                @type boards: array
                @param boards: the devices which will be queried for data
                @rtype: string
                @return: an xml file containing the results of the query                
                """
                xml = XMLGenerator()
                if(self.check_table_name(table)==1):
                        tm = round((end-start)/step)
                        if(boards==[]):
                                chips = self.get_chips()
                                query = []
                                for x in chips:
                                        if(group_id == None):
                                                query = self.cursor.execute('select avg(value),(ROUND(time/?)) as gtime, time from '+table+',chip where chip_addr=? and chip.address='+table+'.chip_addr and time>? and time<? group by (gtime)',(tm,x,start,end)).fetchall()
                                        else:
                                                query = self.cursor.execute('select avg(value),(ROUND(time/?)) as gtime, time from '+table+',chip where chip_addr=? and chip.group_id=? and chip.address='+table+'.chip_addr and time>? and time<? group by (gtime)',(tm,x,group_id,start,end)).fetchall()
                                        xml.add_new_board(x,table)
                                        for y in query:
                                                xml.add_board_value(table,y[0],y[2])
                                                                                    
                        else:
                                query = []
                                for x in boards:
                                        #Query for a single board
                                        if(group_id == None):
                                                query = self.cursor.execute('select avg(value),(ROUND(time/?)) as gtime, time from '+table+',chip where chip_addr=? and chip.address='+table+'.chip_addr and time>? and time<? group by (gtime)',(tm,x,start,end)).fetchall()
                                        else:
                                                query = self.cursor.execute('select avg(value),(ROUND(time/?)) as gtime, time from '+table+',chip where chip_addr=? and chip.group_id=? and chip.address='+table+'.chip_addr and time>? and time<? group by (gtime)',(tm,x,group_id,start,end)).fetchall()
                                        xml.add_new_board(x,'temp')
                                        for y in query:
                                                xml.add_board_temp(y[0],y[2])                                       
                        
                return xml.print_xml()


        def get_times(self,chip_id,table):
                """
                Returns the first and last entry of a specified device in a sensor measurements table
                @type chip_id: string
                @param chip_id: the address of the device
                @type table: string
                @param table: the table containing sensor readings
                @rtype: string
                @return: an xml file containing the query result
                """
                xml = XMLGenerator()
                if(self.check_table_name(table)==1 and self.check_chip(chip_id)==1):
                        query = self.cursor.execute('select min(time),max(time) from '+table+' where chip_addr=?',(chip_id,)).fetchone()
                        xml.add_new_data_element(str(chip_id),('mintime','maxtime'),(query[0],query[1]))
                return xml.print_xml()

        def get_groups(self, xml_stat):
                """
                Returns a list of the existing groups
                @type xml_stat: number
                @param xml_stat: flag for xml parsing. If 1, create an xml file with the results. Otherwise, do not create an xml file
                @return: a list (in xml format or not) containing all existing groups
                """
                if(xml_stat == 1):
                        xml = XMLGenerator()
                query = self.cursor.execute('select * from chip_group').fetchall()
                if(xml_stat == 1):
                        for x in query:
                                xml.add_group(x[0],x[1])
                        return xml.print_xml()
                else:
                        res = []
                        for x in query:
                                res.append((x[0],x[1]))
                        return res

        def get_group_name(self, group_id):
                """
                Returns the name of the group corresponding to its id
                @type group_id: number
                @param group_id: the id of the group
                @rtype: string
                @return: the name of the group corresponding to its group_id
                """
                query = self.cursor.execute('select name from chip_group where id=?',(group_id,)).fetchone()
                if(query != None):
                        return query[0]
                else:
                        return ''

        def get_group_id(self,group_name):
                """
                Returns the id of a group given by its name
                @type group_name: string
                @param group_name: the name of the group
                @rtype: number
                @return: the group id
                """
                query = self.cursor.execute('select id from chip_group where name=?',(group_name,)).fetchone()
                if(query != None):
                        return query[0]
                else:
                        return -1

        def get_user_settings(self, name):
                """
                Returns stored user settings
                @type name: string
                @param name: the name of the settings as stored in the database
                @rtype: array
                @return: the user settings corresponding to the given name
                """
                query = self.cursor.execute('select * from user_settings where name=?',(name,)).fetchone()
                return query
                        
######################################################################################################################

############################################### CREATE ###############################################################
		
		
		def create_user_settings(self, _id, name, maxtemp, mintemp):
              
				self.cursor.execute('insert into user_settings values(?,?,?,?)',(_id,name,maxtemp,mintemp))
				self.commit()
                
                                      
        def create_group(self,name,_id=None, map_path=''):
                """
                Creates a new group
                @type name: string
                @param name: the name of the group
                @type _id: number
                @param _id: the id of the group
                @type map_path: string
                @param map_path: the logical path to the map file corresponding to the group\
                """
                
                if(_id != None):
                        try:
                                self.cursor.execute('insert into chip_group values(? , ?, ?)',(_id, name , map_path))
                        except sqlite3.IntegrityError:
                                LOGMSG('Invalid values selected!')
                else:
                        try:
                                self.cursor.execute('insert into chip_group ( name , map_path ) values(?, ?)',(name,map_path))
                        except sqlite3.IntegrityError:
                                LOGMSG('Chosen name is not unique!')
                self.commit()
                     

######################################################################################################################

############################################## DELETE ################################################################

        def del_chip ( self, chip_addr):
                """
                Deletes a device entry
                @type chip_addr: string
                @param chip_addr: the address of the device
                """
                if(self.check_chip(chip_addr) == 1):
                        
                        self.cursor.execute( """delete from chip where address=? """, (chip_addr,) )
                        self.commit()
                        
                else:
                        LOGMSG('Chip with address '+chip_addr+' not in the database!')
                

        def del_group(self,name):
                """
                Deletes a group
                @type name: string
                @param name: the name of the group
                """
                self.cursor.execute('update chip set group_id=0 where group_id in (select group_id from chip_group where name=?)',(name,))
                self.cursor.execute('delete from chip_group where name=?',(name,))
                self.commit()
                

        def del_from_group(self,chip_addr):
            """
            Removes the group belonging of a device and sets it to default
            @type chip_addr: string
            @param chip_addr: the address of the device
            """
                
            self.cursor.execute('update chip set group_id=0 where address=?',(chip_addr,))
            self.commit()
                
                                
###################################################################################################################### 
        
################################################# CHECK ##############################################################

        def check_db(self):
                """
                Checks the database for inconsistencies
                    - Checks the number of tables: if not equal to the number of tables in 'tables' parameter, then database inconsistent
                    - Deletes the database if inconsistent
                """
                self.cursor.execute('select * from sqlite_master')
                dbs = self.cursor.fetchall()
                if(len(dbs) != 9):
                        LOGMSG("Database is not consistent! Deleting...")
                        i = 0
                        for i in dbs:
                                if(i[0] != 'index'):
                                        self.cursor.execute('drop table '+i[1])
                        LOGMSG("Creating new database...")
                        
                        self.cursor.execute('create table chip_group(id Integer primary key, name Varchar(30) unique,map_path Varchar(70))')
                        self.cursor.execute('create table chip(id Integer primary key, address Varchar(20), status Integer, comment Varchar(30), x Integer, y Integer, group_id Integer refereces chip_group)')
                        self.cursor.execute('create table temp(chip_addr Varchar(20) references chip, value Float, time Integer)')
                        self.cursor.execute('create table light(chip_addr Varchar(20) references chip, value Float, time Integer)')
                        self.cursor.execute('create table humidity(chip_addr Varchar(20) references chip, value Float, time Integer)')
                        self.cursor.execute('create table pressure(chip_addr Varchar(20) references chip, value Float, time Integer)')
                        self.cursor.execute('create table user_settings(id Integer primary key, name Varchar(20), max_temp Float, min_temp Float)')
                        self.cursor.execute('create table stats(chip_addr Varchar(20) references chip, rtt Integer, hops Integer, time Integer)')
                        self.create_user_settings(0,'Default',23,15)
                        self.create_group('unassigned',0)
                        self.commit()
                else:
                        LOGMSG("Database consistent")
				

        def check_chip(self, chip_addr):
            """
            Checks if a device exists in database
            @type chip_addr: string
            @param chip_addr: the address
            @rtype: number
            @return: 0 if devices does not exist, 1 otherwise
            """
            self.cursor.execute('select * from chip where chip.address=?',(chip_addr,))
            query = self.cursor.fetchall()
            if(query == []):
                return 0
            else:
                return 1
        	
        def check_status(self,addr):
                """
                Checks the status of a specified device
                @type addr: string
                @param addr: the address of the device
                @rtype: number
                @return: the status of the device, or -1 if the device does not exist in the database
                """
                if(self.check_chip(addr) == 1):
                        self.cursor.execute('select status from chip where chip.address=?',(addr,))
                        res = self.cursor.fetchone()
                        return res[0]
                else:
                        LOGMSG("Chip not in database!")
                        return -1

        

        def group_exists(self,group_id):
                """
                Checks if a specified group exists in the database
                @type group_id: number
                @param group_id: the id of the group
                @rtype: number
                @return: 0 if group does not exist, 1 otherwise
                """
                query = self.cursor.execute('select * from chip_group where chip_group.id=?',(group_id,)).fetchone()
                if(query == None):
                        return 0
                else:
                        return 1


        

        def check_duplicate_data(self,table,addr, value,time):
                """
                Checks for duplicate entries in database
                    - Sensor table entries are in the form (device address, measurement, time)
                @type table: string
                @param table: the table to be checked for duplicate data
                @type addr: string
                @param addr: the address of the device to be checked for duplicate data
                @type value: number
                @param value: the sensor measurement
                @type time: number
                @param time: the timestamp
                @rtype: number
                @return: 0 if no duplicates corresponding to provided entry, 1 otherwise
                """
                if(self.check_table_name(table)==1):
                        query = self.cursor.execute('select * from '+table+' where chip_addr=? and value=? and time=?',(addr,value,time)).fetchall()
                        if(query==[]):
                                return 0
                        else:
                                return 1
                else:
                        return 1

        
        def check_table_name(self,table):
                """
                Checks if the specified table name is valid (contained in the 'tables' variable)
                @type table: string
                @param table: the table name to be checked
                @rtype: number
                @return: 1 if table name is valid, 0 otherwise
                """
                if(table in self.tables):
                        return 1
                else:
                        return 0
######################################################################################################################
