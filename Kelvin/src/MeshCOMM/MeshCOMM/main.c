/*
 * MeshCOMM: xlowpan, mesh-networking and serial port 
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
#include "dynmem.h"
#include "xlowpan.h"
#include "mac_driver.h"
#include "serial.h"

/* local defines */
#define PAYLOAD_SIZE 8

/* local variables */

static unsigned char payload[PAYLOAD_SIZE];

static unsigned char block0[MEM_BLOCK_SIZE];
static unsigned char block1[MEM_BLOCK_SIZE];
static unsigned char block2[MEM_BLOCK_SIZE];


/* local setup routines */

static void setup_prs8(void)
{
	PRS8_WritePolynomial(PRS_POLY);
	PRS8_WriteSeed(PRS_SEED);
	PRS8_Start();
}

static void setup_counters(void)
{
	Counter16_EnableInt();
	Counter16_Start();
}

/* local core routines */

static void change_color(void)
{
	LED_Off;
	
	++payload[0];
	
	/* don't overwrite upper the bit or it will get locked in the interrupt */
	if(payload[0] >= 8)
		payload[0] = 0;
	
	LED_Data_ADDR = payload[0];
		
	LOGMSG("changed color and transmit");
	xlowpan_send(&XLOWPAN_ADDR_BCAST, payload, PAYLOAD_SIZE);
}

static void gpio_handler(void) {
	LOGMSG("entered gpio_handler");
	
	for(;;) {
		if (mac_radiohandler()) {
			/* maybe there was a packet for us, consume if there was */
			while(xlowpan_recv(NULL, payload, PAYLOAD_SIZE)) {
				LOGMSG("gpio_handler: consuming packet");
				LED_Data_ADDR = payload[0];
			}
		} else if(SWITCH_Pressed()) {
			change_color();
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
	
	/* turn off LED to save power */
	LED_Off;
	
	/* init serial as early as possible for logging */
	serial_init();
	
	/* init essential function blocks before anything else */
	setup_prs8();
	setup_counters();
	
	/* init dynamic memory management system */
	dynmem_init(block0, MEM_BLOCK_SIZE);
	dynmem_append(block1, MEM_BLOCK_SIZE);
	dynmem_append(block2, MEM_BLOCK_SIZE);
	
	/* init xlowpan */
	xlowpan_init(mac_getdrv());
	/*mac_initradio();*//* xlowpan calls this if driver is supplied with the init function */
	
	/* init interrupts before xlowpan */
	M8C_EnableIntMask(INT_MSK0, INT_MSK0_GPIO);	/* Enable interupts on the GPIO port */
	M8C_EnableGInt; /* Turn on global interrupts */
	
#if POWERSAVE
	for(;;) {
		LOGMSG("going to sleep");
		
		asm("nop");
		asm("nop");
		M8C_Sleep;
		asm("nop");
		asm("nop");
		
		LOGMSG("waking up");
	}
#endif
}

/** interrupt handlers **/

#pragma interrupt_handler Counter16_ISR
void Counter16_ISR( void )
{
#if 0
	change_color();
#endif
}

#pragma interrupt_handler PSoC_GPIO_ISR
void PSoC_GPIO_ISR(void)
{
	M8C_DisableIntMask(INT_MSK0, INT_MSK0_GPIO);
	gpio_handler();
	M8C_EnableIntMask(INT_MSK0, INT_MSK0_GPIO);
}
