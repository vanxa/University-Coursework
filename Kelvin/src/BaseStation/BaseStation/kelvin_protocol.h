// Remember to run kelvin2py.py file to update kelvin protocol defines for python


#ifndef __KELVIN_PROTOCOL_H__
#define __KELVIN_PROTOCOL_H__

#include <stdlib.h>
#include "xlowpan.h"


#define KELVIN_DISPATCH 0x67

#define KELVIN_CMD 0x80
#define KELVIN_RSP 0x00

#define KELVIN_ERR		0x7f
#define KELVIN_CONN_REQ	0x01
#define KELVIN_LED		0x02
#define KELVIN_TEMP		0x03
#define KELVIN_PRSR 	0x04
#define KELVIN_HUM		0x05
#define KELVIN_LGHT		0x06
#define KELVIN_PING		0x07


#define KELVIN_SERIAL_SYNC1		0xBE
#define KELVIN_SERIAL_SYNC2		0xEF

#define KELVIN_RESET	0x7e

#define KELVIN_PACKET_HEADER_SIZE	3

typedef struct
{
	unsigned char dispatch;
	unsigned char type;
	unsigned char data_len;
	unsigned char *data;
	
	/* not part of packet payload */
	struct xlowpan_addr64 address;
	unsigned char is_cmd;
} kpacket_t;

void deserialize_address( kpacket_t *pkt , unsigned char *radio_data );
void serialize_address( kpacket_t *pkt , unsigned char *radio_data );

void deserialize_packet( kpacket_t *pkt , unsigned char *radio_data );
size_t serialize_packet( kpacket_t *pkt , unsigned char *buffer );

#endif
