#!/usr/bin/env python

import re

def export():
    """
    Exports kelvin_protocol.h file to kelvin_protocol.py
    """
    f = open('kelvin_protocol.h','r')
    fo = open('kelvin_protocol.py','w')

    p_define = re.compile('(#define\s*)KELVIN_(\w*)\s*(\w*)')

    fo.write('#!/usr/bin/env python\r\r')
    fo.write('class kelvin:\r\r')

    for line in f:
        match = re.match(p_define,line)
        if(match != None):
            res = p_define.sub('\t\g<2> = \g<3>',match.group())+'\n'
            fo.write(res)

    fo.close()
    f.close()
