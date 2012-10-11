/*
 * all sensor code is now in here
 * data gathering functions are exposed externally
 * written by Nicky Ellakirk and Peter Nock
 * with help from Marco Elver
 */
#include "DualADC8.h"
#include "psocapi.h"
#include "psocgpioint.h"
#include "prospeckz.h"
#include "serial.h"

static void sht_pause(int q)
//building block function
//put up here so i can use it for other things too
{
	int t;

	while (q--)
	{
		t = 100;
		while(t--) {
			asm("nop"); asm("nop"); asm("nop"); asm("nop");
			asm("nop"); asm("nop"); asm("nop"); asm("nop");
			asm("nop"); asm("nop"); asm("nop"); asm("nop");
			asm("nop"); asm("nop"); asm("nop"); asm("nop");
			asm("nop"); asm("nop"); asm("nop"); asm("nop");
			asm("nop"); asm("nop"); asm("nop"); asm("nop");
		}
	}
}

//analog section
void sensors_analog_init(void)
{
	//start adc input amplifiers (used to remap pins and clean signals)
	pga_adc_in_1_Start(3);
	pga_adc_in_2_Start(3);
	// initialize ADC
	DUALADC8_Start(DUALADC8_HIGHPOWER); // Turn on ADC section
	DUALADC8_SetCalcTime(5); // Set CalcTime to 100
	DUALADC8_GetSamples(); // Start ADC
	//setup pins to support boards
	ClearPin(pressure_light_gnd); //needs grounded always
	//TEST
	ClearPin(light_v_s); //don't power this yet
	ClearPin(pressure_nc); // just to be sure..
	//the SHT has its own init when it is needed
}

unsigned char read_pressure(void)
{
//reads the adc_in_2 port
//returns the byte of data
	//pressure sensor is externally powered so no power gating
	//unless i make its board even bigger and include a transistor
	//to turn it on and off, which I might do....
	unsigned char result;
	while(DUALADC8_fIsDataAvailable == 0); // Wait for data to be ready
	result = DUALADC8_cGetData2ClearFlag(); // Get Data from ADC Input2
	// and clear data ready flag
	return result;
}

unsigned char read_light(void)
{
//reads the adc_in_1 port
//returns the byte of data
	unsigned char result;
	//power on the light board
	SetPin(light_v_s);
	//allow to stabalise first
	//TEST
	sht_pause(10);
	//then read
	while(DUALADC8_fIsDataAvailable == 0); // Wait for data to be ready
	result = DUALADC8_cGetData1ClearFlag(); // Get Data from ADC Input1
	// and clear data ready flag
	//and power off the sensor
	//TEST
	sht_pause(5);
	ClearPin(light_v_s);
	return result;
}

void read_adc(char* cResult1,char* cResult2)
{
	
	while(DUALADC8_fIsDataAvailable == 0); // Wait for data to be ready
		*cResult1 = DUALADC8_cGetData1(); // Get Data from ADC Input1
		*cResult2 = DUALADC8_cGetData2ClearFlag(); // Get Data from ADC Input2
		// and clear data ready flag
		//User_Function(cResult1,cResult2); // User function to use data
		return;
}
//end of analog section


/* 
 * Hard coded bit bashing to work with the sht sensors.
 * not clever but it should do the job
 * at leat it uses functions....
 *
 * Nicky
 */

static void sht_set_mode(unsigned char mode){
	sht_pause(5);
	if(mode == 'r'){
		//SET TO MODE HIGH Z
		sht_data_DriveMode_0_ADDR &= ~sht_data_MASK; //0
		sht_data_DriveMode_1_ADDR |= sht_data_MASK; //1
		sht_data_DriveMode_2_ADDR &= ~sht_data_MASK; //0
	}
	else if(mode == 'w'){
		//SET MODE STRONG
		sht_data_DriveMode_0_ADDR |= sht_data_MASK; //1
		sht_data_DriveMode_1_ADDR &= ~sht_data_MASK; //0
		sht_data_DriveMode_2_ADDR &= ~sht_data_MASK; //0
	}
	sht_pause(5);
}
static void sht_init(void)
{
//building block function
	//set power pins
	sht_pause(10);
	ClearPin( sht_gnd );
	SetPin( sht_v_s );
	//set mode to write
	sht_set_mode('w');
	sht_pause(5);
	//set clock and data to normal levels
	SetPin( sht_data );
	ClearPin( sht_clk );
	sht_pause(3);
}

static void sht_read_init(void)
{	
//building block function
//sends the overlapping signals the sht is looking for
//data high
//clock high
//data low
//clock low
//clock high
//data high
//clock low
	SetPin( sht_data );
	sht_pause(2);
	SetPin( sht_clk );
	sht_pause(2);
	ClearPin( sht_data );
	sht_pause(2);
	ClearPin( sht_clk );
	sht_pause(2);
	SetPin( sht_clk );
	sht_pause(2);
	SetPin( sht_data );
	sht_pause(2);
	ClearPin( sht_clk );
	sht_pause(2);
	ClearPin( sht_data );
	sht_pause(2);
	//LEAVE DATA LOW!!!!
}

