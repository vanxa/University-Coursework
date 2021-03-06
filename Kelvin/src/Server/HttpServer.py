#!/usr/bin/env python
"""
The HTTP server module. Takes care of serving user requests and issuing data requests to database
    - initializes all other back-end modules, and closes them afterwards
    - serves forever until terminated by the user
"""
import sys
from http.server import HTTPServer, SimpleHTTPRequestHandler
import time
import sqlite3
from Serial_conn import Serial_conn
from queue import PriorityQueue
from DBE import DBE
from Server_config import *

from xml.dom.minidom import Document
PORT=6789
"""
The default Port number which the server uses to listen to user requests
"""
serial = None
"""
Instance of the SComms module
"""

class Handler( SimpleHTTPRequestHandler ):
        """
        The Handler class, used to handle user requests.
        """
        
        def get_params( self ):
                """
                Parses the URL given by the web-based frontend, and extracts the parameters
                @rtype: array
                @return: a list of parameters
                """
                params = {}
                pairs = []
                query = self.path.split('?',1)
                if len(query) == 2:
                        pairs = query[1].split('&');

                for pair in pairs:
                        (key,value) = pair.split('=')
                        params[key] = value

                return params

        def errorDoc( self , errorText ):
                """
                Creates a custom error document if the request is not recognised
                @type errorText: string
                @param errorText: specifies the text to be printed on the error page
                """
                d = Document()
                r = d.createElement('kelvin')
                e = d.createElement('error')
                e.appendChild( d.createTextNode( str(errorText) ) )
                r.appendChild( e )
                d.appendChild( r )
                self.wfile.write( d.toxml().encode() )
        
        def do_GET( self ):
                """
                Custom handler for user requests
                
                All URL requests start with the '/data' delimeter
                """
                if self.path.startswith( '/data' ):
                        #Split HTTP GET parameters
                        self.params = self.get_params()
                                
                        self.send_response( 200 )
                        self.send_header( 'Content-type' , 'text/xml' )
                        self.end_headers()

                        try:

                                #Temperature request
                                if( self.params["type"] == 'data' ):
                                        action = self.params["action"]
                                        if(action =='display'):
                                                req_queue.put((0,db.get_measurements,(self.params['sensor'],int(self.params['start']),int(self.params['end']),int(self.params['step']))))
                                                res = res_queue.get()
                                                self.wfile.write(res[1].encode())
                                        elif(action == 'getminmax'):
                                                req_queue.put((0,db.get_times,(self.params['chip_id'],self.params['data'])))
                                                res = res_queue.get()
                                                self.wfile.write(res[1].encode())       

                                elif(self.params["type"] == 'chips' ):
                                        req_queue.put((0,db.get_chips_xml,(self.params['gid'],)))                       
                                        res = res_queue.get()
                                        self.wfile.write(res[1].encode())

                                elif(self.params["type"] == 'group' ):
                                    action = self.params["action"]
                                    if(action == 'create'):
                                            req_queue.put((0,db.create_group,(self.params["name"],)))
                                    elif(action == 'delete'):
                                            req_queue.put((0,db.del_group,(self.params["name"],)))
                                    elif(action == 'assign'):
                                            req_queue.put((0,db.add_to_group,(self.params['chip'],self.params['group'],self.params['x'],self.params['y'])))
                                    elif(action == 'list'):
                                             req_queue.put((0,db.get_groups,(1,)))
                                             res = res_queue.get()
                                             self.wfile.write(res[1].encode())

                                elif(self.params["type"] == 'settings'):
                                        action = self.params["action"]
                                        if(action == 'set'):
                                                max_temp = self.params["max"]
                                                min_temp = self.params["min"]
                                                serial.sensor.set_user_settings(max_temp,min_temp)
                                        elif(action == 'get'):
                                                req = serial.sensor.get_user_settings()
                                                self.wfile.write(req.encode())                                        
                                else:
                                        self.errorDoc( "Unknown request type" )
                                        
                                return
                        except KeyError:
                                self.errorDoc( "Bad request parameter" );
                elif self.path.startswith( '/put' ):
                        DEBUG( "putting data.." )
                else:
                        #in this case just do default request
                        SimpleHTTPRequestHandler.do_GET( self )

if __name__ == "__main__":
        """
        Initializes the Server and starts all back-end componens
            - If no port is specified, uses default
            - creates two PriorityQueue objects for communication with the database
            - initializes and starts the database
            - user may choose to run in debug mode: in this case the Serial_conn and the Sensor_thread are not started
            - serves until terminated by user: on shutdown, closes all system components
        """
        if len(sys.argv) > 1:
                PORT=int(sys.argv[1])

        # Init queues
        req_queue = PriorityQueue()
        res_queue = PriorityQueue()

        # Init database
        db = DBE(req_queue,res_queue)
        db.start()

        cont = 0
        while(not cont):
                dbg = input("Please enter operation mode: Normal(1) or Debug(2)\n")
                if(dbg=='1' or dbg=='2'):
                        cont  = 1
        if(dbg=='1'):                
                # Init serial connection to base station
                serial = Serial_conn()
                if(serial.sensor != None):
                        sensor = serial.sensor
                        serial.sensor.set_db_data(req_queue,db)

        httpd = HTTPServer(('',PORT),Handler)
        LOGMSG("Server started. Serving at port "+ str(PORT))
                
        try:
                """
                Serve until interrupted
                """
                httpd.serve_forever()
        except KeyboardInterrupt:
                if(serial != None):
                        serial.close()
                req_queue.put((0,db.shutdown))
                httpd.server_close()
