from nltk.corpus import words, wordnet as wn
import time
from Lab2Support import *
import translate as tr
import heapq

dict_index = {} # Contains all words from the words corpora, indexed in alphabetical order and length. For easy access
doc_index = {} # Contains all the documents, indexed by numerical key. Value is the list of words contained in doc, translated and error corrected
inverted_index = {} # The inverted index, representative words are key, value is the list of document keys containing this word (or its synonyms)
word_index = {} # Contains a set of representative words, which define the synonym groups. Key is any word found in the documents, value is the rep for that word 
translate_index = {} # Contains all translated words. For easier and faster translation, since Google strictly controls access times for APIs
corrected_index = {} # Contains all already corrected words. For easier and faster error correction
query_index = {}
translate_queue = []
TIME_THRESH = 4
doc_file = "a2.docs"

avg_doc_len = 0



# START FILE: Starts the whole task
def start(q_file, with_translation = 0):
	global avg_doc_len
	if(q_file == "train"):
		q_file = "a2.qrys"
	else:
		q_file == "test.qrys"
	build_dictionary_index() # Do some processing on nltk's words corpus, so it's easier to access when doing spell check
	(queries, docs) = read_files(q_file) # There is only 1 doc file, so don't need to specify that
	docs = process_docs(docs, with_translation,1) # Will add words to inverted index and word_index, will spell check them, correct them if necessary, or translate them to english
	queries = process_queries(queries, with_translation,0)
	avg_doc_len = compute_avg_doc_len(docs)
	do_tfidf(queries,docs)
	
def do_tfidf(queries,docs):
	output = ''
	for query in queries:
		if(query):
			for doc in docs:
				if(doc):
					q_id = query[0]
					doc_id = doc[0]
					w_sum = compute_tfidf(query,doc)
					output+= str(q_id) + " 0 "+ str(doc_id) + " 0 "+ str(w_sum) + " 0\n"
			
	saveToFile(output,'best.top')
	
# BUILD: dictionary	
def build_dictionary_index():
	global dict_index
	s = set(words.words('en'))
	for word in s:
		if len(word) > 2: # Ignore smallest words
			word = str.lower(word)
			try:
				dict_index[word[0]]
				try:
					dict_index[word[0]][len(word)] += [word]
				except KeyError:
					dict_index[word[0]][len(word)] = [word]
				except TypeError:
					print dict_index[word[0]][len(word)]
			except KeyError:
				dict_index[word[0]] = {len(word):[word]}

# READ: files		
def read_files(q_file):
	queries = readFile(q_file).split('\n')
	docs = readFile(doc_file).split('\n')
	return(queries,docs)
	
	
