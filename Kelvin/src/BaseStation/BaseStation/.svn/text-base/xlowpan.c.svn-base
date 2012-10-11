/**
 * @file xlowpan.c
 *
 * @author Marco Elver <marco.elver AT gmail.com>
 * @date Thu Oct  7 14:29:04 BST 2010
 */

#include <stdlib.h>
#include <string.h>

#ifdef _M8C
  #define OVERRIDE_MALLOC
  #include "dynmem.h"
#endif

#include "xlowpan.h"
#include "hashmap.h"

/*== internal defines ==*/

#define HASHMAP_SIZE		10
#define MAX_FORWARD_HOPS	0x0f

/* the session number should be random, however to prevent issues
when hosts reuse session at bootup, have some special seq nums.*/
#define SEQ_INIT	0 /* special signal to reset the seq to READY */
#define SEQ_READY	1 /* this is never used by host itself */
#define SEQ_BEGIN	2 /* this is where the sequence numbers begin */

/*== data ==*/

struct xlowpan_addr64 XLOWPAN_ADDR_BCAST = {
	{0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff}
};

struct seq_session_pair {
	unsigned char seq;
	unsigned char session;
};

struct packet_info {
	struct xlowpan_addr64 src;
	unsigned char attrs; /* [ hops_travelled:4bits | dst_type:4bits ] */
	size_t len;
	void *data;
};

static struct _mdata {
	struct mac_driver *driver;
	struct xlowpan_addr64 my_addr;
	unsigned char my_seq;
	unsigned char my_session;
	struct hash_map *ss_map; /* mapping: src_addr -> seq_session_pair */
	struct linked_list *receive_list; /* packet_info */
} mdata;

static unsigned char mstatus = 0;

/*== static functions ==*/

/**
 * Internal address hashing function.
 *
 * @param key Pointer to key
 * @return The computed hash value
 */
static size_t hash_addr64(void *key)
{
	struct xlowpan_addr64 *addr = (struct xlowpan_addr64*)key;
	size_t hash = 0;

	if(!key)
		return 0;

	/* TODO: make better hashing function -> use Fletcher-16 ? */

	/* lame */
	{
		size_t i = XLOWPAN_ADDR_LEN;
		while(i--) {
			hash += addr->addr[i] * i;
		}
	}

	return hash;
}

/**
 * Frees the receive buffer. Used for shutdown.
 */
static void free_receive_list(struct ll_node *node, void *p)
{
	if(node->data) {
		struct packet_info *packet = (struct packet_info*)node->data;
		if(packet->data)
			free(packet->data);
		free(node->data);
	}
}

/**
 * To be called when a raw packet is delivered from the layer above xlowpan (mac layer).
 * This is the function passed as argument to mac_driver->set_receive_pkt(receive_pkt).
 *
 * @param data Raw packet data
 * @param length Raw packet length
 */

/*
 * [dsp|org_addr|fin_addr|dsp|len|session|seq|data...]
 */
