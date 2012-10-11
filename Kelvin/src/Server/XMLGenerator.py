from xml.dom.minidom import Document
from struct import *

class XMLGenerator:
    """
    Generates an xml document to be sent to the web-based frontend.
    """
    def __init__(self):
        """
        Creates a new document and attaches a root node called "kelvin"
        """
        self.doc = Document()
        self.root = self.doc.createElement('kelvin')
        self.doc.appendChild(self.root)

    def print_xml(self):
        """
        Outputs the xml document
        """
        return self.doc.toprettyxml()

    def add_unassigned_board(self,addr):
        """
        Creates a new node corresponding to an unassigned device.
            - If no parent node has been specified, creates a new parent 'unassigned boards'.
        @type addr: string
        @param addr: the address of the sensor device
        """
        try:
            self.data_element
        except AttributeError:
            self.data_element = self.doc.createElement('unassigned_boards')
            self.root.appendChild(self.data_element)
        child = self.doc.createElement('board')
        child.setAttribute('id',addr)
        self.data_element.appendChild(child)


    def add_board_value(self,table,temp,time):
        """
        Attaches a sensor data entry to the document
        """
        child = self.doc.createElement('time')
        child.setAttribute('value',str(time))
        self.current_node.appendChild(child)
        text = self.doc.createTextNode(str(temp))
        child.appendChild(text)


    def add_new_board(self,board,_type,state=None,x='',y=''):
        """
        Adds a new device node
        """
        try:
            self.data_element
        except:
            self.data_element = self.doc.createElement(_type)
            self.root.appendChild(self.data_element)
        self.current_node = self.doc.createElement('board')
        self.current_node.setAttribute('id',board)
        if(state != None):
            st = ''
            if state == 1:
                st = 'active'
            else:
                st = 'inactive'
            self.current_node.setAttribute('state',st)
            self.data_element.appendChild(self.current_node)
        if(x != ''):
            if(x == None):
                self.current_node.setAttribute('x','unspecified')
            else:
                self.current_node.setAttribute('x',str(x))
        if(y != ''):
            if(y == None):
                self.current_node.setAttribute('y','unspecified')
            else:
                self.current_node.setAttribute('y',str(y))
        self.data_element.appendChild(self.current_node)
             
    def add_group(self,_id,name):
        """
        Adds a new group node
        """
        self.data_element = self.doc.createElement('group')
        self.data_element.setAttribute('id',str(_id))
        self.data_element.setAttribute('name',str(name))
        self.root.appendChild(self.data_element)
        

    def add_new_data_element(self,name,attributes,values):
        """
        Adds a generic data element
        @param name: name of the element
        @param attributes: a list of attributes corresponding to the element
        @param values: a list of values corresponding to the element's attributes. Size must be equal to the size of the attribtes list
        """
        if(len(attributes) != len(values)):
            return
        try:
            self.data_element
        except:
            self.data_element = self.doc.createElement(name)
            self.root.appendChild(self.data_element)
        i = 0
        while(i<len(attributes)):
            self.data_element.setAttribute(attributes[i],str(values[i]))
            i += 1

    def add_user_setting(self,setting,value):
        """
        Adds a new user settings node
        """
        child = self.doc.createElement(setting)
        val  = self.doc.createTextNode(str(value))
        child.appendChild(val)
        self.root.appendChild(child)
