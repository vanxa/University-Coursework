/**	This file is part of DVS128-Robot-Control.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DVS128-Robot-Control is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

// Simple type for stack of doubles
#include <stdio.h>
#include <stdlib.h>
#include <obstack.h>
#include "stack.h"

// creating a new stack
stack * new_stack()
{
  stack *n;

  n = (stack *) malloc (sizeof(stack));

  n->top = NULL;
  
  return n;
}

// cleaning up after use
void free_stack(stack *s)
{
  free(s);
}

// Push data to stack s, data has to be an array of 2 doubles
void push (double *data, stack *s)
{
  stack_node *n;
  n = (stack_node *) malloc (sizeof(stack_node));
  n->data[0]  = data[0];
  n->data[1]  = data[1];

  if (s->top == NULL) {
    n->next = NULL;
    s->top  = n;
  } else {
    n->next = s->top;
    s->top = n;
  }
}

// Pop data from stack s
double * pop (stack * s)
{
  stack_node * n;
  double *data;
  
  if (s == NULL || s->top == NULL) {
    return NULL;
  }
  n = s->top;
  s->top = s->top->next;
  data = (double *) malloc(2*(sizeof(double)));
  data[0] = n->data[0];
  data[1] = n->data[1];
  free (n);

  return data;
}

// Check for an empty stack
int is_empty (stack * s) {
  return (s == NULL || s->top == NULL);
}