static void sht_send_zero(int len)
{
//building block function
//sends lots of zeros
//we need this quite a lot
	//data low
	ClearPin( sht_data );
	sht_pause(2);
	while (len--)
	{
		//clock tick for ever zero we want to send
		SetPin( sht_clk );
		sht_pause(2);
		ClearPin( sht_clk );
		sht_pause(2);
	}
}
static void sht_send_one(int len)
{
//building block function
//sends lots of ones
//we dont need this often
	//data high
	SetPin( sht_data );
	sht_pause(2);
	while (len--)
	{
		//clock tick for every 1 we want to send
		SetPin( sht_clk );
		sht_pause(2);
		ClearPin( sht_clk );
		sht_pause(2);
	}
}
static void sht_send_optional_one(void)
{
//building block function
	//data high
	SetPin( sht_data );
	sht_pause(2);
	//clock tick
	SetPin( sht_clk );
	sht_pause(2);
	ClearPin( sht_clk );
	sht_pause(2);
	//deliberately leave data high
}

static void sht_send_zero_one(void)
{
//building block function
	//because all read requests need this
	//data low
	ClearPin( sht_data );
	sht_pause(2);
	//clock tick
	SetPin( sht_clk );
	sht_pause(2);
	ClearPin( sht_clk );
	sht_pause(2);
	//data high
	SetPin( sht_data );
	sht_pause(2);
	//clock tick
	SetPin( sht_clk );
	sht_pause(2);
	ClearPin( sht_clk );
	sht_pause(2);
	//deliberately leave data high
}

void req_sht_result(char sht_req)
{	
//will need called from a function to tell the sht to prepare the result
//needs to be told what sort of result you want
//humidity h or temperature t
//uses building block functions
	//init the sensor
	sht_init();
	//
	sht_read_init();
	sht_send_zero(5);
	if(sht_req=='h'){
		sht_send_optional_one();
		LOGMSG("requesting h");
	}
	sht_send_zero_one();
	if(sht_req=='t') {
		sht_send_optional_one();
		LOGMSG("requesting t");
	}
	LOGMSG("finished requesting result");
}

void sht_reset_bus(void)
{
//special function
//to be used if there is a problem
//sends a bunch of ones then inits
	sht_set_mode('w');
	sht_send_one(10);
	sht_read_init();
}

static unsigned char register_value(void)
{
	if(sht_data_Data_ADDR & sht_data_MASK) {
	//check the exact bit in the port data register
		//if its set return 1
		return 1;
	}
	return 0;
	//else return 0
}

static int init_rec_sht_result(void)
{
//building block function
//after we have sent the read command we enter receive mode
//returns 1 when receive is about to start
	SetPin( sht_clk );
	sht_set_mode('r');
	sht_pause(5);
	//wait to see the ack for the command
	while(register_value());
	//go!
	sht_pause(5);
	ClearPin( sht_clk );
	sht_pause(10);
	return 1;
}


static unsigned char read_sht_result(unsigned char number)
{
//building block function
//should normally be called with 8
//as the sht returns data 1 byte at a time
	unsigned char result = 0;
	//initialise result to keep things clean
	while(number--){
		//clock tick per bit
		SetPin( sht_clk );
		sht_pause(5);
		//the sht returns serial data
		//so it needs packed into the result byte
		result |= (register_value() << (number));
		ClearPin( sht_clk );
		sht_pause(5);
	}
	//then return 
	return result;
}

void rec_sht_result(unsigned char *result_msb, unsigned char *result_lsb)
{
//the other main function
//uses building block functions
//this will need called after the setup function: req_sht_result
	
	/* now we play the waiting game, will be 100+ miliseconds */
	LOGMSG("start rec result");
	if(init_rec_sht_result()){
		LOGMSG("rec result initd");
		//WATCH DATA REGISTER WAITING FOR IT TO DROP LOW, indicating data is ready
		//READ DATA REGISTER FOR PORT 2 PIN 5
		sht_pause(50);
		while(register_value());
		//reay?? then go!
		//read first byte
		*result_msb = read_sht_result(8);
		//ACK FIRST BYTE
		sht_set_mode('w');
		ClearPin( sht_data );
		SetPin( sht_clk );
		sht_pause(5);
		ClearPin( sht_clk );
		//read second byte
		sht_set_mode('r');
		sht_pause(5);		
		*result_lsb = read_sht_result(8);
		//NO ACK!!!!!!!! unless we want to use CRC aswell....
		sht_pause(5);
	}
	LOGMSG("finished rec result");
}

unsigned char sht_read_value( unsigned char *msb , unsigned char *lsb , char type )
{
/* peters wrapper function to run the entire set of sht commands
 * to return the temperature data
 */
	req_sht_result( type );
	//change_color();			
	rec_sht_result(msb, lsb);
	if(( msb == 0 ) && ( lsb == 0 )){
		sht_reset_bus();
		return 0;
	}
	else
	{
		//Data is good!
		return 1;
	}
}
