#!/usr/bin/python

import sys

feat_count = {}
for line in sys.stdin:
	feature, count_0, count_1 = line.strip().split('\t')[1].split()
	try:
		feat_count[feature]
		feat_count[feature][0] += int(count_0)
		feat_count[feature][1] += int(count_1)
	except KeyError:
		feat_count[feature] = {0:int(count_0),1:int(count_1)}
	
for feature in feat_count:
	print feature + "\t" + str(feat_count[feature][0]) + " " + str(feat_count[feature][1])
