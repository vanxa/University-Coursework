#!/usr/bin/env python
import hashlib as hl
import re
from subprocess import PIPE, Popen
import sys
from operator import itemgetter

TRAIN_DIR = "train/"
CORPUS_DIR = "0787128/"
# MD5 doesn't apparently hash all inputs to 128-bit hashes, so fixing the hash value to 128 bits will not always work: some hashes are 110-120 bits 
hash_size = 112

# Will be good to ignore some of the stop words, as they appear A LOT and do not provide any meaningful info regarding document similarity
stop_words = ['and','in','out','the','an','a','to','for','of']

# For FINN Plateau algorithm
INV_SLOPE = 100

############## DEBUG LEVEL ############## 
D_LEVEL = '-'

if D_LEVEL == 'C':
	def DEBUG(msg, _flag=1) : None
	def CONTROL():
		_continue = str.lower(raw_input("Do you want to continue? (y/n/s)"))
		if _continue == 'n':
			sys.exit(0)
		elif _continue == 'y':
			return 1
		elif _continue == 's':
			return 0

elif D_LEVEL == 'D':
	def DEBUG(msg, _flag=1): 
		if _flag == 1:
			print msg
	def CONTROL() : None
	
	
elif D_LEVEL == '-':
	def DEBUG(msg, _flag=1) : None
	def CONTROL() : None


elif D_LEVEL == 'C+D':
	def DEBUG(msg, _flag=1):
		if _flag == 1:
			print msg
	def CONTROL():
		_continue = str.lower(raw_input("Do you want to continue? (y/n/s)"))
		if _continue == 'n':
			sys.exit(0)
		elif _continue == 'y':
			return 1
		elif _continue == 's':
			return 0

		
############## CHOICE OF ALGORITHM ############## 
ALG = 'C'

if ALG == 'H': # Hamming
	def AL_PREPARE(txt,tok,index):
		txt_hash = sim_hash(get_doc_props(txt)[0])
		index[tok] = txt_hash
		return txt_hash,index
		
	def DISTANCE(doc_1,doc_2):
		return hamming_distance(doc_1,doc_2)
	
	# The hamming distance threshold: if distance between two documents is below threshold, they're duplicates (near or exact). Bit inflexible :(
	THRESH = hash_size / 8
		
elif ALG == 'C': # Word count algorithm
	def AL_PREPARE(txt,tok,index):
		index[tok] = get_doc_props(txt)
		txt_hash = sim_hash(index[tok][0]) 
		return txt_hash,index
		
	def DISTANCE(doc_1,doc_2):
		return doc_words_dist(doc_1,doc_2)
		
	# Threshold for word count algorithm: best thresh (tested using train data) is around 0.8: 1.0 means very close to exact duplicates (but they may not necessarily be exact duplicates, for example 5097977 and 2309398 score 1.0) 
	THRESH = 0.8

############## START ############# 
def start(_corpus='0787128',algorithm='all',outfile='output.txt'):
	print "Starting... Using text corpus: " + _corpus + "; Duplicates: " + algorithm+"; Output file: " + outfile
	if algorithm != 'finn':
		if ALG == 'H':
			print "Algorithm: Hamming distance"
		else:
			print "Algorithm: Word count distance" 
	filenames = []
	corpus = ''
	if re.search("0787128",str.lower(_corpus)):
		filenames = read_filenames(ls_corpus_dir(CORPUS_DIR))
		corpus = CORPUS_DIR
	elif re.search("train",str.lower(_corpus)):
		filenames = read_filenames(ls_corpus_dir(TRAIN_DIR))
		corpus = TRAIN_DIR
	
	duplicates = {} # This will get a list of all duplicates, found using the algorithm in task 3 and will be used for output
	# Flag: 0-find both exact and near duplicates; 1-find only exact duplicates; 2-find only near duplicates
	if re.search("exact", str.lower(algorithm)):
		exact_duplicates = find_exact_duplicates(filenames, corpus)
		return exact_duplicates
	elif re.search("near",str.lower(algorithm)):
		duplicates = find_near_duplicates(filenames, corpus)
		return duplicates
	elif re.search('finn',str.lower(algorithm)):
		duplicates = finn_duplicates(filenames,corpus)
		return duplicates
	elif re.search("all",str.lower(algorithm)):
		print "Finding exact duplicates...."
		exact_duplicates = find_exact_duplicates(filenames, corpus)
		print "Done. Finding near duplicates...."
		duplicates = find_near_duplicates(filenames, corpus)
		print "Done. Now, finding duplicates using Finn method..."
		finn = finn_duplicates(filenames,corpus)
		print "Done. Saving to file..."
		for dupl_pair in exact_duplicates:
			if dupl_pair in duplicates or [dupl_pair[1],dupl_pair[0]] in duplicates:
				continue # Ignore repeating lines
			else:
				duplicates += [dupl_pair]
		for dupl_pair in finn:
			if dupl_pair in duplicates or [dupl_pair[1],dupl_pair[0]] in duplicates:
				continue # Ignore repeating lines
			else:
				duplicates += [dupl_pair]
		save_to_file(duplicates,outfile)
		print "Done"
		return duplicates
		
