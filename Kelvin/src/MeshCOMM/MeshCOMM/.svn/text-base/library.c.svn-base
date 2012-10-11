#include "prospeckz.h"
#include "prospeckz_radio.h"

//********************************************
// Misc Functions
//********************************************

//Detects if the switch is pressed
unsigned char SWITCH_Pressed(void){
	SWITCH_Data_ADDR &= ~SWITCH_MASK;	//force low
	if (SWITCH_Data_ADDR & SWITCH_MASK)
		return TRUE;
	else
		return FALSE;		
}

void prospeckz_reset(void) {
	Radio_receiverOff;
	Radio_receiverOff;	
	Radio_rxFlush;
	Radio_rxFlush;
	asm("ljmp 0x00");
}
