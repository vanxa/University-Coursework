#!/usr/bin/python

import sys
import random

for line in sys.stdin:
	feat,counts = line.split('\t')
	c0 = counts.split()[0]
	c1 = counts.split()[1]
	print "%d\t%s %s"%(random.randint(0,9),c0, c1)
	# Am using a random number between 0 and 9 (the number of reducers I have) as key