############# EXACT DUPLICATES #############
# Algorithm for finding exact duplicates
def find_exact_duplicates(fl_nm,corpus):
	index = {}
	duplicates = []
	for tok in fl_nm:
		txt = preprocess_text(read_file(corpus + tok))
		tok = tok.replace('.txt','')
		_md5 = hl.md5()
		_md5.update(txt)
		_hash = _md5.digest()
		try:
			index[_hash] += [tok]
			duplicates += [index[_hash]] 
		except KeyError:
			index[_hash] = [tok]
	return pairs(duplicates)

# Converts the list of duplicates to an index for printing for file. The whole reason is for easier check that no line is repeated when outputing results from both algorithms
def pairs(lsts):
	_pairs = []
	for lst in lsts:
		lst_len = len(lst)
		i = 0
		if lst_len % 2 == 0:	 # odd number elements
			while i < lst_len:
				_pairs+= [[lst[i],lst[i+1]]]
				i += 2
		else:
			while i< lst_len - 1:
				_pairs += [[lst[i],lst[i+1]]]
				i += 2
			_pairs += [[lst[lst_len-1],lst[0]]]
	return _pairs
############# NEAR DUPLICATES ############# 
def find_near_duplicates(filenames,corpus):
	# Fix number of groups to 18; vary bits per group
	duplicates = []
	num_groups = 28
	DEBUG("Hash size is: " + str(hash_size))
	# For general cases, must enforce this to be an integer
	group_bit_size = hash_size/num_groups
	DEBUG("Group bit size is: " + str(group_bit_size))
	DEBUG("Number of groups is: " + str(num_groups))
	groups = [{} for i in range(num_groups)]
	doc_index = {}
	dist_index = {}
	DEBUG("Starting near duplicate calculation...")
	for tok in filenames:
		txt = read_file(corpus + tok)
		tok = re.sub('.txt','',tok)
		DEBUG("Document index is: " + str(doc_index))
		DEBUG("Got text file " + tok)
		txt_hash, doc_index = AL_PREPARE(txt,tok,doc_index) # Prepare necessary variables for document distance measurement
		DEBUG("Sim hash for text is: " + txt_hash)
		hash_groups = ['']*num_groups
		hash_index = 0
		seen_files = []
		# Don't want to use magic numbers in terms of hash groups
		while hash_index < num_groups:
			hash_groups[hash_index] = txt_hash[hash_index*group_bit_size:(hash_index+1)*group_bit_size]
			hash_index += 1
		DEBUG("Hash groups are: " + str(hash_groups))
		CONTROL()
		_index = 0
		while _index < num_groups:
			_hash = hash_groups[_index]
			DEBUG("Using hash value " + str(_hash))
			DEBUG("Index is: " + str(_index))
			DEBUG("The groups are: " + str(groups))
			try:
				DEBUG("Hashing using hash value " + str(_hash) + " into group " + str(groups[_index]))
				files = groups[_index][_hash]
				for fl in files:
					if fl not in seen_files:
						_dist = DISTANCE(doc_index[tok],doc_index[fl])
						if _dist >= THRESH:
							try:
								# Have an index for debugging purposes
								dist_index[_dist] += [{tok:fl}]
							except KeyError:
								dist_index[_dist] = [{tok:fl}]
							finally:
								duplicates += [[tok,fl]]
						seen_files += [fl]
				DEBUG("Got files: " + str(files))
				groups[_index][_hash] += [tok]
				DEBUG("Added document to group list. Current group contents are: ")
				print_group_contents(groups)
			except KeyError:
				DEBUG("No collision")
				groups[_index][_hash] = [tok]
				DEBUG("Added document to group list. Current group contents are: ")
				print_group_contents(groups)
			finally:
				_index += 1
		CONTROL()
	return duplicates
				
