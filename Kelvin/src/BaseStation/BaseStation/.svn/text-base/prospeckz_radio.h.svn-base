#include <M8c.h>
#include "psocapi.h"
#include "psocgpioint.h"

#ifndef PROSPECKZ_RADIO_H
#define PROSPECKZ_RADIO_H

//**********************************************************
// Defines
//**********************************************************
#define RADIO_SUCCESS 0
#define RADIO_FAIL 1
#define RADIO_FAIL_CRC 2
#define RADIO_FAIL_BUFFER_TOO_SHORT 3

#define RADIO_MAX_TX_LEVEL 31

#define RADIO_MAX_PKT_LENGTH 100
#define RADIO_MIN_PKT_LENGTH 2

typedef struct {
	unsigned char radioStatus;
	unsigned char rssi;
	unsigned char lqi;
	unsigned char length;
} receiveStatus_t;

//*********************************************************
// MACROS
//**********************************************************

// Turns radio receiver on... enable CC2420 to receive packets
#define Radio_receiverOn\
	{\
	RADIO_SPIM_OUT(0x08);\
	RADIO_SPIM_OUT(0x08);\
	RADIO_SPIM_OUT(0x03);\
	}
// Send RFOFF strobe to CC2420
#define Radio_receiverOff RADIO_SPIM_OUT(0x06); //SRFOFF	
	
/**
* Faster SPI transfers, no function overhead.
*/
#define RADIO_Q_SPIM_IO(input,output)\
	{	while (!(SPIM_CONTROL_REG & SPIM_SPIM_TX_BUFFER_EMPTY));\
		SPIM_TX_BUFFER_REG = input;\
	 	while (!(SPIM_CONTROL_REG & SPIM_SPIM_RX_BUFFER_FULL));\
		output=SPIM_RX_BUFFER_REG; }
//quick SPIM out... risky and depends on 4mbps SPIM and processor running at 4MIPS... But fastest way
#define RADIO_Q_SPIM_OUT(output)	SPIM_TX_BUFFER_REG = output;
// Send a byte via SPI, fast?				
#define RADIO_SPIM_OUT(txByte) {while (!(SPIM_bReadStatus() & SPIM_SPIM_TX_BUFFER_EMPTY));SPIM_SendTxData(txByte);}

#define Radio_rxFlush RADIO_SPIM_IO(0x08)
//Flushes RX Buffer

//**********************************************************
// Function Prototypes
//**********************************************************

void Radio_init(unsigned int frequency);

receiveStatus_t Radio_receive(unsigned char* pktPtr, unsigned char pktBufferLength);

char RADIO_SPIM_IO(char txByte);

unsigned char Radio_transmit(unsigned char* pktPtr, unsigned char length);

unsigned char Radio_transmitPkt(unsigned char* pktPtr);
#define Radio_transmitPkt(pktPtr) Radio_transmit((pktPtr+1), *pktPtr)

void Radio_delay(unsigned char time);
unsigned char Radio_getHWAddr(void);

#define Radio_readTxLevel Radio_readTxStrength
#define Radio_setTxLevel Radio_setTxStrength

// Change the radio transmission strength
// Inputs:	strength=> 0..31
// Outputs: None
void Radio_setTxStrength(unsigned char strength);

// Read the radio transmission strength
// Inputs:	None
// Outputs: Strength=> 0..31
unsigned char Radio_readTxStrength(void); 

void Radio_setFrequency(unsigned int freq);

#endif //PROSPECKZ_RADIO_H