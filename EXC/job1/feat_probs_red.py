#!/usr/bin/python

import sys
import math

for line in sys.stdin:
	feature, data = line.strip().split('\t')
	freq0 = float(data.split()[0])
	freq1 = float(data.split()[1])
	num_feat_0 = float(data.split()[2])
	num_feat_1 = float(data.split()[3])
	tot_num_features = float(data.split()[4])
	likelihood_0 = math.pow(float(freq0+1)/float(freq0+freq1+2),math.log(float(tot_num_features)/float(freq0+freq1)+1))
	likelihood_1 = math.pow(float(freq1+1)/float(freq0+freq1+2),math.log(float(tot_num_features)/float(freq0+freq1)+1))
	print feature + "\t" + str(likelihood_0) + " " + str(likelihood_1)