static void receive_pkt(void *data, size_t length)
{
	unsigned char forward_hops = 0;
	unsigned char hops_travelled;
	unsigned char *packet = (unsigned char *)data;
	struct xlowpan_addr64 org_addr;
	struct xlowpan_addr64 fin_addr;
	enum xlowpan_addr_type dst_type = XLOWPAN_ADDR_TYPE_NULL;

	if(!length)
		return;

	/* first round */
	if((*packet & XLOWPAN_DISPATCH_MESH) == XLOWPAN_DISPATCH_MESH) {
		forward_hops = (*packet & 0x0f) - 1;
		hops_travelled = MAX_FORWARD_HOPS - forward_hops;
		*packet = XLOWPAN_DISPATCH_MESH | forward_hops;
		++packet; /* done with dispatch */

		/* get addressing information */
		memcpy(org_addr.addr, packet, XLOWPAN_ADDR_LEN); packet += XLOWPAN_ADDR_LEN;
		memcpy(fin_addr.addr, packet, XLOWPAN_ADDR_LEN); packet += XLOWPAN_ADDR_LEN;

		/* fin_addr != my_addr && packet expired */
		if(!xlowpan_addrcmp(&fin_addr, &mdata.my_addr)) {
			dst_type = XLOWPAN_ADDR_TYPE_SELF;
		} else if(!xlowpan_addrcmp(&fin_addr, &XLOWPAN_ADDR_BCAST)) {
			dst_type = XLOWPAN_ADDR_TYPE_BCAST;
		} else {
			dst_type = XLOWPAN_ADDR_TYPE_IGNORE;
		}

		if(!xlowpan_addrcmp(&org_addr, &mdata.my_addr)) {
		   	/* this node sent the packet, don't send twice! */
			forward_hops = 0;
		}

		if(dst_type == XLOWPAN_ADDR_TYPE_IGNORE && !forward_hops) {
			/* discard */
			return;
		}
	}

	/* second round */
	switch(*packet++) {
		case XLOWPAN_DISPATCH_SLIP:
			{
				unsigned char payload_len;
				unsigned char session;
				unsigned char seq;
				struct seq_session_pair *ssp;

				/* retrieve data */
				payload_len = *packet++;
				session = *packet++;
				seq = *packet++; /* packet should now point to data */

				/* check in hash-map if session&seqnum are sane */
				if((ssp = (struct seq_session_pair*)hmap_get(mdata.ss_map, &org_addr))) {
					if(ssp->session == session &&
						!(seq == SEQ_INIT && ssp->seq != SEQ_READY) /* seq reset */
						) {
						if(seq <= ssp->seq) {
							/* bouncy packet, discard */
							return;
						}
					}
				} else {
					ssp = (struct seq_session_pair*)malloc(sizeof(struct seq_session_pair));

					if(!ssp) {
						ERROR("receive_pkt: out of memory!");
						return;
					}

					hmap_set(mdata.ss_map, &org_addr, ssp);
				}
				
				if(seq == SEQ_INIT) {
					/* make sure ssp->seq is set to READY,
					otherwise packet might be consumed twice. */
					seq = SEQ_READY;
				}

				/* update org_addr info in hash-map */
				ssp->session = session;
				ssp->seq = seq;

				/* append to receive_list */
				if((dst_type == XLOWPAN_ADDR_TYPE_BCAST || dst_type == XLOWPAN_ADDR_TYPE_SELF)
						&& payload_len) {
					/* this packet is for us! hurray! */
					struct packet_info *pk_info = (struct packet_info*)malloc(sizeof(struct packet_info));
					if(!pk_info) {
						ERROR("receive_pkt: out of memory!");
						return;
					}

					/* potentially dangerous if not caught */
					if((packet + payload_len) - (unsigned char*)data > length) {
						ERROR("receive_pkt: headers + payload_len > length; truncated payload?");
						payload_len = (length + (unsigned char*)data) - packet;
					} 

					xlowpan_addrcpy(&pk_info->src, &org_addr);
					pk_info->attrs = (dst_type & 0x0f) | ((hops_travelled) << 4);
					pk_info->len = payload_len;

					pk_info->data = (void*)malloc(payload_len);
					if(!pk_info->data) {
						ERROR("receive_pkt: out of memory!");
						free(pk_info);
						return;
					}

					memcpy(pk_info->data, packet, payload_len);
					packet += payload_len;

					/* assert */
					if(packet - (unsigned char*)data < length) {
						ERROR("receive_pkt: headers + payload_len < length");
					}

					/* put at end of list, for further processing by upper layer */
					{
						struct ll_node *newnode;
					   
						if(!(newnode = llist_append(mdata.receive_list))) {
							ERROR("receive_pkt: could not append to receive_list");
						} else {
							newnode->data = pk_info;
						}
					}

					if(dst_type == XLOWPAN_ADDR_TYPE_SELF)
						break; /* don't forward */
				}

				/* fall through and forward */
			}
		default:
			if(forward_hops) {
				/* forward */
				mdata.driver->send_pkt(data, length);
			}
			break;
	}
}

/*== implementation ==*/

void xlowpan_init(struct mac_driver *d)
{
	/* might be worth checking if d==NULL, but if you don't provide a mac_driver,
	 * it's your own fault! */

	d->set_receive_pkt(receive_pkt);
	mdata.driver = d;

	/* get own address */
	d->get_addr64(&mdata.my_addr);

	mdata.ss_map = hmap_create(HASHMAP_SIZE, hash_addr64);
	mdata.receive_list = llist_create();
	
	/* initialize sequence number and session */
	mdata.my_seq = SEQ_INIT;
	mdata.my_session = d->make_session();
	
	/* bring up mac layer if desired.
	the advantage would be that xlowpan takes care of session INIT. */
	if(d->init) {
		d->init();
		xlowpan_send(&XLOWPAN_ADDR_BCAST, NULL, 0); /* dummy packet */
	} else {
		ERROR("WARNING: Did not initalise mac-layer.");
	}
	
	mstatus = 1;
}

void xlowpan_shutdown(void)
{
	mstatus = 0;
	
	/* free hashmap and all allocated seq_session pairs. */
	hmap_destroy(mdata.ss_map, hmap_generic_free_data);
	llist_destroy(mdata.receive_list, free_receive_list, NULL);
}

/*
 * Copy src to dst.
 */
void xlowpan_addrcpy(struct xlowpan_addr64 *dst, struct xlowpan_addr64 *src)
{
	if(!dst || !src)
		return;
	
	/*memcpy(dst, src, sizeof(struct xlowpan_addr64));*/
	{
		size_t i = XLOWPAN_ADDR_LEN;
		while(i--) {
			dst->addr[i] = src->addr[i];
		}
	}
}