# PROCESS: document	
def process_doc(doc, flag):
	global doc_index, inverted_index, word_index, corrected_index,translate_index,translate_queue
	if doc != '':
		doc_lst = doc.split()
		doc_id = int(doc_lst[0]) # For indexing the document index
		print "Processing doc "+  doc_lst[0]
		words = doc_lst[1:]
		#print words
		last_time = 0
		for word in words:
			if word != '':
				word = str.lower(word)
				wn_chk = wn.synsets(word)
				if(wn_chk):
					words = process_word(word,doc_id, words,'')
					(last_time, word) = check_queue_for_translation(last_time)
					if(word != ''):
						words = process_word(word, doc_id, words, '')
				else:
					#print "Not found in wordnet"
					# Two options here: errors or foreign. So, test for language, if result is english, do error correction; otherwise translate
					# First: check for any non-characters that may confuse dictionary check: remove non-chars, concat strings or place '-' and try again
					found = 0
					word_split = re.split('\W',word)
					if len(word_split) != 1:
						new_word = ''
						# First, concatenate the parts of the word
						for part in word_split:
							new_word += part
						wh_chk = wn.synsets(new_word)
						if(wh_chk):
							words = process_word(new_word,doc_id,words,word)
							found = 1
						else:
							# Concatenation failed. Try again, this time placing '-' between word parts
							new_word = ''
							for part in word_split:
								new_word += "-"+part
							new_word = new_word[1:] # Get rid of the first '-'
							wh_chk = wn.synsets(new_word)
							if(wh_chk):
								words = process_word(new_word,doc_id,words,word)
								found = 1
					if(not found):
						#now_time = time.time()
						#leng = None
						#if now_time - last_time > TIME_THRESH:
							#leng = tr.detect_language(word) # At least during single-word tests, google translate have shown considerable accuracy when dealing with words that contain errors. For example, the word 'mounain', which may sound a bit French, is determined as english. Which is good
							#last_time = time.time()
						#else:
							#time.sleep(TIME_THRESH-(now_time - last_time))
							#leng = tr.detect_language(word)
						if(flag):
							print ('Trying to translate...')
							if(leng):
								leng = leng['language']
								if leng != 'en':
									print "Translating..."
									try:
										print "Already translated"
										tr_word = translate_index[word] # Word has already been translated. No need to ask google for translation
										words = process_word(tr_word,doc_id,words,word)
									except KeyError:
										res = translate(word,leng)
										if res['responseStatus'] == 403:
											print "GOT 403"
											heapq.heappush(translate_queue, (word,leng))
											increase_time_thresh(1)
										else:
											print "Translated"
											tr_word = str.lower(str(res['responseData']['translatedText']))
											translate_index[word] = tr_word
											words = process_word(tr_word,doc_id,words,word)
								else:
									print "Correcting word"
									try:
										cor_word = corrected_index[word]
										print "Already corrected"
									except KeyError:
										cor_word = correct_errors(word)
										corrected_index[word] =  cor_word
									finally:
										words = process_word(cor_word,doc_id,words,word) # We assume that word can now be found in wordnet
							else:
								print "GOT 403"
						else:
							#if(leng and leng['language'] == 'en'):
								# Don't want to translate. Do error correction on all words
								#print "Correcting word"
								try:
									cor_word = corrected_index[word]
									#print "Already corrected"
								except KeyError:
									cor_word = correct_errors(word)
									corrected_index[word] =  cor_word
								finally:
									words = process_word(cor_word,doc_id,words,word) # We assume that word can now be found in wordnet
		doc_index[doc_id] = words
		#print words
		return [doc_id] + words
	else:
		return []					


# PROCESS: query
def process_query(query,flag):
	global corrected_index,translate_index,translate_queue, query_index
	if query != '':
		query_lst = query.split()
		query_id = int(query_lst[0]) # Query id
		print "Processing query " + query_lst[0]
		words = query_lst[1:]
		last_time = 0
		for word in words:
			if word != '':
				word = str.lower(word)
				# Check if word in wordnet
				wn_chk = wn.synsets(word)
				if(wn_chk):
					words = check_query_word(word, words,query_id, '')
				# Not in wordnet
				else:
					# Two options here: errors or foreign. So, test for language, if result is english, do error correction; otherwise translate
					# First: check for any non-characters that may confuse dictionary check: remove non-chars, concat strings or place '-' and try again
					found = 0
					word_split = re.split('\W',word)
					if len(word_split) != 1:
						new_word = ''
						# First, concatenate the parts of the word
						for part in word_split:
							new_word += part
						wh_chk = wn.synsets(new_word)
						if(wh_chk):
							# Check in word_index
							words = check_query_word(new_word, words,query_id, word)
							found = 1
						else:
							# Concatenation failed. Try again, this time placing '-' between word parts
							new_word = ''
							for part in word_split:
								new_word += "-"+part
							new_word = new_word[1:] # Get rid of the first '-'
							wh_chk = wn.synsets(new_word)
							if(wh_chk):
								words = check_query_word(new_word, words,query_id, word)
								found = 1
					if(not found):
						now_time = time.time()
						#if now_time - last_time > TIME_THRESH:
							#leng = tr.detect_language(word) # At least during single-word tests, google translate have shown considerable accuracy when dealing with words that contain errors. For example, the word 'mounain', which may sound a bit French, is determined as english. Which is good
							#last_time = time.time()
						if(flag): # If I want translation enabled
							print ('Trying to translate...')
							if(leng):
								leng = leng['language']
								if leng != 'en':
									print "Translating..."
									try:
										print "Already translated"
										tr_word = translate_index[word] # Word has already been translated. No need to ask google for translation
										words = process_word(tr_word,doc_id,words,word)
									except KeyError:
										res = translate(word,leng)
										if res['responseStatus'] == 403:
											print "GOT 403"
											heapq.heappush(translate_queue, (word,leng))
											increase_time_thresh(1)
										else:
											print "Translated"
											tr_word = str.lower(str(res['responseData']['translatedText']))
											translate_index[word] = tr_word
											words = check_query_word(tr_word,words,query_id,word)
								else:
									cor_word = correct_errors(word)
									words = check_query_word(cor_word,words,query_id,word) # We assume that word can now be found in wordnet
							else:
								print "GOT 403"
						else:
							#if(and leng['language'] == 'en'):
								# Don't want to translate. Do error correction on all words
							try:	
								cor_word = corrected_index[word]
								#print "Already corrected"
							except KeyError:
								cor_word = correct_errors(word)
								corrected_index[word] =  cor_word
								words = check_query_word(cor_word, words,query_id, word)
								
		query_index[query_id] = words
		return [query_id] + words
	else:
		return []


