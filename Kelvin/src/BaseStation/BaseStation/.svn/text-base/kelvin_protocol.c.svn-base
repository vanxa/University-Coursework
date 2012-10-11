#include "kelvin_protocol.h"
#include "dynmem.h"
#include "serial.h"

void deserialize_address( kpacket_t *pkt , char *radio_data )
{
	// TAKE HW ADDR
	m_memcpy( (void *) pkt->address.addr , (void *) radio_data , XLOWPAN_ADDR_LEN );
}

void serialize_address( kpacket_t *pkt , char *radio_data )
{
	// TAKE HW ADDR
	m_memcpy( (void*) radio_data , (void*) pkt->address.addr , XLOWPAN_ADDR_LEN );
}

void deserialize_packet( kpacket_t *pkt , char *radio_data )
{	
	// Fill fields
	pkt->dispatch = *(radio_data);
	pkt->is_cmd = *(radio_data+1) & KELVIN_CMD;
	pkt->type = *(radio_data+1) & (~KELVIN_CMD);
	
	pkt->data_len = *(radio_data+2);
	
	pkt->data = (radio_data+3);
}

size_t serialize_packet( kpacket_t *pkt , unsigned char *buffer )
{	
	// Serialize fields
	//Dispatch
	*buffer = pkt->dispatch;
	
	//Command/response + type
	*(buffer+1) = (pkt->is_cmd | pkt->type);
	
	//Data length
	*(buffer+2) = pkt->data_len;
	
	//Copy data
	m_memcpy( (void*) (buffer+3) , (void*) pkt->data , pkt->data_len );

	return pkt->data_len + KELVIN_PACKET_HEADER_SIZE;
}