int xlowpan_addrcmp(struct xlowpan_addr64 *addr1, struct xlowpan_addr64 *addr2)
{
	size_t i = XLOWPAN_ADDR_LEN;

	while(i--) {
		if(addr1->addr[i] == addr2->addr[i])
			continue;
		else if(addr1->addr[i] < addr2->addr[i])
			return -1;
		else if(addr1->addr[i] > addr2->addr[i])
			return 1;
	}

	return 0; /* same */
}

/*
 * [dsp|org_addr|fin_addr|dsp|len|session|seq|data...]
 *
 * @return number of bytes sent. should be equal to length, otherwise only partially sent. */
size_t xlowpan_send(struct xlowpan_addr64 *dstaddr, void* data, size_t length)
{
	size_t final_len = 5 + (2*XLOWPAN_ADDR_LEN) + length;
	size_t pos = 0;
	unsigned char *packet;

	if(final_len > MAC_MAX_PAYLOAD) {
		/* assuming our headers fit in MAC_MAX_PAYLOAD */
		length = MAC_MAX_PAYLOAD - (final_len - length);
		final_len = MAC_MAX_PAYLOAD;
	}
		
	if(!(packet = (unsigned char*)malloc(final_len))) {
		ERROR("xlowpan_send: out of memory!");
		return 0;
	}

	/* build the packet */

	/* mesh part */
	*packet = XLOWPAN_DISPATCH_MESH | MAX_FORWARD_HOPS; ++pos;
	memcpy(packet+pos, mdata.my_addr.addr, XLOWPAN_ADDR_LEN); pos += XLOWPAN_ADDR_LEN;
	memcpy(packet+pos, dstaddr->addr, XLOWPAN_ADDR_LEN); pos+= XLOWPAN_ADDR_LEN;

	/* SLIP part */
	*(packet+pos) = XLOWPAN_DISPATCH_SLIP; ++pos;
	*(packet+pos) = (unsigned char)(0xff & length); ++pos;
	*(packet+pos) = mdata.my_session; ++pos;
	*(packet+pos) = mdata.my_seq; ++pos;
	if(data && length) {
		memcpy(packet+pos, data, length); pos += length;
	}
	
	/* next seq */
	if(mdata.my_seq == 0xff) {
		/* sequence number wrap */
		xlowpan_resetsession();
	} else if(mdata.my_seq == SEQ_INIT) {
		/* this was the init seq packet. skip READY, as only used by receivers. */
		mdata.my_seq = SEQ_BEGIN;
	} else {
		++mdata.my_seq;
	}

	/* send off */
	if(final_len != pos) {
		ERROR("xlowpan_send: final_len != pos; check your code!");
	}

	mdata.driver->send_pkt(packet, final_len);

	/* TODO: does send_pkt need the packet still ? */
	free(packet);

	return length;
}

size_t xlowpan_recv5(struct xlowpan_addr64 *srcaddr, enum xlowpan_addr_type *dst_type,
		unsigned char *hops_travelled, void *data, size_t buflen)
{
	struct packet_info *pk_info;
	struct ll_node *node;
	size_t to_read;

	node = mdata.receive_list->head;

	if(!node) {
		/* nothing in the queue */
		return 0;
	}

	pk_info = (struct packet_info*)node->data;

	/* how much can we read */
	to_read = (pk_info->len > buflen ? buflen : pk_info->len);
	
	if(srcaddr) {
		/* provide source address if desired */
		xlowpan_addrcpy(srcaddr, &pk_info->src);
	}

	if(dst_type) {
		*dst_type = (pk_info->attrs & 0x0f);
	}

	if(hops_travelled) {
		*hops_travelled = ((pk_info->attrs & 0xf0) >> 4);
	}

	if(data) {
		/* copy data to upper layer */
		memcpy(data, pk_info->data, to_read);
	}

	if(pk_info->len > buflen) {
		pk_info->len -= to_read;
		pk_info->data = ((unsigned char*)pk_info->data) + to_read;
	} else {
		/* this should be regular case; discard node from list */
		llist_remove(mdata.receive_list, node, free_receive_list, NULL);
	}

	return to_read;
}

size_t xlowpan_recv(struct xlowpan_addr64 *srcaddr, void *data, size_t buflen)
{
	return xlowpan_recv5(srcaddr, NULL, NULL, data, buflen);
}

struct xlowpan_addr64 *xlowpan_getaddr(void)
{
	return &mdata.my_addr;
}

unsigned char xlowpan_getstatus(void)
{
	return mstatus;
}

unsigned char xlowpan_resetsession(void)
{
	mdata.my_seq = SEQ_BEGIN; /* Not an INIT. Just start from BEGIN. */
	mdata.my_session = mdata.driver->make_session(); /* session reset */
	return mdata.my_session;
}
