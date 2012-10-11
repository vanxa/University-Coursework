import Lab2Support as lab # Using TTS Lab2 support functions
import re
import math

queries = "a2.qrys"
docs = "a2.docs"
num_docs = 0
avg_doc_len = 0
tot_words = 0
index = {}

# Main tf-idf algorithm
def compute_tfidf(query,document):
	k = 2 # Weight
	w_sum = 0
	for word in query:
		if word != '':
			tfwd = document.count(word)
			if tfwd != 0:
				tfwq = query.count(word)
				doc_size = len(document)
				doc_freq = index[word] # Here, we assume that every word in every document is indexed. Is there a case when this may not hold?
				w_sum+= tfwq*(float(tfwd)/float(tfwd+float(k*doc_size)/avg_doc_len)*math.log10(float(num_docs)/float(doc_freq)))
	return w_sum
	
	
# Computes the inverted index for all words in the document list	
def compute_inverted_index(documents):
	global index
	doc_list = split_to_list(documents)
	index = {}
	# Populate index
	for doc in doc_list:
		distinct = [] # The list of distinct words in the document
		if(doc != ''):
			words = tokenize(doc)
			for word in words[1:]: # Skip the doc id
				if word != '' and word not in distinct:
					try:
						index[word] += 1
					except KeyError:
						index[word] = 1
					finally:
						distinct += [word]
	
	
	
# Reads the files defined by the globals 'queries' and 'docs'	
def read_files():
	global queries,docs
	q = lab.readFile(queries)
	d = lab.readFile(docs)
	return (q,d)


def compute_avg_doc_len(docs):
	global num_docs, tot_words
	doc_list = split_to_list(docs) # Must somehow strip the document ids
	for doc in doc_list: # In this case, the last item in doc_list is '' because we're splitting on the last character in the file, namely newline. But here, we generalize
		if doc != '':
			num_docs += 1
			words = tokenize(doc)
			for word in words[1:]:
				if word != '':
					tot_words += 1
	avg_doc_len = float(tot_words)/float(num_docs)
	return avg_doc_len

# Main function. Will compute and output everything
def start():
	global index,avg_doc_len
	(queries,docs) = read_files()
	index = compute_inverted_index(docs)
	avg_doc_len = compute_avg_doc_len(docs)
	output = ""
	doc_list = split_to_list(docs)
	q_list = split_to_list(queries)
	for query in q_list:
		if query != '':
			tokens = tokenize(query)
			q_id = tokens[0]
			q_words = tokens[1:]
			for doc in doc_list:
				if doc != '':
					doc_tokens = tokenize(doc)
					doc_id = doc_tokens[0]
					doc_words = doc_tokens[1:]
					w_sum = compute_tfidf(q_words,doc_words)
					output+= str(q_id) + " 0 "+ str(doc_id) + " 0 "+ str(w_sum) + " 0\n"
	lab.saveToFile(output,"tfidf.top")
	
def split_to_list(fl):
	return re.split("[\n]*", fl)
	
def tokenize(txt):
	return re.split("[\\s]*",txt)
	
