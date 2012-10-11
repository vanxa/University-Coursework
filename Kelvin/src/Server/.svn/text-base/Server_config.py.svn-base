#!/usr/bin/env python

LOG_LEVEL = 'L'

# LOGLEVEL: - for no messages, L for activity messages, D for debug messages, L+D for all messages
if LOG_LEVEL == '-':
        def DEBUG(msg): None
        def LOGMSG(msg): None
        
elif LOG_LEVEL == 'L':
        def LOGMSG(msg): print(msg)
        def DEBUG(msg): None

elif LOG_LEVEL == 'D':
        def LOGMSG(msg): None
        def DEBUG(msg): print(msg)

elif LOG_LEVEL == 'L+D':
        def LOGMSG(msg): print(msg)
        def DEBUG(msg): print(msg)
