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

/************************************************************************************************************************
*										DESCRIPTION OF THE IMPLEMENTATION											    *
*************************************************************************************************************************
*************************************************************************************************************************
* The program is designed around the task farm method (although the number of tasks is not known from the beginning).   *
*																														*
*													FARMER																*
* The farmer is responsible for dynamically allocating tasks to idle workers, while keeping idle time to a minimum.		*
* To do so, it uses an array, worker_status, in which the status of each worker (either 0 for idle, or 1 for busy) is	*
* stored. Furthermore, the farmer keeps track on how many tasks it has sent to the workers, which have not been 		*
* completed yet, given by num_tasks. This variable is essential as it is one of the two components of the main control  *
* loop, in which the program runs. 																						*
*																														*
* On startup, the farmer sends the initial task, defined as (A,B) to worker 1 and initializes the num_tasks variable 	*
* to 1 and then enters the main control loop. The loop terminates when both the task stack is empty and there are no    *
* more tasks, whose results are pending (i.e. num_tasks = 0). The variable num_tasks is necessary to ensure that the	*
* farmer does not exit the loop early if the stack is empty. but the workers busy executing tasks. 						*
*																													    *
* Within the control loop, the farmer checks the status of each worker (in consecutive order, defined by the worker's   *
* id). If the worker is idle and the stack is not empty, the farmer sends a new task for execution and updates the      *
* worker's status (also, num_tasks is increased by 1). Otherwise, the farmer waits and collects the result from the		*
* same worker. This is done by first probing for messages using the MPI_Probe primitive (the choice of a blocking probe *
* is discussed below), in order to determine the size of the message. Two different sizes are considered: 1 and 4, 		*
* which correspond to a computed result and two new tasks, respectively. MPI_Probe uses the specific worker id instead	*
* of a wildcard (i.e. MPI_ANY_SOURCE), in order to ensure a happened-before message relation: if the farmer sends a 	*
* task to worker 1 and then a task to worker 2, it should receive the results in that exact order. This was necessary 	*
* to allow the farmer to uniformly distribute tasks among the workers.													*
*																														*
* If the received message contains a computed result, it is added to the total area, and if the stack is not empty, a 	*
* new task is sent to the worker. If no tasks are available, the worker's status is set to 'idle' and num_tasks is 		*
* decremented. If two more tasks are received, the farmer pushes the first task onto the stack and immediately sends 	*
* the second to the same worker, so that idle time is kept to a minimum. 												*
*																														*
* When all tasks have been completed and the final area has been computed, the farmer exits the control loop and sends 	*
* a shutdown message, which consists of a 'pseudo-task' (0,0) and a special tag 100.									*
*																														*
*													WORKER																*
* The worker processes run until they receive a message with tag 100, in which case they terminate. Before entering the *
* main control loop, they block until a message is received from the farmer using the MPI_Recv primitive. They extract	*
* the message tag, used to break from the loop, and then proceed to execute the task. The received task consists of the *
* start and end points, stored in a doube array of size 2, and the area between those points is computed as done in the *
* sequential version of the program. When a result is computed, the worker sends it back to the worker and blocks for 	*
* the next message. If a result cannot be computed, two new tasks are created and sent back to the farmer. In this case *
* the message is a double array of size 4. 																				*
*																														*
* The worker processes always receive messages of the same size (2) and therefore do not need to use MPI_Probe. 		*
*																														*
*											CHOICE OF PRIMITIVES														*
*																														*
* The MPI primitives that were used are the following: blocking receive MPI_Recv, standard blocking send MPI_SEND, 		*
* blocking probe MPI_Probe and MPI_Get_counts for counting the message length. Blocking receive ensures that workers 	*
* do not continuously loop around receiving messages (as they only perform tasks when a message is received), while 	*
* the farmer's function does not depend on what type is used: it has already blocked on MPI_Probe until a message is 	*
* received, thus MPI_Irecv and MPI_Recv would produce the same results. A blocking send, MPI_Send, was used, because 	*
* memory for the data structure storing the message is freed immediately after sending. Thus, to prevent the program	*
* from crashing, the process (either worker of farmer) should block until the data has been copied elsewhere before 	*
* freeing the memory used to store it. An alternative would be to use MPI_Isend and to wait until it returns 			*
* MPI_SUCCESS before freeing allocated memory.																			* 
*																														*
* A blocking probe is used to check the size of the message, since two different size messages must be handled by 		*
* the farmer. Because a specific worker id is used for the source value, the farmer blocks until a message from that	*
* worker has arrived: if other workers have also sent data, it is processed at a later stage. This behavior would not	*
* make sense if different workers were executing tasks that do not depend on each other to produce the final result and *
* tasks differ in complexity and execution time. In this case, all tasks must be completed before the area is computed  *
* and therefore the farmer will eventually have to wait on all workers to finish before producing a result. 			*
*																														*
*************************************************************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <mpi.h>
#include "stack.h"

#define EPSILON 1e-3
#define F(arg)  cosh(arg)*cosh(arg)*cosh(arg)*cosh(arg)
#define A 0.0
#define B 5.0

#define SLEEPTIME 1

int *tasks_per_process;

double farmer(int);

void worker(int);

int main(int argc, char **argv ) {
  int i, myid, numprocs;
  double area, a, b;

  MPI_Init(&argc, &argv);
  MPI_Comm_size(MPI_COMM_WORLD,&numprocs);
  MPI_Comm_rank(MPI_COMM_WORLD,&myid);

  if(numprocs < 2) {
    fprintf(stderr, "ERROR: Must have at least 2 processes to run\n");
    MPI_Finalize();
    exit(1);
  }

  if (myid == 0) { // Farmer
    // init counters
    tasks_per_process = (int *) malloc(sizeof(int)*(numprocs));
    for (i=0; i<numprocs; i++) {
      tasks_per_process[i]=0;
    }
  }

  if (myid == 0) { // Farmer
    area = farmer(numprocs);
  } else { //Workers
    worker(myid);
  }

  if(myid == 0) {
    fprintf(stdout, "Area=%lf\n", area);
    fprintf(stdout, "\nTasks Per Process\n");
    for (i=0; i<numprocs; i++) {
      fprintf(stdout, "%d\t", i);
    }
    fprintf(stdout, "\n");
    for (i=0; i<numprocs; i++) {
      fprintf(stdout, "%d\t", tasks_per_process[i]);
    }
    fprintf(stdout, "\n");
    free(tasks_per_process);
  }
  MPI_Finalize();
  return 0;
}

double farmer(int numprocs) {
	/**
	* Initialize the variables as follows :
	* worker_status: stores the current status for each process: idle (0) or busy (1)
	* num_tasks: stores the number of tasks that are sent to the worker processes (<= numprocs). Used for setting the main control loop
	* count: the data length: 1 if the worker sends a computed result, 4 if the worker sends 2 new tasks
	* task: stores a task
	* area: the resulting area
	* tag: the worker's tag, currently its id
	* who: the id of the worker, from 1 to numprocs-1
	* stack : the stack holding the tasks
	* status : the MPI status
	*/
	int worker_status[numprocs], i, num_tasks, count, tag, who;
	double *data, area, temp[2];
	stack *stack = new_stack();
	MPI_Status status;

	// Initialize the worker status array, set to idle
	for(i=0;i<numprocs;i++)
	{
		worker_status[i]=0; 
	}

	// Send the initial task to worker 1
	data = (double *) malloc (2*sizeof(double));
	data[0] = A;
	data[1] = B;
	MPI_Send(data, 2, MPI_DOUBLE, 1, 1, MPI_COMM_WORLD);
	worker_status[1] = 1;
	free(data);
	tasks_per_process[1]++; // don't forget to increment the worker's task counter
	num_tasks++; // Necessary in order to enter the control loop
	while(num_tasks != 0 || is_empty(stack) == 0) // The farmer finishes when there are no more tasks in the stack and the workers have completed their last tasks
	{
		for(i=1;i<numprocs;i++) // Iterate through all workers
		{
			if(worker_status[i] == 0) // Worker is idle
			{
				if(is_empty(stack) == 0) // Send new task
				{
					data = pop(stack);
					tasks_per_process[i]++;
					MPI_Send(data, 2, MPI_DOUBLE, i,i,MPI_COMM_WORLD);
					worker_status[i] = 1;
					num_tasks++;			
				}				
			}
			else // Worker is busy, retrieve results
			{
				if(num_tasks == 0)
				{
					break;
				}
				MPI_Probe(i,MPI_ANY_TAG,MPI_COMM_WORLD, &status); // Block on probing 
				MPI_Get_count(&status, MPI_DOUBLE, &count); // Get data length
				who = status.MPI_SOURCE; // Get worker source
				tag = status.MPI_TAG; // Should be same with above, so not really necessary
				if(count == 1) // This is a computed result
				{
					data = (double *)malloc(sizeof(double));
					MPI_Recv(data, 1, MPI_DOUBLE, who, tag, MPI_COMM_WORLD, &status);
					area += data[0]; // Add to final output
					free(data);
					if(is_empty(stack) == 1) 
					{ // No tasks are available, so worker should stay idle
						worker_status[who] = 0; 
						num_tasks--; // Decrease the number of tasks that are currently executed by workers
					}
					else
					{ // Send a new task
						data = pop(stack);
						tasks_per_process[who]++;
						MPI_Send(data, 2, MPI_DOUBLE, i,i,MPI_COMM_WORLD);
					}					
				}
				else if(count == 4) // Worker sends two new tasks
				{
					data = (double *)malloc(4*sizeof(double));
					MPI_Recv(data, 4, MPI_DOUBLE, who, tag, MPI_COMM_WORLD, &status);
					temp[0] = data[0];
					temp[1] = data[1];
					push(temp,stack); // Push first task onto the stack
					temp[0] = data[2];
					temp[1] = data[3];
					MPI_Send(&temp, 2, MPI_DOUBLE, who, tag, MPI_COMM_WORLD); // Send the second task to the same worker
					tasks_per_process[who]++;
					free(data);
				}
			}
		}
	}

	// Finish
	temp[0] = 0; // For convenience, instead of having the worker probe each time what type of message it is receiving, simply send a pseudo-task
	temp[1] = 0;
	for(i =1;i<numprocs;i++)
	{
		MPI_Send(&temp, 2, MPI_DOUBLE, i, 100, MPI_COMM_WORLD); // 100 is SHUTDOWN
	}
	return area;
}

