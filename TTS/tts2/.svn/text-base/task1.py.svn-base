import Lab2Support as lab # Using TTS Lab2 support functions
import re
#import time

queries = "a2.qrys"
docs = "a2.docs"

def overlap():
	global queries, docs
	output = ""
	queries = re.split("[\\n]+",lab.readFile(queries))
	docs = re.split("[\\n]+",lab.readFile(docs))
	for doc in docs:
		doc_id = re.findall("[^\s]*",doc)[0] # extract the doc id. We assume that no line starts with whitespace
		for query in queries:
			if query != '' and doc != '':
				tokens = re.split("[\s]*",query)
				query_id = tokens[0] # extract the query number. We assume (again) that no line starts with whitespace
				q = tokens[1:] # strip string from query_id
				output += str(query_id) + " 0 "+ str(doc_id) + " 0 "+ str(similarity(q,doc)) + " 0\n"
#	print(str(stop-start))
	lab.saveToFile(output,"overlap.top")
	
	
def similarity(query, document):
	sim = 0
	distinct = []
	for token in query:
		if token in document and token not in distinct:
			distinct += [token]
			sim+=1
						
	return sim
	

