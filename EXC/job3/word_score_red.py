#!/usr/bin/python

import sys

word_scores = {}

for line in sys.stdin:
	word, dat = line.strip().split('\t')
	score, num_feats = dat.split()
	try:
		if word_scores[word][0] < int(num_feats):
			word_scores[word] = {0:int(num_feats),1:int(score)}
	except KeyError:
		word_scores[word] = {0:int(num_feats),1:int(score)}
		
for word in word_scores:
	print "%s\t%d"%(word,word_scores[word][1])
