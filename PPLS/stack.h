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

/* stack.h
 *
 * University of Edinburgh
 * 
 * Stack containing an array of doubles
 */

typedef struct stack_node_tag stack_node;
typedef struct stack_tag stack;

struct stack_node_tag {
  double     data[2];
  stack_node *next;
};

struct stack_tag {
  stack_node     *top;
};


stack *new_stack();
void free_stack(stack *);

void    push(double *, stack *);
double *pop (stack *);

int is_empty (stack *);
