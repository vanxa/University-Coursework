#!/usr/bin/python

import sys
# number of features seen with label 0
num_features_0 = 0
# number of features seen with label 1
num_features_1 = 0

tot_num_features = 0

# Compute counts first
for line in file('result_job1_task2'):
	label,count = line.strip().split('\t')
	if label == '0':
		num_features_0 += int(count)
	elif label == '1':
		num_features_1 += int(count)
	elif label == '2':
		tot_num_features += int(count)
	
# Now, update feature frequencies and pass to reducers
for line in sys.stdin:
	feature,freqs = line.strip().split('\t')
	freq_0 = int(freqs.split()[0])
	freq_1 = int(freqs.split()[1])
	freq_0 *= float(num_features_1) / float(tot_num_features)
	freq_1 *= float(num_features_0) / float(tot_num_features)
	print feature+"\t%f %f %d %d %d"%(freq_0,freq_1, num_features_0, num_features_1,tot_num_features)