# TRANSLATE: increase sleep period between consecutive translations
def increase_time_thresh(quant):
	global TIME_THRESH
	if TIME_THRESH <= 9:
		TIME_THRESH += quant


# TRANSLATE: check the translate queue for available words for translation					
def check_queue_for_translation(last_time):
	global translate_queue
	now_time = time.time()
	if ((now_time - last_time >= TIME_THRESH) and (translate_queue != [])):
		(word,leng) = heapq.heappop(translate_queue)
		res = translate(word,leng)
		if(res['responseStatus'] == 403): # Google doesn't like automatic translation
				increase_time_thresh(1)
				heapq.heappush(translate_queue,(word,leng))
		else:
			word = str.lower(str(res['responseData']['translatedText']))
		last_time = time.time()
		return (last_time, word)	
	else:
		return (last_time, '')
		
# TRANSLATE: translate a word
def translate(word,leng):
	return tr.translate_language(word,leng,'en')
	
# WORD: get word's synonyms
def get_synonyms(word):
	#print "Retrieving synonyms of word: " + word
	start = time.time()
	lst = []
	syns = wn.synsets(word)
	if(syns):
		for s in syns:
			s_set = wn.synset(s.name)
			lst += [str.lower(s_set.name.split('.')[0])]
			hypo = s_set.hyponyms()
			hyper = s_set.hypernyms()
			if(hypo):
				ln = len(hypo)
				if ln < 4:
					ln = 4
				for hyp in hypo[ln:]:
					lst += [str.lower(hyp.name.split('.')[0])]
			if(hyper):
				ln = len(hyper)
				if ln < 4:
					ln = 4
				for hyp in hyper[ln:]:
					lst += [str.lower(hyp.name.split('.')[0])]
	stop = time.time()
	#print "TIME for synonyms: " + str(stop - start)
	return set(lst)
			
# WORD: correct errors using edit distance
def correct_errors(word):
	global dict_index
	#print "Correcting " + word
	start = time.time()
	first_letter = word[0]
	length = len(word)
	if(length<3):
		length = 3
	search_range = length+2
	best = ''
	best_dist = 4
	done = 0
	while(length < search_range ):
		try:
			for dict_word in dict_index[first_letter][length]:
				dist = edit_distance(word,dict_word)
				if dist < best_dist:
					best = dict_word
					best_dist = dist
				if dist == 1:
					done = 1
					break
			if done:
				break
			length += 1
		except KeyError:
			break
	stop = time.time()
	#print "Time for correction " + str(stop-start)
	return best
	
