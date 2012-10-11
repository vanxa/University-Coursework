/** 
 * FILENAME: prospeckz_radio.c
 * Original Authors: Mat Barnes, University of Edinburgh.
 * 
 * DESCRIPTION:
 * Radio primitives for ProspeckzII / Prospeckz IIK. This is the source for the ProspeckzRadio library (part of libProspeckz.a)
 * 
 * Blocks Required :
 * 	-- SPIM (SPI Master connecting to CC2420)
 *
 **/
#include "prospeckz_radio.h"

/**
* Simple blocking delay for the processor using loops, the time delay is dependant on CPU clock frequency.
* \param loops the number of loops to be done, control the delay time 
*/
void Radio_delay(unsigned char loops){
	int x,y;
	for (x=0; x<loops; x++)
		for (y=0; y<256; y++);
}		

/**
* Single byte SPI transfer with CC2420
* Outputs a character to the CC2420 as well as recieves a character from it
* \param txByte the byte to transmit
* \returns the byte received from the CC2420 
*/
char RADIO_SPIM_IO(char txByte){
 	while (!(SPIM_bReadStatus() & SPIM_SPIM_TX_BUFFER_EMPTY));
   	SPIM_SendTxData(txByte);
 	while (!(SPIM_bReadStatus() & SPIM_SPIM_RX_BUFFER_FULL));
   	return (SPIM_bReadRxData());   	
}

/** 
* Sets up the CC2420 for the bootloader and enables the receiver.
* The radio frequency is set to 2048 + frequencyOffset MHz. Where frequencyOffset is in the range 0-511.
* \param frequencyOffset the desired frequency offset from 2048 Mhz
*/
void Radio_init(unsigned int frequencyOffset){
	unsigned char x;

    CSn_Data_ADDR |= CSn_MASK;			// Disable CC2420 SPI 
	ResetN_Data_ADDR &= ~ResetN_MASK;	// Reset CC2420 with low signal
	Radio_delay(100);
	ResetN_Data_ADDR |= ResetN_MASK;	// Bring CC2420 to normal operation
    CSn_Data_ADDR &= ~CSn_MASK;			// Enable CC2420 SPI
    Radio_delay(100);
	SPIM_Start(SPIM_SPIM_MODE_0);    
	RADIO_SPIM_IO(0x11);		//MDMCTRL0
	RADIO_SPIM_IO(0x02);		//15:14=0, 13:RFM=0, 12:PC=0, 11:AD=0, 10:8:CCAH=2 // Diasables HW address decode - VITAL!
	RADIO_SPIM_IO(0x62);		//7:6:CCAM=1, 5:AUTOCRC=1, 4:AUTOACK=0, 3:0:PL=2 // CCA Pin Mode set to most useful mode. 

	RADIO_SPIM_IO(0x12);		//MDMCTRL1		
	RADIO_SPIM_IO(0x05);		// Set correlator threshold. // Has to be set to 20 by application, reset is zero, no good.
	RADIO_SPIM_IO(0x00);		// Buffered TX, RX modes, 802.15.4 compliant RF mode.

	RADIO_SPIM_IO(0x13);		//Set CCA Threshold		
	RADIO_SPIM_IO((char)-32);   // default (ie reset value)
	RADIO_SPIM_IO(0x00);		// not writable.

	RADIO_SPIM_IO(0x17);		//Set RXCTRL1		
	RADIO_SPIM_IO(0x2A);		// Bias Current changed as recomended in Data Sheet.
	RADIO_SPIM_IO(0x56);		// default settings

	RADIO_SPIM_IO(0x1C);		//IOCFG0		
	RADIO_SPIM_IO(0x00);		// Default Polarity for all IO pins. 
	RADIO_SPIM_IO(0x7F);		// Set threshold to 127.

	RADIO_SPIM_IO(0x18);		//Set Radio Frequency to BOOTLOADER CHANNEL. Correctly, Freq for channel select is Reg 0x18, FDSCTRL[9:0]
	RADIO_SPIM_IO(0x40 | (unsigned char)(frequencyOffset >> 8)); // sets Lock theshold and upper bits of Frequency
	RADIO_SPIM_IO((unsigned char) frequencyOffset);
	
  	RADIO_SPIM_IO(0x15);		//TXCTRL
	RADIO_SPIM_IO(0x80);		//Set TX_Turnaround to 128uS
	RADIO_SPIM_IO(0xFF);		//Set PA Current to default and TX Str to max
	
	RADIO_SPIM_IO(0x01);		//SXOSXON ON
	RADIO_SPIM_IO(0x02);		//STXCAL 

	RADIO_SPIM_IO(0x40 | 0x2C);			//read state mode	
	RADIO_SPIM_IO(0x00);					//should show 0x00
	while ((RADIO_SPIM_IO(0x00))!=0x01){	//wait until state is 0x01
		RADIO_SPIM_IO(0x40 | 0x2C);		//continue reading state mode
		RADIO_SPIM_IO(0x00);				
	}
}

