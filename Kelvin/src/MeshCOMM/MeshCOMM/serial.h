
#ifndef SERIAL_H
#define SERIAL_H

#include "config.h"

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
void serial_cprint(const unsigned char *str);
void serial_print(unsigned char *str);

#endif