# WORD: edit distance algorithm
def edit_distance(word_a, word_b):
	len_a = len(word_a)
	len_b = len(word_b)
	dist = zeros([len_a+1,len_b+1],int)
	for i in range(len_a+1):
		dist[i][0] = i
	for j in range(len_b+1):
		dist[0][j] = j
	for i in range(1,len_a+1):
		for j in range(1,len_b+1):
			insert = dist[i-1][j] + 1
			delete = dist[i][j-1] + 1
			substitute = dist[i-1][j-1]
			if word_a[i-1] != word_b[j-1]:
				substitute += 1
			dist[i][j] = min(insert,delete,substitute)
	return dist[len_a][len_b]		
			
# PROCESS: documents
# d_type defines the type of doc: 1 for doc, 0 for query
def process_docs(docs,flag, d_type):	
	for doc in docs:
		_doc = []
		if d_type:
			_doc = process_doc(doc,flag)
		else:
			_doc = process_query(doc,flag)
		if(doc):
			docs[docs.index(doc)] = _doc
	return docs

# WORD: index a word
def index_word(word,doc_id):
	global inverted_index
	#print "Indexing " + word
	start = time.time()
	try:
		s_rep = word_index[word] # Word is in the word_index; Take the word's synonym representative
		if doc_id not in inverted_index[s_rep]:
			inverted_index[s_rep] += [doc_id]
		stop = time.time()
		#print "INDEXING time: " + str(float(stop - start))
		return 1
	except KeyError:
	# Word is not in the word_index
		stop = time.time()
		#print "INDEXING time: " + str(float(stop - start))
		return 0
		
# WORD: process a word	
def process_word(word,doc_id, words,old_word = ""):
	global word_index
	#print "Processing " + word
	start = time.time()
	success = index_word(word,doc_id)
	if not success:
	# The word has not been processed yet; get its synonyms and perform check again	
		syn_list = get_synonyms(word)
		found = 0
		for syn in syn_list:
			if(index_word(syn,doc_id)):
			# A synonym has been found in the word_index; Add word to the index and point it to the synonym's s_rep
				word_index[word] = word_index[syn]
				found = 1
				break
		if(not found): # No synonyms of the word have been found in the word index; add the word to the index and set its s_rep to itself
			word_index[word] = word
			inverted_index[word] = [doc_id]
	if old_word != "":
		words[words.index(old_word)] = word_index[word]
	else:
		words[words.index(word)] = word_index[word]
	stop = time.time()
	#print "Time for process " + str(stop-start)
	return words

# WORD: check query word
def check_query_word(word,words,query_id,old_word=''):
	global corrected_index
	try:
		rep = word_index[word] 
		if old_word != '':
			words[words.index(old_word)] = rep
		else:
			words[words.index(word)] = rep
	except KeyError:
		# Word not in word_index: leave it as it is
		pass
	return words
	
# TFIDF
def compute_tfidf(query,doc):
	k = 2 # Weight
	w_sum = 0
	for word in query[1:]:
		if word != '':
			tfwd = doc.count(word)
			if tfwd != 0:
				tfwq = query.count(word)
				doc_size = len(doc)
				doc_freq = len(inverted_index[word])
				w_sum+= tfwq*(float(tfwd)/float(tfwd+float(k*doc_size)/avg_doc_len)*math.log10(float(4500)/float(doc_freq)))
	return w_sum
	
def compute_avg_doc_len(docs):
	num_docs = 0
	tot_words = 0
	for doc in docs: # In this case, the last item in doc_list is '' because we're splitting on the last character in the file, namely newline. But here, we generalize
		if doc != '':
			num_docs += 1
			for word in doc[1:]:
				if word != '':
					tot_words += 1
	avg_doc_len = float(tot_words)/float(num_docs)
	return avg_doc_len