/**
* Checks the radio for any received packets and attempt to retrieve any received data into the provided buffer. In the event of an error the radio receive buffer is flushed.
* \param pktPtr pointer to the structure to store received data in.
* \param pktBufferLength the length of the structure/buffer to store the received data, thus is the maximial length of data that can be received.
* \return a receiveStatus_t structure containing the status of the operation (SUCCESS / FAIL etc) and in the event of a successfull reception the rssi and lqi indicators from the radio. The status byte is the important field of the structure, the others are included for completeness and are usually superfluous. The status byte will be one of three values: RADIO_SUCCESS if successfully received all data, RADIO_FAIL_BUFFER_TOO_SHORT if the provided structure is not large enought to receive all data from the radio or RADIO_FAIL if some error occurs.
*/
receiveStatus_t Radio_receive(unsigned char* pktPtr, unsigned char pktBufferLength) {
	unsigned char counter;	
	unsigned char length;
	receiveStatus_t status; 
	
	if ((FIFOP_Data_ADDR & FIFOP_MASK)== 0)	{//No Data to Read as FIFOP will indicate either MPDU received or overflow
		status.radioStatus = RADIO_FAIL;
		return status;
	}	
	
	Radio_receiverOff;
	
	status.radioStatus = RADIO_SUCCESS;
	
	CSn_Data_ADDR |= CSn_MASK;			// Disable CC2420 SPI 
   	CSn_Data_ADDR &= ~CSn_MASK;			// Enable CC2420 SPI
	RADIO_SPIM_IO(0x40 | 0x3F);	//read RX FIFO
	*pktPtr = RADIO_SPIM_IO(0x00);   //Length
	length = (*pktPtr) - 2; // Take off the FCS length from the data
	
	// check for packets exceeding max expected packet length
	if (*pktPtr > (RADIO_MAX_PKT_LENGTH)) {
		status.radioStatus = RADIO_FAIL;
	}	
	// check for packet thats too short (less than headers + FCS)
	if (length < RADIO_MIN_PKT_LENGTH) {
		status.radioStatus = RADIO_FAIL;
	}
	if(pktBufferLength < length) {
		status.radioStatus = RADIO_FAIL_BUFFER_TOO_SHORT;
	}
	if (status.radioStatus != RADIO_SUCCESS){	
		CSn_Data_ADDR |= CSn_MASK;			// Disable CC2420 SPI 
		CSn_Data_ADDR &= ~CSn_MASK;			// Enable CC2420 SPI
		RADIO_SPIM_IO(0x40 | 0x3F);	//read a byte from RX FIFO Buffer
		RADIO_SPIM_IO(0x00); 
		CSn_Data_ADDR |= CSn_MASK;			// Disable CC2420 SPI 
		CSn_Data_ADDR &= ~CSn_MASK;			// Enable CC2420 SPI
	  	RADIO_SPIM_IO(0x08);		//SFLUSHRX
  		RADIO_SPIM_IO(0x08);		//SFLUSHRX
  		Radio_receiverOn;
		return status;	
	}		
	
	status.length = (*pktPtr);
	for (counter=0; counter<=length-1; counter++){					
		// If no outstanding data in RXBUFF when there should be some... ERROR!!! 
		if ((!(FIFOP_Data_ADDR & FIFOP_MASK)) && (!(FIFO_Data_ADDR & FIFO_MASK))){
	   		CSn_Data_ADDR |= CSn_MASK;			// Disable CC2420 SPI 
    		CSn_Data_ADDR &= ~CSn_MASK;			// Enable CC2420 SPI
			status.radioStatus = RADIO_FAIL;
		}								
		RADIO_Q_SPIM_IO(0x00,*(pktPtr+counter));		// Receive the next data byte
	}	
	RADIO_Q_SPIM_IO(0x00, status.rssi);	//receive RSSI
	RADIO_Q_SPIM_IO(0x00, status.lqi);	//receive CRC
	if (!(status.lqi & 0x80)) {
			status.radioStatus = RADIO_FAIL_CRC;
	}	
	status.lqi &= ~0x80; // Take off CRC indicator
    	CSn_Data_ADDR |= CSn_MASK;	// Disable CC2420 SPI 
    	CSn_Data_ADDR &= ~CSn_MASK;	// Enable CC2420 SPI

	Radio_receiverOn;
	return status;
}

