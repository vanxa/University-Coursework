/*
 * BaseStation: Acts as base station, but also as nodes (TODO: rename?)
 *
 * Author: Group C
 * Date: October 15, 2010
 * 
 * Based on code from Steven Wong, Matthew Barnes and James Mathews
*/

/* PSoc port defintions */
#include "psocapi.h"
#include "psocgpioint.h"

/* Prospeckz api */
#include "prospeckz.h"
#include "config.h"

/* our libs */
#define OVERRIDE_MALLOC
#include "sensors.h"
#include "dynmem.h"
#include "xlowpan.h"
#include "mac_driver.h"
#include "serial.h"
#include "kelvin_protocol.h"
#include "thresholds.h"
#include "timing.h"

/* Packet to send and receive */
kpacket_t rx_packet;
kpacket_t tx_packet;

/* local variables */

unsigned char payload[PAYLOAD_BUFFER_SIZE];

static unsigned char block0[MEM_BLOCK_SIZE];
static unsigned char block1[MEM_BLOCK_SIZE];
//static unsigned char block2[128];

/* local setup routines */

static void setup_prs8(void)
{
	PRS8_WritePolynomial(PRS_POLY);
	PRS8_WriteSeed(PRS_SEED);
	PRS8_Start();
}

static void setup_counters(void)
{
	Counter16_Start();
	Counter8_DC_Start();
	
#if 0
	Counter16_EnableInt();
#endif
	//Counter8_DC_EnableInt();
}

static void chk_mem(void) 
{
	UART_CPutString("[ dynmem available = ");
	{
		char buf[5];
		utoa(buf, dynmem_avail(), 10);
		UART_PutString(buf);
	}
	UART_CPutString(" bytes ]\r\n");
}


/* local core routines */

static void recv_payload(void)
{
	unsigned char hops_travelled;
	enum xlowpan_addr_type dst_type;
	
	/* maybe there was a packet for us, consume if there was,
	BTW: xlowpan_recv returns number of bytes written into payload. */
	while(xlowpan_recv5(&rx_packet.address, &dst_type, &hops_travelled, payload, PAYLOAD_BUFFER_SIZE)) {
		LOGMSG("main: xlowpan received packet");
		if(!xlowpan_addrcmp(&rx_packet.address, xlowpan_getaddr())) {
			LOGMSG("main: xlowpan_addrcmp returns false");
			continue;
		}
		
		//Test to see if the dispatch matches
		if( payload[0] != KELVIN_DISPATCH )
		{
			UART_PutSHexByte(payload[0]);
			UART_PutCRLF();
			LOGMSG("main: bad dispatch");
			continue;
		}
		
		//Dispatch matches
		deserialize_packet( &rx_packet , payload );
		
		if( rx_packet.is_cmd && rx_packet.type == KELVIN_LED && rx_packet.data_len == 1 )
		{
			//Need to test payload RX'd
			LED_Data_ADDR = *rx_packet.data & 0x07;
			//LED_Data_ADDR ^= 0x06;
		} else if ( rx_packet.is_cmd )
		{
			unsigned char packet_data[2];

			LOGMSG("main: rx_packet is command");

			/* New! Read SHT temperature */
						
			/* defaults, may be overridden below */
			tx_packet.dispatch = KELVIN_DISPATCH;
			tx_packet.is_cmd = KELVIN_RSP;
			tx_packet.type = rx_packet.type;
			tx_packet.data = packet_data;
		
			if(rx_packet.type == KELVIN_TEMP) {
				LOGMSG("main: request is for temp");
				tx_packet.data_len = 2;
				sht_read_value( packet_data , packet_data+1 , 't' );
				
				//If returns 0 then no SHT connected (or very low reading) so ignore
				if( !( *packet_data | *(packet_data+1) ) ) {
					LOGMSG("main: temperature value is 0. Not sending packet");
					continue;
				}
			}
			else if(ENABLE_A_PRESSURE && rx_packet.type == KELVIN_PRSR) {
				LOGMSG("main: request is for pressure");
				
				tx_packet.data_len = 1;
				packet_data[0] = read_pressure();
			}
			else if(rx_packet.type == KELVIN_HUM) {
				LOGMSG("main: request is for humidity");
				
				tx_packet.data_len = 2;
				sht_read_value(packet_data, packet_data+1 , 'h');
				
				//If returns 0 then no SHT connected (or very low reading) so ignore
				if( !( *packet_data | *(packet_data+1) ) ) {
					LOGMSG("main: humidity value is 0. Not sending packet");
					continue;
				}
			}
			else if(ENABLE_A_LIGHT && rx_packet.type == KELVIN_LGHT) {
				tx_packet.data_len = 1;
				packet_data[0] = read_light();
			}
			else if(rx_packet.type == KELVIN_PING) {
				tx_packet.data_len = rx_packet.data_len;
				tx_packet.data = rx_packet.data;
			} else {
				// Don't respond
				continue;
			}

			//Serialize for sending
			serialize_packet( &tx_packet , payload );
						
			/* this is to prevent flooding in the network! */
			if(dst_type == XLOWPAN_ADDR_TYPE_BCAST) {
				LOGMSG("main: xlowpan address is BCAST");
				//serial_cprint("Sleeping! Don't flood the network!");
				sleep_millis((HW_ADDR*5)%1000);
				//serial_cprint("Wake up! Send the packet....");
			}

			LOGMSG("main: xlowpan_send");

			//Send to basestation
			xlowpan_send( &rx_packet.address, payload , PAYLOAD_BUFFER_SIZE );
		} 
		else if ( ! rx_packet.is_cmd ) {
			if(rx_packet.type == KELVIN_PING) {
				/* preprocess data before forwarding to payload */
				unsigned long rtt;
				memcpy(&rtt, rx_packet.data, sizeof(unsigned long));
				rtt = sys_millis() - rtt;
				memcpy(rx_packet.data, &rtt, sizeof(unsigned long));
				*(rx_packet.data+sizeof(unsigned long)) = hops_travelled;
				rx_packet.data_len = sizeof(unsigned long) + 1; //time + hops

				serialize_packet(&rx_packet, payload); // re-serialize packet.
			}

			/* forward to server */
			LOGMSG("main: rx_packet is rsp, sending to server");
			serial_send_addr_payload(&rx_packet.address, payload, KELVIN_PACKET_HEADER_SIZE+rx_packet.data_len);
		}		
		else {
			LOGMSG("main: general error");
		}
	}
}

