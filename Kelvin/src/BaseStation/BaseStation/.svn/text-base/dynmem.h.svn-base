/**
 * @file dynmem.h
 * Define OVERRIDE_MALLOC before including this header to use the API provided here as a replacement
 * for malloc/free/memcpy.
 *
 * @author Marco Elver <marco.elver AT gmail.com>
 * @date Mon Oct 11 21:34:18 BST 2010
 */

#ifndef DYNMEM_H
#define DYNMEM_H

#include <stddef.h>

/*== API ==*/

/**
 * Initialize the memory manager with an initial block of memory to be managed.
 *
 * Please note that the maximum amount of memory that can be allocated by dynmem_alloc
 * at once, is the size of the largest block of memory added (less header overhead).
 * 
 * @param buffer Pointer to block of memory
 * @param size Length of block of memory pointed to by buffer
 */
void dynmem_init(unsigned char *buffer, size_t size);

/**
 * Append another block of memory to existing pool of memory.
 * This is useful if the underlying system cannot provide the desired memory pool in one block.
 *
 * @param buffer Pointer to block of memory
 * @param size Length of block of memory pointed to by buffer
 */
void dynmem_append(unsigned char *buffer, size_t size);

/**
 * @return Bytes still available for allocation.
 */
size_t dynmem_avail(void);

/**
 * @param buflen Size of memory block to be allocated
 * @return Pointer to block of memory
 */
void *dynmem_alloc(size_t buflen);

/**
 * Free memory previously alloated.
 *
 * @param ptr A pointer that was returned by dynmem_alloc
 */
void dynmem_free(void *ptr);

/*== other functions that are not guaranteed to work. ==*/

/**
 * Optimized memcpy while retaining portability.
 *
 * @param dst Destination buffer
 * @param src Source buffer
 * @param len Bytes to be copied
 */
void m_memcpy(void *dst, void *src, size_t len);

#ifdef OVERRIDE_MALLOC
  #define malloc(_x) dynmem_alloc(_x)
  #define free(_x) dynmem_free(_x)
  #define memcpy(_d, _s, _l) m_memcpy(_d, _s, _l)
#endif

#endif

