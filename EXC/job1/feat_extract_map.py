#!/usr/bin/python

import sys
import re
import random

features = {}

for line in sys.stdin:
	preprocessed = re.sub('\`|\!|\'|\"|\#|\$|\%|\^|}|{|;|\.|\,|:|\[|\]|\(|&|\)|(\\\)|\/|\+|\?|\-|\=|\~|\*|\@|\||_',' ',line)
	for word in preprocessed.split():
		if not word.isdigit():
			if len(word) > 3:
				feature_lst = []+[word]+[word[0:2]]+[word[0:3]]+[word[-2:]]+[word[-3:]] # Take needed features: length 5
			elif len(word) == 3:
				feature_lst = []+[word]+[word[0:2]]+[word[-2:]] # length 3
			else:
				feature_lst = []+[word] # length 1
			label = 0
			for ch in word:
				if not ch.isdigit() and ch.isupper():
					label = 1
					break
			for feature in feature_lst:
				feat = feature.lower()
				try:
					features[feat]
					try:
						features[feat][label] += 1
					except KeyError:
						features[feat][label] = 1
				except KeyError:
					features[feat] = {label:1,1-label:0}
				
				
# Here, I'm saving a lot of space, because my output is considerably smaller than my input: If I were to output a line such as:
# feature 1 label
# I would get a LOT more lines than I need
for feature in features.items():
	print "%d\t%s %d %d" % (random.randint(0,9),feature[0],feature[1][0],feature[1][1])
	
