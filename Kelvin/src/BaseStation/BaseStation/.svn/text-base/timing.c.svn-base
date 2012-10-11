
/* PSoc port defintions */
#include "psocapi.h"
#include "psocgpioint.h"

/* Prospeckz api */
#include "prospeckz.h"
#include "config.h"

#include "timing.h"

static unsigned int last_counter = 0;
static unsigned int counter; /* don't put on stack. semantically is global. */
static unsigned long sys_millis_ = 0x0000000;

/* needs to execute at least every 2sec, so it can maintain the sys_millis_ counter.
 * otherwise it will be come inaccurate. */
unsigned long sys_millis(void)
{
	/* expecting Counter16's Period to be 65535! */
	counter = (0xffff - Counter16_wReadCounter()) >> 5;
	
	if(last_counter > counter) {
		sys_millis_ = ((sys_millis_ & 0xfffff800) + 0x00000800);
	}
	
	sys_millis_ = (sys_millis_ & 0xfffff800) | (counter & 0x000007ff);
	
	last_counter = counter;
	return sys_millis_;
}

void sleep_millis(unsigned int millis)
{
	unsigned long tmp = sys_millis();
	while(tmp + millis > sys_millis());
}
