#!/usr/bin/python

import sys
import random

word_scores = {}

# Import the model first
for line in file('word_scores'):
	word, label = line.strip().split('\t')
	word_scores[word] = int(label)
	
	
# Next, read the test features and compute labels
for line in sys.stdin:
	word,feats = line.strip().split('\t')
	print "%d\t%s %d"%(random.randint(0,9),word,word_scores[word])
	
