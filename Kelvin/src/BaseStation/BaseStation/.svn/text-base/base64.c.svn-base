/*
 * Copyright (C), 2000-2007 by the monit project group.
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <stdlib.h>
#include "base64.h"

#define SKIP_NON_BASE64(__buf) while(*__buf && !is_base64(*__buf)) { ++__buf; }


/* Private prototypes */
static int is_base64(char c);
static char encode(unsigned char u);
static unsigned char decode(char c);


/**
 *  Implementation of base64 encoding/decoding.
 *
 *  @author Jan-Henrik Haukeland, <hauk@tildeslash.com>
 *  @author Marco Elver, <marco.elver AT gmail.com> for PSoC
 *
 *  Marco: Modified to not use mallocs and string functions anymore.
 *
 *  @version \$Id: base64.c,v 1.19 2007/07/25 12:54:31 hauk Exp $
 *
 *  @file base64.c
 */

/* ------------------------------------------------------------------ Public */

size_t base64_encode_callback(unsigned char *src, size_t size, void (*callback)(char c, void *param), void* param)
{
	size_t i;
	size_t len_enc = 0;
	unsigned char b1, b2, b3;

	if(!src || !callback)
		return 0;

	for(i=0; i<size; i+=3) {

		b1=0;
		b2=0;
		b3=0;

		b1 = src[i];

		if(i+1<size)
			b2 = src[i+1];

		if(i+2<size)
			b3 = src[i+2];

		callback(encode(b1>>2), param);
		callback(encode(((b1&0x3)<<4)|(b2>>4)), param);

		if(i+1<size) {
			callback(encode(((b2&0xf)<<2)|(b3>>6)), param);
		} else {
			callback('=', param);
		}

		if(i+2<size) {
			callback(encode(b3&0x3f), param);
		} else {
			callback('=', param);
		}

		len_enc += 4;
	}

	return len_enc;

}

static void append_callback_(char c, void *param)
{
	char **p = (char**)param;
	*((*p)++) = c;
}

size_t base64_encode(char *dst, unsigned char *src, size_t size)
{
	size_t result;

	result = base64_encode_callback(src, size, append_callback_, (void*)(&dst));

	*dst = '\0';
	return result;
}


size_t base64_decode(unsigned char *dest, char *src, size_t dst_maxlen)
{
	if(src && *src) {

		unsigned char *p= dest;
		char c1, c2, c3, c4;
		unsigned char b1, b2, b3, b4;


		while(*src) {

			c1='A'; c2='A';
			c3='A'; c4='A';
			b1=0; b2=0;
			b3=0; b4=0;

			SKIP_NON_BASE64(src);
			if(*src)
				c1= *src++;

			SKIP_NON_BASE64(src);
			if(*src)
				c2= *src++;

			SKIP_NON_BASE64(src);
			if(*src)
				c3= *src++;

			SKIP_NON_BASE64(src);
			if(*src)
				c4= *src++;

			b1= decode(c1);
			b2= decode(c2);
			b3= decode(c3);
			b4= decode(c4);

			if(!dst_maxlen)
				break;
			*p++=((b1<<2)|(b2>>4) );
			--dst_maxlen;

			if(c3 != '=') {
				if(!dst_maxlen)
					break;
				*p++=(((b2&0xf)<<4)|(b3>>2) );
				--dst_maxlen;
			}

			if(c4 != '=') {
				if(!dst_maxlen)
					break;
				*p++=(((b3&0x3)<<6)|b4 );
				--dst_maxlen;
			}
		}

		return (p-dest);
	}

	return 0;
}


/* ----------------------------------------------------------------- Private */

/**
 * Base64 encode one byte
 */
static char encode(unsigned char u)
{
	if(u < 26)  return 'A'+u;
	if(u < 52)  return 'a'+(u-26);
	if(u < 62)  return '0'+(u-52);
	if(u == 62) return '+';

	return '/';
}


/**
 * Decode a base64 character
 */
static unsigned char decode(char c)
{
	if(c >= 'A' && c <= 'Z') return(c - 'A');
	if(c >= 'a' && c <= 'z') return(c - 'a' + 26);
	if(c >= '0' && c <= '9') return(c - '0' + 52);
	if(c == '+')             return 62;

	return 63;
}


/**
 * Return TRUE if 'c' is a valid base64 character, otherwise FALSE
 */
static int is_base64(char c)
{
	if((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
			(c >= '0' && c <= '9') || (c == '+')        ||
			(c == '/')             || (c == '=')) {

		return 1;

	}

	return 0;
}

