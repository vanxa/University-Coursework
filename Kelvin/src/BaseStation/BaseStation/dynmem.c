/**
 * @file dynmem.c
 *
 * This implementation is based on the article found at:
 * http://www.eetimes.com/design/automotive-design/4007638/Memory-allocation-in-C
 *
 * @author Marco Elver <marco.elver AT gmail.com>
 * @date Mon Oct 11 21:34:51 BST 2010
 */

#include "dynmem.h"

struct dynmem_header {
	struct dynmem_header *ptr;
	size_t size; /* size in sizeof(dynmem_header) blocks */
};

static struct dynmem_info {
	size_t mem_avail; /* in sizeof(dynmem_header) blocks */
	struct dynmem_header *frhd;
} dmem;

/*== implementation ==*/

void dynmem_init(unsigned char *buffer, size_t size)
{
	dmem.frhd = (struct dynmem_header*)buffer;
	dmem.mem_avail = size / sizeof(struct dynmem_header);

	dmem.frhd->ptr = NULL;
	dmem.frhd->size = dmem.mem_avail;
}

void dynmem_append(unsigned char *buffer, size_t size)
{
	struct dynmem_header *next, *prev, *newblock;
	
	/* after setup, the linked list is expected to be ordered in ascending memory address order */

	for(	prev=NULL, next=dmem.frhd;
			next && ((struct dynmem_header*)buffer) > next;
			prev=next, next=next->ptr) {}
	
	newblock = (struct dynmem_header*)buffer;
	newblock->ptr = next;
	newblock->size = size / sizeof(struct dynmem_header);

	if(prev) {
		prev->ptr = newblock;
	} else {
		/* only other possibility */
		dmem.frhd = newblock;
	}

	dmem.mem_avail += newblock->size;
}

size_t dynmem_avail(void)
{
	return dmem.mem_avail * sizeof(struct dynmem_header);
}

void *dynmem_alloc(size_t buflen)
{
	struct dynmem_header *next, *prev;
	size_t nunits;

	/* round up */
	nunits = (buflen + sizeof(struct dynmem_header) - 1) / sizeof(struct dynmem_header) + 1;

	for(prev=NULL, next=dmem.frhd; next; prev=next, next=next->ptr) {
		if(next->size >= nunits) {
			if(next->size > nunits) {
				/* top[  still free   |  nunits  allocated here ]bottom */
				next->size -= nunits;
				next += next->size; /* point to memory to be allocated */
				next->size = nunits;
			} else {
				/* exactly the right size */
				if(!prev)
					dmem.frhd = next->ptr;
				else
					prev->ptr = next->ptr;
			}

			dmem.mem_avail -= nunits;

			/* return pointer past the header */
			return ((void*)(next+1));
		}
	}

	return NULL;
}

void dynmem_free(void *ptr)
{
	struct dynmem_header *prev = NULL;
	struct dynmem_header *next, *to_free;

	if(!ptr) /* NULL you ! */
		return;

	/* pointer to header of block being returned */
	to_free = ((struct dynmem_header *)ptr) - 1;

	dmem.mem_avail += to_free->size;

	if(!dmem.frhd || dmem.frhd > to_free) {
		/* free space head is higher up */
		next = dmem.frhd; /* old head */
		dmem.frhd = to_free; /* new head */
		prev = to_free + to_free->size;

		if(prev == next) {
			/* old and new are contiguous */
			to_free->size += next->size;
			to_free->ptr = next->ptr;
		} else {
			to_free->ptr = next;
		}

		return;
	}

	for(next=dmem.frhd; next && next < to_free; prev=next, next=next->ptr) {
		if(next+next->size == to_free) {
			/* they're contiguous */
			next->size += to_free->size; 
			to_free = next + next->size;
			if(to_free == next->ptr) {
				/* to contiguous free blocks, no need to continue checking,
				 * since if the block after those were free it would have been merged already */
				next->size += to_free->size;
				next->ptr = to_free->ptr;
			}
			return;
		}
	}

	prev->ptr = to_free;
	prev = to_free + to_free->size;
	if(prev == next) {
		to_free->size += next->size;
		to_free->ptr = next->ptr;
	} else {
		to_free->ptr = next;
	}
}

void m_memcpy(void *_dst, void *_src, size_t len)
{
	size_t *dst = (size_t*)_dst;
	size_t *src = (size_t*)_src;
	size_t remainder = len % sizeof(size_t);

	if(dst == src)
		return;
	
	if(remainder) {
		len -= remainder;
		while(remainder--) {
			*((unsigned char*)dst) = *((unsigned char*)src);
			dst = (size_t*)(((unsigned char*)dst) + 1);
			src = (size_t*)(((unsigned char*)src) + 1);
		}
	}
	
	len /= sizeof(size_t);
	while(len--) {
		*dst = *src++; ++dst;
	}
}

