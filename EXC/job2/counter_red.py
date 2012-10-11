#!/usr/bin/python

import sys

word_feats = {}

for line in sys.stdin:
	word,feats = line.strip().split('\t')
	feats = feats.split()
	try:
		word_feats[word]
	except KeyError:
		word_feats[word] = feats
		
for word in word_feats:
	print word + "\t" + ' '.join([feat for feat in word_feats[word]])
