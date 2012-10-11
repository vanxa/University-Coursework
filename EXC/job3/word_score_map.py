#!/usr/bin/python

import sys

feat_probs = {}

p_0 = 0.45
p_1 = 0.55

# Import the model first
for line in file('model'):
	feature,probs = line.strip().split('\t')
	prob_0 = float(probs.split()[0])
	prob_1 = float(probs.split()[1])
	feat_probs[feature] = {0:prob_0,1:prob_1}
	
# Next, read the test features and compute labels
reduce_index = 0
for line in sys.stdin:
	word,feats = line.strip().split('\t')
	score_0 = p_0
	score_1 = p_1
	for feature in feats.split():
		trained_feats = 0
		if feature in feat_probs:
				trained_feats += 1
				score_0 *= feat_probs[feature][0]
				score_1 *= feat_probs[feature][1]
		if score_0 > score_1:
			print "%s\t0 %d"%(word, trained_feats)
		else:
			print "%s\t1 %d"%(word, trained_feats)

