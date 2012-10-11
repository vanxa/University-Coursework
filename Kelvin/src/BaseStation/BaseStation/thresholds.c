#include "sensors.h"
#include "thresholds.h"
#include "prospeckz.h"
#include "stdlib.h"

/* 
 * if temperature > UPPER_BOUND set LED to RED 
 * else if LOWER_BOUND < temp < UPPER_BOUND set the LED to GREEN 
 * else (= temp < LOWER_BOUND) and set the LED color to BLUE
 *
 */ 
 
void set_LED(void)
{
	char light_sen;
	char temp_sen;
	int temp, light;
	
	/* read the data from light and temperature sensors */
	//read_adc(&light_sen, &temp_sen); // do NOT use this function anymore.
	
	/* convert to decimals */
	temp = temp_sen;
	light = light_sen;
	
	/* turn off the LED */
	LED_Off;
	
	if (temp > UPPER_BOUND)
	{
		LED_RED_On;	
	}
	else if (temp > LOWER_BOUND)
	{
		LED_GREEN_On;
	}
	else 
	{
		LED_BLUE_On;
	}
}
