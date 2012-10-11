/**
 * @file hashmap.h
 *
 * @author Marco Elver <marco.elver AT gmail.com>
 * @date Fri Oct  8 19:34:34 BST 2010
 */

#ifndef HASHMAP_H
#define HASHMAP_H

#include <stdlib.h>

/* strip it down as much as possible, without loosing too much functionality */
#define STRIPPED

/* ERROR define */
#include "serial.h"
#ifndef ERROR
#define ERROR(msg)
#endif /* ERROR */

/*== data ==*/

struct ll_node {
	void *data;
	struct ll_node *next;
	struct ll_node *prev;
};

struct linked_list {
	struct ll_node *head; /* "first" entry */
	struct ll_node *tail; /* subsequent entries */
#ifndef STRIPPED
	size_t elements;
#endif
};

typedef size_t (*hashfn_t)(void *key);

struct hash_map {
	struct linked_list *buckets_array; /* array of linked_lists */
	size_t num_buckets;
	hashfn_t hash_fn;
};

/*== API ==*/

/*= linked list =*/
struct linked_list *llist_create(void);
void llist_destroy(struct linked_list* list, void (*destroy_callback)(struct ll_node *node, void *p), void *param);
struct ll_node *llist_search(struct linked_list *list, struct ll_node *(*callback)(struct ll_node *node, void *p), void *param); /* stop when callback returns not NULL */
struct ll_node *llist_append(struct linked_list* list);
void llist_remove(struct linked_list* list, struct ll_node* node, void (*destroy_callback)(struct ll_node *node, void *p), void *param);

#ifndef STRIPPED
void llist_insertafter(struct linked_list* list, struct ll_node *node, struct ll_node *newnode);
void llist_generic_free_data(struct ll_node *node, void *param);
#endif

/*= hash map =*/
struct hash_map *hmap_create(size_t num_buckets, hashfn_t hash_fn);
void hmap_destroy(struct hash_map *map, void (*destroy_callback)(void *data));
void hmap_set(struct hash_map *map, void *key, void *data);
void *hmap_get(struct hash_map *map, void *key);
void hmap_generic_free_data(void *data);

#endif /* HASHMAP_H */
