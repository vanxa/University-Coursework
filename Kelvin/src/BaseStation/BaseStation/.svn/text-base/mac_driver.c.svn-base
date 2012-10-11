
/* PSoc port defintions */
#include "psocapi.h"
#include "psocgpioint.h"

/* Prospeckz api */
#include "prospeckz.h"
#include "prospeckz_radio.h"

#include "config.h"
#include "serial.h"

#include "mac_driver.h"

void (*xlowpan_receive_pkt)(void *data, size_t length);

/*== local variables ==*/

static unsigned char rx_data[MAC_MAX_PAYLOAD];

/*== local functions ==*/

static void send_pkt(void *data, size_t length)
{
	Radio_transmit((unsigned char*)data, (unsigned char)length);
	LOGMSG("send_pkt: sending packet done");
}

static void set_receive_pkt(void (*receive_pkt)(void *data, size_t length))
{
	LOGMSG("called set_receive_pkt");
	xlowpan_receive_pkt = receive_pkt;
}

/* return 64bit address; should be HW-id based. */
static void get_addr64(struct xlowpan_addr64 *addr)
{
	unsigned char hw_addr = HW_ADDR;
	LOGMSG("called get_addr64");
	
	XLOWPAN_SET_ADDR64((*addr), 'S', 'L', 'I', 'P', 'C', 'C', hw_addr, hw_addr);
}

/* should return a random number. TODO: needs proper random! */
static unsigned char make_session(void)
{
	static unsigned char sess = 0;
	unsigned char newsess;
	
	do {
		newsess = PRS8_bReadPRS();
	} while(newsess == sess);
	sess = newsess;
	
	//LOGMSG("called make_session");
	UART_CPutString("called make_session: ");
	UART_CPutString(" sess=");
	UART_PutSHexInt(sess);
	UART_CPutString("\r\n");
	
	return sess;
}

/*== API ==*/

struct mac_driver *mac_getdrv(void)
{
	static struct mac_driver drv;
	LOGMSG("called mac_getdrv");
	
	drv.send_pkt = send_pkt;
	drv.set_receive_pkt = set_receive_pkt;
	drv.get_addr64 = get_addr64;
	drv.make_session = make_session;
	drv.init = mac_initradio;
	
	return &drv;
}

/* core radio routines */

void mac_initradio(void)
{
	Radio_init(RADIO_CHANNEL);
	Radio_setTxLevel(RADIO_TXLEVEL);
	Radio_receiverOn;
}

unsigned char mac_radiohandler(void)
{
	/* If FIFOP pin is high there is a complete packet or the buffer has overflowed */
	if((FIFOP_Data_ADDR & FIFOP_MASK)) {
		receiveStatus_t rx_stat;
		
		LOGMSG("mac_radiohandler: radio interrupt");
			
		/* Now get the packet */
		if((rx_stat = Radio_receive(rx_data, MAC_MAX_PAYLOAD )).radioStatus == SUCCESS) {
			LOGMSG("mac_radiohandler: received packet");
			/* deliver to upper layer (xlowpan) */
			if(xlowpan_getstatus())
				xlowpan_receive_pkt(rx_data, rx_stat.length);
		}
		
		return 1;
	}
	
	return 0;
}
