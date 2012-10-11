#!/usr/bin/env python
"""
This module contains all command values, taken from kelvin_protocol.h header file
"""
class kelvin:
	DISPATCH = 0x67
	CMD = 0x80
	RSP = 0x00
	ERR = 0x7f
	CONN_REQ = 0x01
	LED = 0x02
	TEMP = 0x03
	PRSR = 0x04
	HUM = 0x05
	LGHT = 0x06
	PING = 0x07
	SERIAL_SYNC1 = 0xBE
	SERIAL_SYNC2 = 0xEF
	RESET = 0x7e
	PACKET_HEADER_SIZE = 3
