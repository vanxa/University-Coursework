/**
 * @file hashmap.c
 *
 * @author Marco Elver <marco.elver AT gmail.com>
 * @date Fri Oct  8 19:34:34 BST 2010
 */

#include <stdlib.h>

#ifdef _M8C
  #define OVERRIDE_MALLOC
  #include "dynmem.h"
#endif

#include "hashmap.h"

/*== linked list implementation ==*/

static void _llist_init_content(struct linked_list *list)
{
	list->head = NULL;
	list->tail = NULL;
#ifndef STRIPPED
	list->elements = 0;
#endif
}

struct linked_list *llist_create(void)
{
	struct linked_list *list;

	if(!(list = (struct linked_list*)malloc(sizeof(struct linked_list)))) {
		ERROR("llist_create: out of memory");
		return NULL;
	}

	_llist_init_content(list);

	return list;
}

static void _llist_destroy_content(struct linked_list* list, void (*destroy_callback)(struct ll_node *node, void *p), void *param)
{
	struct ll_node *current;
	struct ll_node *next;

	current = list->head;
	while(current) {
		next = current->next;
		if(destroy_callback)
			destroy_callback(current, param);
		free(current);
		current = next;
	}

}

void llist_destroy(struct linked_list* list, void (*destroy_callback)(struct ll_node *node, void *p), void *param)
{
	_llist_destroy_content(list, destroy_callback, param);
	free(list);
}

struct ll_node *llist_search(struct linked_list *list, struct ll_node *(*callback)(struct ll_node *node, void *p), void *param)
{
	struct ll_node *current;
	struct ll_node *result = NULL;

	if(!callback)
		return NULL;

	current = list->head;
	while(current) {
		result = callback(current, param);
		if(result)
			break;
		current = current->next;
	}

	return result;
}

#ifndef STRIPPED
void llist_insertafter(struct linked_list* list, struct ll_node *node, struct ll_node *newnode)
{
	if(!list || !node || !newnode)
		return;

	if(node->next) {
		newnode->next = node->next;
		newnode->prev = node;
		newnode->next->prev = newnode;
		node->next = newnode;
	} else { /* end of list */
		/* sanity check */
		if(node != list->tail) {
			ERROR("llist_insertafet: there is somethis seriously wrong here!");
			return;
		}

		node->next = newnode;
		newnode->next = NULL;
		newnode->prev = node;
		list->tail = newnode;
	}

	++list->elements;
}
#endif

struct ll_node *llist_append(struct linked_list* list)
{
	struct ll_node *node;

	if(!list)
		return NULL;

	if(!(node = (struct ll_node*)malloc(sizeof(struct ll_node)))) {
		ERROR("llist_append: out of memory!");
		return NULL;
	}

	node->prev = list->tail;
	
	if(list->tail)
		list->tail->next = node;
	else
		list->head = node;

	list->tail = node;

	node->data = NULL;
	node->next = NULL;

#ifndef STRIPPED
	++list->elements;
#endif
	return node;
}

void llist_remove(struct linked_list* list, struct ll_node* node, void (*destroy_callback)(struct ll_node *node, void *p), void *param)
{
	if(!list || !node)
		return;

	if(destroy_callback)
		destroy_callback(node, param);

	if(node->prev)
		node->prev->next = node->next;
	else
		list->head = node->next;

	if(node->next)
		node->next->prev = node->prev;
	else
		list->tail = node->prev;

#ifndef STRIPPED
	--list->elements;
#endif
	free(node);
}

#ifndef STRIPPED
void llist_generic_free_data(struct ll_node *node, void *param)
{
	if(node->data)
		free(node->data);
}
#endif

/*== hash map implementation ==*/

struct keydata_pair {
	size_t hashed_key;
	void *data;
};

struct hash_map *hmap_create(size_t num_buckets, hashfn_t hash_fn)
{
	struct hash_map* map;
	size_t i;

	if(!(map = (struct hash_map*)malloc(sizeof(struct hash_map)))) {
		ERROR("hmap_create: out of memory!");
		return NULL;
	}

	map->num_buckets = num_buckets;
	map->hash_fn = hash_fn;

	if(!(map->buckets_array = (struct linked_list*)malloc(sizeof(struct linked_list)*num_buckets))) {
		ERROR("hmap_create: out of memory! (2)");
		free(map);
		return NULL;
	}

	for(i=0;i<num_buckets;++i) {
		_llist_init_content(&map->buckets_array[i]);
	}

	return map;
}

static void _destroy_keydata(struct ll_node *node, void *param)
{
	struct keydata_pair *kdp = (struct keydata_pair*)node->data;
	if(kdp) {
		void (*destroy_callback)(void *data) = (void (*)(void*))param;
		if(destroy_callback)
			destroy_callback(kdp->data);
		free(kdp);
	}
}

void hmap_destroy(struct hash_map *map, void (*destroy_callback)(void *data))
{
	size_t i;

	if(!map)
		return;

	for(i=0;i<map->num_buckets;++i) {
		_llist_destroy_content(&map->buckets_array[i], _destroy_keydata, (void*)destroy_callback);
	}

	free(map->buckets_array);
	free(map);
}

static struct ll_node *_find_key(struct ll_node *node, void *param)
{
	size_t hash_to_find = *((size_t*)param);
	struct keydata_pair *kdp = (struct keydata_pair*)node->data;
	
	if(kdp->hashed_key == hash_to_find)
		return node;

	return NULL;
}

void hmap_set(struct hash_map *map, void *key, void *data)
{
	size_t hashed_key = map->hash_fn(key);
	struct linked_list *list = &map->buckets_array[hashed_key % map->num_buckets];
	struct ll_node *node;

	node = llist_search(list, _find_key, (void*)&hashed_key);
	if(!node) {
		if(!(node = llist_append(list))) {
			return;
		}
	}

	/* set data */
	{
		struct keydata_pair *kdp;

		if(!node->data) {
			if(!(node->data = (struct keydata_pair*)malloc(sizeof(struct keydata_pair)))) {
				llist_remove(list, node, NULL, NULL);
				ERROR("hmap_set: out of memory!");
				return;
			}
		}

		kdp = (struct keydata_pair*)node->data;
		kdp->hashed_key = hashed_key;
		kdp->data = data;
	}
}

void *hmap_get(struct hash_map *map, void *key)
{
	size_t hashed_key = map->hash_fn(key);
	struct linked_list *list = &map->buckets_array[hashed_key % map->num_buckets];
	struct ll_node *node;

	node = llist_search(list, _find_key, (void*)&hashed_key);

	if(!node)
		return NULL;

	return ((struct keydata_pair*)node->data)->data;
}

void hmap_generic_free_data(void *data)
{
	if(data)
		free(data);
}