void worker(int mypid) {
	/**
	* Initialize variables as follows :
	* task : the received task
	* result : the computed result
	* new_tasks : the derived tasks 
	* tag : the messag tag, used to terminate the process
	* status : MPI status variable
	* computation variables : left and right correspond to task[0] and task[1], rest is computed as in the sequential version
	*/	
	double task[2], result, new_tasks[4], left,right,mid, fmid, larea, rarea, fleft, fright, lrarea;
	int tag;
	MPI_Status status;

	// Receive the first task
	MPI_Recv(&task, 2, MPI_DOUBLE, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
	tag = status.MPI_TAG;

	while (tag != 100) // 100 is SHUTDOWN
	{
		usleep(SLEEPTIME);  // Sleep now
	
		// Perform computations: like in the sequential version
		left = task[0];
		right = task[1];
		fleft = F(left);
		fright = F(right);
		lrarea = (F(left)+F(right))*(right-left)/2;
		mid = (left+right) /2;
		fmid = F(mid);
		larea = (fleft+fmid)*(mid-left) / 2;
		rarea = (fmid + fright) * (right - mid) / 2;
		
		if(fabs((larea+rarea)-lrarea) > EPSILON)
		{ // Create two new tasks
			
			new_tasks[0] = left;
			new_tasks[1] = mid;
			new_tasks[2] = mid;
			new_tasks[3] = right;
	
			MPI_Send(&new_tasks, 4, MPI_DOUBLE, 0, tag, MPI_COMM_WORLD);
		}
		else
		{ // Compute result
			result = (larea + rarea);
			MPI_Send(&result, 1, MPI_DOUBLE, 0, tag, MPI_COMM_WORLD);
		}
		
		MPI_Recv(&task, 2, MPI_DOUBLE, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &status); // Receive next task
		tag = status.MPI_TAG; // Update the tag
	}    
}
