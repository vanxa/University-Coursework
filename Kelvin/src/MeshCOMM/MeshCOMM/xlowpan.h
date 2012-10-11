/**
 * @file xlowpan.h
 *
 * @author Marco Elver <marco.elver AT gmail.com>
 * @date Thu Oct  7 14:29:04 BST 2010
 */

#ifndef XLOWPAN_H
#define XLOWPAN_H

#include <stdlib.h>

/*== Definitions ==*/

#define XLOWPAN_DISPATCH_IPV6 0x41 /* 01000001 =  65 */
#define XLOWPAN_DISPATCH_MESH 0x80 /* 1000xxxx = 128, for now just fixed 64bit, no 16bit address support */
#define XLOWPAN_DISPATCH_SLIP 0x77

/* 802.15.4 MAC header takes 25, which leaves us with 127 - 25 bytes */
#define MAC_MAX_PAYLOAD 102

/* Address definition */

struct xlowpan_addr64 {
	unsigned char addr[8];
};

#define XLOWPAN_ADDR_LEN	8

/* Address types. */
enum xlowpan_addr_type {
	XLOWPAN_ADDR_TYPE_NULL,
	XLOWPAN_ADDR_TYPE_IGNORE,
	XLOWPAN_ADDR_TYPE_BCAST,
	XLOWPAN_ADDR_TYPE_SELF
};

/**
 * Variable containing broadcast address. Broadcast is FFFFFFFFFFFFFFFF.
 */
extern struct xlowpan_addr64 XLOWPAN_ADDR_BCAST;

#define XLOWPAN_SET_ADDR64(aX, a0, a1, a2, a3, a4, a5, a6, a7) \
	(aX).addr[0]=a0; (aX).addr[1]=a1; (aX).addr[2]=a2; (aX).addr[3]=a3; \
	(aX).addr[4]=a4; (aX).addr[5]=a5; (aX).addr[6]=a6; (aX).addr[7]=a7;

#if 0
/*
 * Header definitions
 * ------------------
 * Do not copy them to the packet buffer directly, due to padding issues.
 * These are just here for reference.
 */

struct xlowpan_header_mesh {
	unsigned char dsp; /* dispatch */
	struct xlowpan_addr64 org_addr;
	struct xlowpan_addr64 fin_addr;
};

struct xlowpan_header_slip {
	unsigned char dsp; /* dispatch */
	unsigned char length;

	unsigned char session; /* random number, unique until seq gets reset. */
	unsigned char seq;

	void *data;
};
#endif

/**
 * Driver struct, to be passed to xlowpan_init.
 */
struct mac_driver {
	void (*send_pkt)(void *data, size_t length);
	void (*set_receive_pkt)(void (*receive_pkt)(void *data, size_t length));
	void (*get_addr64)(struct xlowpan_addr64 *addr); /* return 64bit address; should be HW-id based. */
	unsigned char (*make_session)(void); /* should return a random number */
	void (*init)(void); /* initialise hardware. set to NULL to init yourself. */
};

/*== API ==*/

/**
 * Initialises xlowpan. Call before using xlowpan.
 *
 * @param driver mac_driver instance to be used by xlowpan. 
 */
void xlowpan_init(struct mac_driver *driver);

/**
 * Shutdown xlowpan.
 */
void xlowpan_shutdown(void);

/**
 * Send a packet.
 *
 * @param dstaddr Destination address pointer.
 * @param data Pointer to payload data.
 * @param length Length of payload.
 * @return Length of payload sent. Should equal length param, unless final packet len > MAC_MAX_PAYLOAD.
 */
size_t xlowpan_send(struct xlowpan_addr64 *dstaddr, void* data, size_t length);

/**
 * Receives the next packet in the queue.
 *
 * @param srcaddr Pointer to address struct to be filled with source address. Can be set to NULL.
 * @param data Buffer to be filled with packet data.
 * @param buflen Buffer length; the maximum length to be read.
 * @return Number of bytes read (copied into data buffer)
 */
size_t xlowpan_recv(struct xlowpan_addr64 *srcaddr, void *data, size_t buflen);

/**
 * Receives the next packet in the queue.
 *
 * @param srcaddr Pointer to address struct to be filled with source address. Can be set to NULL.
 * @param dst_type Pointer to xlowpan_addr_type to be filled with dst type address. Can be set to NULL.
 * @param hops_travelled Pointer to byte to be filled with the hops this packet has travelled. Can be set to NULL.
 * @param data Buffer to be filled with packet data.
 * @param buflen Buffer length; the maximum length to be read.
 * @return Number of bytes read (copied into data buffer)
 */
size_t xlowpan_recv5(struct xlowpan_addr64 *srcaddr, enum xlowpan_addr_type *dst_type,
		unsigned char *hops_travelled, void *data, size_t buflen);

/**
 * Copies addresses.
 *
 * @param dst Destination address pointer.
 * @param src Source address pointer.
 */
void xlowpan_addrcpy(struct xlowpan_addr64 *dst, struct xlowpan_addr64 *src);

/**
 * Compares addresses.
 *
 * @param addr1 xlowpan address.
 * @param addr2 xlowpan address.
 * @return 0 if addr1 and addr2 are the same. -1 if addr1 < add2. 1 if addr1 > addr2.
 */
int xlowpan_addrcmp(struct xlowpan_addr64 *addr1, struct xlowpan_addr64 *addr2);

/**
 * @return Address of this host
 */
struct xlowpan_addr64 *xlowpan_getaddr(void);

/**
 * @return Status of xlowpan. 0 = not ready; 1 = ready. May be extended in future.
 */
unsigned char xlowpan_getstatus(void);

/**
 * Generates a new session.
 * @return The new session.
 */
unsigned char xlowpan_resetsession(void);

#endif /* XLOWPAN_H */
