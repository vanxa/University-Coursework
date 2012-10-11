
/*
 * Configuration for project
*/

#ifndef CONFIG_H
#define CONFIG_H

/* hardware identifier of this firmware */
#define HW_ADDR		0x56

/* enable ADC or not; suggest disabling if ADC sensor not used */
#define ENABLE_A_LIGHT		0
#define ENABLE_A_PRESSURE	0

/* automatically reset board after specific period of time. set to 0 to disable. */
#define AUTO_RESET_INTERVAL 180000

/* size of one block of memory */
#define MEM_BLOCK_SIZE 256

/* maximum size of incoming packet the payload buffer can hold */
#define PAYLOAD_BUFFER_SIZE 32

/* Pseudo random number generator settings */
#define PRS_POLY 	0xB8
#define PRS_SEED 	0xFF

/* radio settings */
#define RADIO_CHANNEL 400 /* 0-511 */
#define RADIO_TXLEVEL 31 /* 0-31 */

/* this will enable continous looping in main() setting CPU to sleep after interrupts have been handled. */
#define POWERSAVE	0

/* logging information. 0 disable logging completely, 1 only log errors, 2 log everything. */
#define LOGGING		0

// TRUE AND FALSE already definded in m8c.h

#ifndef FALSE
#define FALSE	(0)
#endif

#ifndef TRUE
#define TRUE	(1)
#endif

 
#endif

