#!/usr/bin/python

import sys

for line in sys.stdin:
	word,label = line.strip().split('\t')[1].split()
	new_word = word
	if label == str(1):
		new_word = word[0].upper()+word[1:]
	print "%s\t%s"%(word,new_word)
