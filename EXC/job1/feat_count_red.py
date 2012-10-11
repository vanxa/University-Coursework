#!/usr/bin/python

import sys

count_0 = 0 
count_1 = 0
num_feats = 0

for line in sys.stdin:
	key,values = line[:-1].split('\t')
	values = values.split()
	count_0 += int(values[0])	
	count_1 += int(values[1])
	num_feats += 1
	
print "0\t%d"%count_0
print "1\t%d"%count_1
print "2\t%d"%num_feats
