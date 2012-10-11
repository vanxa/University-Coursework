
/* PSoc port defintions */
#include "psocapi.h"
#include "psocgpioint.h"
#include "serial.h"

void serial_init(void)
{	
	UART_CmdReset(); // Initialize receiver/cmd
	// buffer
	UART_IntCntl(UART_ENABLE_RX_INT); // Enable RX interrupts
	Counter8_WritePeriod(155); // Set up baud rate generator
	Counter8_WriteCompareValue(77);
	Counter8_Start(); // Turn on baud rate generator
	UART_Start(UART_PARITY_NONE); // Enable UART
	
	serial_cprint("\r\n:: Welcome to Kelvin [v0.1.1] ::");
}

void serial_cprint(const unsigned char *str)
{
	UART_CPutString(str);
	UART_CPutString("\r\n");
}

void serial_print(unsigned char *str) {
	UART_PutString(str);
	UART_CPutString("\r\n");
}