def sim_hash(words):
	DEBUG("\n\n\nThe words in the documents are:\n " + str(words))
	DEBUG("Starting...")
	word_index = make_word_index(words)
	DEBUG("All words processed. Word index is: "+str(word_index))
	weight_vector = compute_doc_vector(word_index)
	doc_vector = ""
	DEBUG("Vector: "+str(weight_vector))
	DEBUG("Finally...")
	for entry in weight_vector:
		if entry > 0:
			doc_vector += str(1)
		else:
			doc_vector += str(0)
	DEBUG("Document vector is: "+doc_vector)
	return doc_vector


#################### DOCUMENT DISTANCE MESAUREMENT ALGORITHMS #######################
# Computes the distance between two documents by counting the number of words occurring in both documents, normalized by the occurrence count and document length
def doc_words_dist(doc1_props, doc2_props):
	# If a word w is occurring in both documents, compute |w/len1-w/len2|; if smaller than error, add to similarity
	#_err = 0.13
	# Add up all words occurring in both documents, weighted by their average occurrence. If the score, divided by average document length (len1+len2)/2 is above threshold, documents are near duplicates
	#_doc_sim = 0.7 Use this thresh at the end
	score = 0
	tot_len = doc1_props[1] + doc2_props[1]
	for word in doc1_props[0].keys():
		if word in doc2_props[0].keys():
			score += doc1_props[0][word] + doc2_props[0][word]
	return float(score)/float(tot_len)
		

# Thank you wikipedia!
def hamming_distance(_hash1, _hash2):
	if len(_hash1) == len(_hash2):
   		return sum(bit1 != bit2 for bit1, bit2 in zip(_hash1, _hash2))
   		
#################### FRINN PLATEAU #######################   		

# find the plateau of a document: tokens are numbers, non-tokens are words
def finn_duplicates(filenames,corpus):
	# Store all documents that have a concentrated number of numbers
	# Only documents with at least len(doc)/inverted_slope numbers will be considered
	finn_docs = {}
	duplicates = []
	for fl in filenames:
		txt = preprocess_text(read_file(corpus + fl))
		tok = re.sub('.txt','',fl)
		words = txt.split()
		numbers = get_tokens(words)
		if len(numbers) >= float(len(words))/float(INV_SLOPE):
			print numbers
			finn_docs[tok] = [numbers] 
		keys = finn_docs.keys()
		for first in range(0,len(finn_docs)):
			for second in range(first+1,len(finn_docs)):
				sim = compare_plateaus(finn_docs[keys[first]],finn_docs[keys[second]])
				if sim >= 0.6: # find threshold
					duplicates += [keys[first],keys[second]]
	return duplicates


def find_plateau(words):
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
			curr_score = bfore_plateau + after_plateau + INV_SLOPE*plateau
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
		
def get_tokens(words):
	toks = []
	a,b = find_plateau(words)
	for i in range(a,b):
		if not val(words[i]):
			toks += [words[i]]
	return toks		
   		
   # Compares the plateaus of two documents, similar to doc_words_dist