/**
* Transmits the provided data packet. 
* Transmission is immediate, no guarantees are made on the availability of the channel.
* \param pktPtr pointer to the first byte of data to send.
* \param length the length of data to send (not including this length byte which will be sent also)
* \return RADIO_SUCCESS after transmission.
*/
unsigned char Radio_transmit(unsigned char* pktPtr, unsigned char length) {
	unsigned char counter;	
	if (RADIO_SPIM_IO(0x00)&20)	// If TX Buffer overflow occurs, flush TX buffer. Should never happen 
		RADIO_SPIM_IO(0x09);
	CSn_Data_ADDR |= CSn_MASK;		// Disable CC2420 SPI 
	CSn_Data_ADDR &= ~CSn_MASK;		// Enable CC2420 SPI	
	RADIO_SPIM_IO(0x3E);			// Write to TX FIFO
	RADIO_SPIM_IO(length + 2);		// Length of the Packet (Data (supplied by packet length) + FCS (2B))	
	for (counter = 0; counter < length; ++counter)
		RADIO_SPIM_IO(*(pktPtr+counter));
	CSn_Data_ADDR |= CSn_MASK;		// Disable CC2420 SPI 
	CSn_Data_ADDR &= ~CSn_MASK;		// Enable CC2420 SPI	
	RADIO_SPIM_OUT(0x04);		//STXON // Could use the TXCCA strobe for auto cca checking, then delay for random time and try again
	while (RADIO_SPIM_IO(0x00) & 0x08);	//loop until tx complete		
	return RADIO_SUCCESS;	
}

/**
* Sets the radio's transmission strength. 
* There are 32 levels, coded 0  to 31; 0 is the lowest power, 31 the highest. When passed a value greater than 31 the strength is set to the maximum.
*/
void Radio_setTxStrength(unsigned char strength){
  	RADIO_SPIM_IO(0x15);			//TXCTRL
	RADIO_SPIM_IO(0x80);			//Set TX_Turnaround to 128uS
	if (strength>31) strength=31;
	strength += 0xE0;
	RADIO_SPIM_IO(strength);		//Set to Radio Power
}

/**
* Reads the radio's current transmission strength setting. 
* There are 32 levels, coded 0  to 31; 0 is the lowest power, 31 the highest.
* \return the radio's transmission strength level (range:0-31);
*/
unsigned char Radio_readTxStrength(void) {
	unsigned char x;
	RADIO_SPIM_IO(0x40 | 0x15);		//READ TXCTRL
	RADIO_SPIM_IO(0x00);			//Read MSB
	x = RADIO_SPIM_IO(0x00);			//Set to Radio Power
	x = x & 0x1F;
	return x;
}

/**
* Sets the radio's transmission frequency. 
* The transimission frequency is computed on the CC2420 as a base of 2480 MHz plus the passed offset. The offset range is 0 - 511; giving a frequency range of 2048 - 2559 MHz. An offset greater than 511 will be set to 511. 
* \param frequencyOffset the desired offset from the base frequency of 2480 MHz.
*/
void Radio_setFrequency(unsigned int frequencyOffset) {
	if(frequencyOffset > 511) 
		frequencyOffset = 511;
	RADIO_SPIM_IO(0x18);		//Set Radio Frequency to BOOTLOADER CHANNEL. Correctly, Freq for channel select is Reg 0x18, FDSCTRL[9:0]
	RADIO_SPIM_IO(0x40 | (unsigned char)(frequencyOffset >> 8)); // sets Lock theshold and upper bits of Frequency
	RADIO_SPIM_IO((unsigned char) frequencyOffset);
}
