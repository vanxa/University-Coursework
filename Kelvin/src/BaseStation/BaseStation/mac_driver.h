
#ifndef MACDRIVER_H
#define MACDRIVER_H

#include "xlowpan.h"

/**
 * Function pointer, to be set by xlowpan.
 * Call this when a packet is received by the radio.
 */
extern void (*xlowpan_receive_pkt)(void *data, size_t length);

/*== API ==*/

/**
 * @return mac_driver pointer to be passed to xlowpan_init.
 */
struct mac_driver *mac_getdrv(void);

/**
 * Initialisie the radio.
 */
void mac_initradio(void);

/**
 * Check for incoming packets in the radio buffer and deal with it accordingly.
 * Should be called by the interrupt handler.
 * This function calls xlowpan_receive_pkt.
 *
 * @return 1 if there was a packet to be handled, 0 otherwise.
 */
unsigned char mac_radiohandler(void);

#endif /* MACDRIVER_H */