def compare_plateaus(pl1, pl2):
	score = 0
	tot_len = len(pl1) + len(pl2)
	for num in pl1:
		if num in pl2:
			score += pl1.count(num) + pl2.count(num)
	return float(score)/float(tot_len)
############# HELPER FUNCTIONS ############# 

## Reads a file, given its name; Taken from Lab2Support
def read_file(filename):
	f = open(filename, 'r')
	try:
	    content = f.read()
	finally:
	    f.close()
	return content
	
## Removes the first sentence which will prevent the program from finding exact duplicates; will also remove non-characters and will lower all letters	
def preprocess_text(txt):
	first_sentence = re.sub('This(\s)+is+[^\n]*transcript[^\n]*\n','',txt)
	processed_txt = lower_remove_nonchars(first_sentence)
	return processed_txt

def lower_remove_nonchars(txt):
	return re.sub('[\.\'\^\,\:\;\"\<\>\\\/\{\}\[\]]','',str.lower(txt))
	
## List the contents of the corpus directory 
def ls_corpus_dir(_dir):
	return Popen(['ls',_dir],stdout=PIPE).stdout
	
## Read the file names inside the directory and return a list
def read_filenames(pipe):
	lst = []
	for line in pipe:
		lst += [line.rstrip('\n')]
	return lst
	
# Find the preferred hashcode size given a corpus
def test_hash_size(corpus):
	hash_size = 8
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
	return hash_size
	
def print_group_contents(group):
	if D_LEVEL != '-' or D_LEVEL != 'C':
		index = 0
		while index < len(group):
			DEBUG("Group " + str(index+1) + ": "+ str(group[index]))
			index += 1
			
# Returns a {doc:[{word:occurrence},doc_len]}, where the word:occurrence tuple is for each word in the document
def get_doc_props(doc):
	txt = preprocess_text(doc)
	words = txt.split()
	doc_len = len(words)
	seen_words = []
	props = [{}]
	for word in words:
		if word not in stop_words and word not in seen_words:
			props[0][word] = words.count(word)
			seen_words += [word]
		if word in stop_words and word not in seen_words:
			doc_len -= words.count(word)
			seen_words += [word]
	props += [doc_len]
	return props
	
def make_word_index(words):
	_flag = 1
	word_index = {}
	for word in words:
		DEBUG("Word: " + word, _flag)
		_md5 = hl.md5()
		_md5.update(word)
		_hash = bin(int(_md5.hexdigest(),16))
		word_hash = _hash[2:hash_size+2]
		DEBUG("md5: " + word_hash, _flag)
		DEBUG("Word index: " + str(word_index), _flag)
		DEBUG("Checking if word in index...", _flag)
		if word_hash not in word_index:
			DEBUG("Word not in index", _flag)
			word_count = words[word]
			DEBUG("Occurrences of word: "+str(word_count), _flag)
			word_index[word_hash] = word_count
		else:
			DEBUG("Word in index", _flag)
		if _flag != 0:
			_flag = CONTROL()
	return word_index
	
def compute_doc_vector(word_index):
	weight_vector = [0]*hash_size
	DEBUG("Calculating locality")
	_flag = 1
	for hash_key in word_index.keys():
		DEBUG("Hash key is: "+hash_key, _flag)
		DEBUG("Word occurrence is: " + str(word_index[hash_key]),_flag)
		bit_index = 0
		while(bit_index < len(hash_key)):
			bit = int(hash_key[bit_index])
			DEBUG("Bit  " + str(bit), _flag)
			if(bit == 0):
				bit = -1
			DEBUG("Bit after conversion: "+str(bit), _flag)
			weight_vector[bit_index] += bit*word_index[hash_key]
			bit_index += 1
		DEBUG("Vector after processing "+str(weight_vector), _flag)
		if _flag != 0:
			_flag = CONTROL()
	return weight_vector
	
# Taken from Lab2Support.py, with some modifications
def save_to_file(data, filename):
	d = []
	f = open(filename, 'w')
	try:
	    if type(data) == type(d):
			for fl1,fl2 in data:
				f.write(str(fl1)+"-"+str(fl2)+"\n")
	finally:
	    f.close()
