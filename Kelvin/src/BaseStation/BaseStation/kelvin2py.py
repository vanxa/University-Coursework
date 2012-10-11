#!/usr/bin/env python

import re


f = open( 'kelvin_protocol.h' )
fo = open( 'kelvin_protocol.py' , 'w' )

headerDefine = re.compile('#define\s+KELVIN_(\w+)\s+(\w+)$')

fo.write( '#!/usr/bin/env python\n' )
fo.write( 'class kelvin:\n' )

for line in f:
    if( headerDefine.match( line ) ):
        print( "Line matches: " , line )
        fo.write( headerDefine.sub( '\t\g<1> = \g<2>' , line ) )

f.close()
fo.close()
    
