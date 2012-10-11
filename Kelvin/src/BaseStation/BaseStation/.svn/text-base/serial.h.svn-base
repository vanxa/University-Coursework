
#ifndef SERIAL_H
#define SERIAL_H

#include <stdlib.h>

#include "config.h"
#include "xlowpan.h"

/* setup serial logging */

#if LOGGING >= 2
  #define LOGMSG(msg) serial_cprint(msg)
#else
  #define LOGMSG(msg) 
#endif

#if LOGGING >= 1
  #define ERROR(msg) serial_cprint(msg)
#else
  #define ERROR(msg)
#endif

/* API */

void serial_init(void);
void serial_main(void);
void serial_cprint(const unsigned char *str);
void serial_print(unsigned char *str);
void serial_send_pkt(unsigned char* buf, size_t len);
void serial_send_addr_payload(struct xlowpan_addr64 *addr, unsigned char *buf, size_t buf_len);
void serial_handle_packet(unsigned char *buffer);

#endif
