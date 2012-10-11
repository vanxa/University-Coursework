
/* PSoc port defintions */
#include "psocapi.h"
#include "psocgpioint.h"

#define OVERRIDE_MALLOC
#include "config.h"
#include "dynmem.h"
#include "kelvin_protocol.h"
#include "xlowpan.h"
#include "timing.h"
#include "base64.h"

#include "serial.h"

/* reuse the payload buffer from main. this is safe as the code that uses
 * payload is only executed in sequence. DO NOT DO THIS WITH POTENTIALLY THREADED CODE! */
extern unsigned char payload[PAYLOAD_BUFFER_SIZE];

void serial_init(void)
{	
	// buffer
	UART_IntCntl(UART_ENABLE_RX_INT); // Enable RX interrupts
	Counter8_WritePeriod(155); // Set up baud rate generator
	Counter8_WriteCompareValue(77);
	Counter8_Start(); // Turn on baud rate generator
	UART_Start(UART_PARITY_NONE); // Enable UART
	
	serial_cprint(":: Welcome to Kelvin [v0.8.0] ::");
}

void serial_cprint(const unsigned char *str)
{
	UART_CPutString(str);
	UART_CPutString("\r\n");
}

void serial_print(unsigned char *str)
{
	UART_PutString(str);
	UART_CPutString("\r\n");
}

static void _serial_put_char(char c, void *param)
{
	UART_PutChar(c);
}

void serial_send_pkt(unsigned char* buf, size_t len)
{
	/* put non-base64 char to indicate base64 encoding */
	UART_PutChar('!');
	base64_encode_callback(buf, len, _serial_put_char, NULL);
	UART_PutCRLF();
}

void serial_send_addr_payload(struct xlowpan_addr64 *addr, unsigned char *buf, size_t buf_len)
{
	/* put non-base64 char to indicate base64 encoding */
	UART_PutChar('!');
	base64_encode_callback(addr->addr, XLOWPAN_ADDR_LEN, _serial_put_char, NULL);
	/* put non-base64 char to indicate base64 encoding */
	UART_PutChar('!');
	base64_encode_callback(buf, buf_len, _serial_put_char, NULL);
	
	UART_PutCRLF();
}

/* serial_start -> serial_main */
void serial_main(void)
{
	unsigned char *buffer;
	kpacket_t packet;

    if(UART_bCmdCheck()) { // Wait for command
		LOGMSG("serial: received message from server");
		
        if(buffer = UART_szGetParam()) { // More than delimiter"
			/* check if Base64 */
			if(buffer[0] == '!') {
				base64_decode(payload, buffer, PAYLOAD_BUFFER_SIZE);
				buffer = payload;
			}
			
			LOGMSG("serial: packet deserialization started");
			
            // TAKE HW ADDR
            deserialize_address( &packet , buffer );
			//Fetch packet contents
            deserialize_packet( &packet , buffer + XLOWPAN_ADDR_LEN );
			
			LOGMSG("serial: packet deserialization ended");
			
            //Check for 'Dispatch'
			if( packet.dispatch == KELVIN_DISPATCH ) {
				
				if( packet.is_cmd && packet.type == KELVIN_CONN_REQ ) {
						LOGMSG("serial: packet_type is conn_req");
						
						packet.is_cmd = KELVIN_RSP;
                        packet.type = KELVIN_CONN_REQ;
						
						serial_send_pkt(payload, serialize_packet(&packet, payload));
                } else if(packet.is_cmd && packet.type == KELVIN_RESET){
					LOGMSG("serial: received RESET request. Resetting...");
					M8C_Reset;
				} else if ( packet.is_cmd ) {
					LOGMSG("serial: packet is a command");
                    //Otherwise if command it should be forwarded
					
					/* re-serialize payload, before directly writing to payload.data */
                    serialize_packet( &packet , payload + XLOWPAN_ADDR_LEN );

					/* payload.data now points to usable memory address (in payload) */
					if(packet.type == KELVIN_PING) {
						unsigned long current_time = sys_millis();
						packet.data_len = sizeof(unsigned long);
						memcpy(packet.data, &current_time, sizeof(unsigned long));
					}
					
					xlowpan_send(&packet.address, payload + XLOWPAN_ADDR_LEN,
									KELVIN_PACKET_HEADER_SIZE + packet.data_len);

					LOGMSG("serial: xlowpan_send done");
				}
            } else {
        		//Otherwise ASCII send failure
                serial_cprint( "BAD DISPATCH" );
			}
        }
		
        UART_CmdReset();
    }
}