/* needs to be executed regardless, to let sys_millis() maintin the system-timer.
 * or just take sys_millis() and let it execute somewhere else without it's value being used. */
static void periodic_tasks(void)
{
	static unsigned long last_time_1s = 0;
	unsigned long current_sys_millis = sys_millis(); // cache result
	
	if(last_time_1s + 1000 < current_sys_millis) {
		last_time_1s = current_sys_millis;
		/* executes every second: */
		//set_LED();
	}
	
#if AUTO_RESET_INTERVAL
	if(current_sys_millis > AUTO_RESET_INTERVAL) {
		M8C_Reset;
	}
#endif
}

static void gpio_handler(void)
{
	LOGMSG("entered gpio_handler");
	
	for(;;) {
		if(mac_radiohandler()) {
			//serial_cprint("Got packet!");
			//shouldn't handle received data here
		} else if(SWITCH_Pressed()) {
			chk_mem();
		} else {
			break;
		}
	}
	
	LOGMSG("exiting gpio_handler");
}

/* main function */

void main(void)
{	
	/* init local data */
	payload[0] = 0;
	
	/* init essential function blocks before anything else */
	setup_prs8();
	setup_counters();
	
	/* Init sensors */
	//Absolutely must be done before even attempting
	//to read any analog data 
#if (ENABLE_A_LIGHT || ENABLE_A_PRESSURE)
	sensors_analog_init();
#endif

	/* turn off LED to save power */
	LED_Off;
	
	/* init dynamic memory management system */
	dynmem_init(block0, sizeof(block0));
	dynmem_append(block1, sizeof(block1));
	//dynmem_append(block2, sizeof(block2));
	
	/* init serial early for debugging */
	serial_init();
	
	/* init xlowpan */
	xlowpan_init(mac_getdrv());
	/*mac_initradio();*//* xlowpan calls this if driver is supplied with the init function */

	M8C_EnableIntMask(INT_MSK0, INT_MSK0_GPIO);	/* Enable interupts on the GPIO port */
	M8C_EnableGInt; /* Turn on global interrupts */
	
	UART_CPutString("[ dynmem available = ");
	{
		char buf[5];
		utoa(buf, dynmem_avail(), 10);
		UART_PutString(buf);
	}
	UART_CPutString(" bytes ]\r\n");
	
	/* this is essentially a list of continously executed tasks. */
	for(;;) {
		recv_payload();
		serial_main();
		
		periodic_tasks(); // needs to execute because of sys_millis() (see above)
		
#if POWERSAVE
		M8C_Sleep;
#endif
	}
}

/** interrupt handlers **/

#if 0
#pragma interrupt_handler Counter16_ISR
void Counter16_ISR( void )
{
}
#else
/* see scheduler() above */
#endif

#pragma interrupt_handler PSoC_GPIO_ISR
void PSoC_GPIO_ISR(void)
{
	M8C_DisableIntMask(INT_MSK0, INT_MSK0_GPIO);
	gpio_handler();
	M8C_EnableIntMask(INT_MSK0, INT_MSK0_GPIO);
}
