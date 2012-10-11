#!/usr/bin/python

import sys
import re

word_feats = {}

for line in sys.stdin:
	preprocessed = re.sub('\`|\!|\'|\"|\#|\$|\%|\^|}|{|;|\.|\,|:|\[|\]|\(|&|\)|(\\\)|\/|\+|\?|\-|\=|\~|\*|\@|\||_',' ',line.strip())
	for word in preprocessed.split():
		if not word.isdigit():
			if len(word) > 3:
				feature_lst = []+[word]+[word[0:2]]+[word[0:3]]+[word[-2:]]+[word[-3:]] # Take needed features: length 5
			elif len(word) == 3:
				feature_lst = []+[word]+[word[0:2]]+[word[-2:]] # length 3
			else:
				feature_lst = []+[word] # length 1
			word_feats[word.lower()] = feature_lst
				
for word in word_feats:
	print word + "\t" + ' '.join([feat for feat in word_feats[word]])
