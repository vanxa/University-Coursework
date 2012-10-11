
/*
 * Configuration for project
*/

#ifndef CONFIG_H
#define CONFIG_H

/* hardware identifier of this firmware */
#define HW_ADDR		35

/* size of one block of memory */
#define MEM_BLOCK_SIZE 256

/* Pseudo random number generator settings */
#define PRS_POLY 	0xB8
#define PRS_SEED 	0xFF

/* radio settings */
#define RADIO_CHANNEL 400 /* 0-511 */
#define RADIO_TXLEVEL 31 /* 0-31 */

/* this will enable continous looping in main() setting CPU to sleep after interrupts have been handled. */
#define POWERSAVE	0

/* logging information. 0 disable logging completely, 1 only log errors, 2 log everything. */
#define LOGGING		2

#ifndef FALSE
#define FALSE   (0)
#endif

#ifndef TRUE
#define TRUE    (1)
#endif

#endif
