#include <M8c.h>
#include "psocapi.h"
#include "psocgpioint.h"

#ifndef PROSPECKZ_H
#define PROSPECKZ_H
//**************************************
// Defines for packet format
// Change these to define new packet sizes or format
// Do not let MAX_PKT_LEN be bigger than 64 bytes
//**************************************
#define MAX_PKT_LEN 32		//Max packet length (NOT including headers, CRC, RSSI, etc)
#define MIN_PKT_HDR_LEN 8  	//Min. packet header length (Not including the Length Field, CRC, RSSI)
#define MAX_CMD_BUF_LEN 16


//Defines for radio function returns
#define SUCCESS 0x00
#define CRC_FAIL 0x01
#define NO_DATA 0x02
#define WAKEUP 0x03
#define WAKEUP_MASK 0x20
#define FAILURE 0xFF
#define COMMAND 0x04

//**********************************************************
// Prospeckz LED Macros
//**********************************************************
#define LED_RED_On 	LED_RED_Data_ADDR &=~ LED_RED_MASK
#define LED_RED_Off 	LED_RED_Data_ADDR |= LED_RED_MASK
#define LED_GREEN_On 	LED_GREEN_Data_ADDR &=~ LED_GREEN_MASK
#define LED_GREEN_Off 	LED_GREEN_Data_ADDR |= LED_GREEN_MASK
#define LED_BLUE_On 	LED_BLUE_Data_ADDR &= ~LED_BLUE_MASK
#define LED_BLUE_Off 	LED_BLUE_Data_ADDR |= LED_BLUE_MASK
#define LED_Off			LED_Data_ADDR=0x07
#define LED_ALL_On		LED_Data_ADDR=0x00
#define LED_Data_ADDR	LED_RED_Data_ADDR

//**********************************************************
// Interrupt Macros
//**********************************************************
#define RaiseInterrupt(intMask, intType) {INT_MSK3 |= INT_MSK3_ENSWINT;intMask |= intType;INT_MSK3 &= ~INT_MSK3_ENSWINT;}	
#define ClearInterrupt(intMask, intType) intMask &= ~intType;	

//**********************************************************
// GPIO Macros
//**********************************************************
#define SetPin(pinname) pinname##_Data_ADDR |= pinname##_MASK
#define ClearPin(pinname) pinname##_Data_ADDR &= ~pinname##_MASK

//**********************************************************
// Hard Sleep Mode Macro.
// Disables Interrupts (Globally), sleeps and re-enables interrupts on wake by sleep timer
//**********************************************************
#define GoToSleep {\
	asm("and F, FEh");\
   	CPU_SCR0 |= CPU_SCR0_SLEEP_MASK;\
   	asm("or  F, 01h");}
   	
void prospeckz_reset(void);


//**********************************************************
// Poll the input from the switch
//**********************************************************   
unsigned char SWITCH_Pressed(void);

#endif //PROSPECKZ_H