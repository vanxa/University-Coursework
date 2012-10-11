import hashlib as hl
from detector import *
import re

# Want to find the preferred hash size, in order to get a unique b-bit hash value for every word in the corpus
def test(corpus):
	pref_hash_size = 8
	done = 0
	flnms = read_filenames(ls_corpus_dir(corpus))
	while(not done):
		done = 1
		for filename in flnms:
			txt= read_file(corpus+filename)
			words = txt.split()
			index = {}
			for word in words:
				word = str.lower(word)
				_md5 = hl.md5()
				_md5.update(word)
				_hash = bin(int(_md5.hexdigest(),16))
				_hash = _hash[len(_hash)-pref_hash_size:]
				try:
					index[_hash] += [word]
				except KeyError:
					index[_hash] = [word]
			keys = index.keys()
			for key in keys:
				word = index[key][0]
				if index[key].count(word) != len(index[key]):
					done = 0
					pref_hash_size += 1
					break
	return pref_hash_size
	
def diff(prop1, prop2):
	score = 0
	tot_len = prop1[1] + prop2[1]
	print "Total length: " + str(tot_len)
	for word in prop1[0].keys():
		if word in prop2[0].keys():
			score += prop1[0][word] + prop2[0][word]
	print "Score: " + str(score)
	return float(score)/float(tot_len)
	
def output(lst):
	lst_len = len(lst)
	index = {}
	i = 0
	if lst_len % 2 == 0:	 # odd number elements
		while i < lst_len:
			index[lst[i]] = lst[i+1]
			i += 2
	else:
		while i< lst_len - 1:
			index[lst[i]] = lst[i+1]
			i += 2
		index[lst[lst_len-1]] = lst[0]
	return index
	
def find_plateau(words):
	inv_slope = 100
	best_start = 0 # best_a
	best_stop = 0 # best_b
	score = 0
	bfore_plateau = 0 # L
	for start in range(0,len(words)-1): # start is a
		bfore_plateau += val(words[start])
		after_plateau = 0 # R
		plateau = 0 # M
		for stop in range(start+1,len(words)-1): # stop is b
			after_plateau -= val(words[stop])
			plateau += 1 - val(words[stop])
			curr_score = bfore_plateau + after_plateau + inv_slope*plateau
			if curr_score >= score:
				score = curr_score
				best_start = start
				best_stop = stop
	return best_start,best_stop
		
		
def val(tok):
	try:
		int(tok)
		return 0
	except ValueError:
		return 1
		
def get_tokens(doc):
	toks = []
	txt = preprocess_text(doc)
	words = txt.split()
	a,b = find_plateau(words)
	for i in range(a,b):
		if not val(words[i]):
			toks += [words[i]]
	return toks
	
