import re
import itertools
import os

graph_dat = 'graph.txt'
pr_index = {} # Contains node => page_rank relations
node_inlinks = {} # Contains node => {inlinks} relations
node_outlinks = {} # Contains node => count_outlinks relations
nodes = {}
black_holes = []

iterations = 11
top = "21"
conv_weight = 0.8 # Speed of algorithm convergence
importance = 0.01

# Calculated using process_dat. If using the same data, can simply use the number than having to compute each time
sum_nodes = 86029


def do_all(_top = top, its = iterations):
	print "Processing..."
	process_dat()
	print "Calculating pr"
	pr(_top, its)
	print "Producing graph"
	find_top_links(_top)
	print "Done"

def process_dat():
	global pr_index,node_inlinks, count_nodes
	txt = readFile(graph_dat)
	lines = txt.split('\n')
	for line in lines:
		if line != '':
			tokens = line.split()
			if len(tokens) != 3:
				print 'OOOPs, we have a problem with' + line
				break
			msg = tokens[0]
			sender = tokens[1]
			recipient = tokens[2]
			if sender != recipient:
				init_pr(sender)
				init_pr(recipient)
				add_to_outlinks(sender)
				add_node_to_inlinks(recipient,sender)
			
				# Using the sum_nodes, calculated during initial coding: don't need to dynamically calculate it given that the data is constant
				#add_node_to_sum(sender)
				#add_node_to_sum(recipient)
				
def get_black_holes():
	global black_holes
	for node in pr_index:
		try:
			node_outlinks[node]
		except KeyError:	
			black_holes += [node]
	print 'ready'
				
def pr(_top,its):
	global pr_index
	it = 0
	while(it < its):
		#print "Starting iteration " + str(it+1)
		tmp_pr = {} # For normalization
		norm_sum = 0
		for node in pr_index:
			#print node
			link_sum = 0
			try:
				for link in node_inlinks[node]:
					link_sum += pr_index[link] / node_outlinks[link]
			except KeyError:
				pass
			rank = (1 - conv_weight) / sum_nodes + conv_weight*link_sum
			tmp_pr[node] = rank
		_sum = 0
		for node in tmp_pr:
			_sum += tmp_pr[node]
		norm = 1/_sum
		for node in pr_index:
			pr_index[node] = norm*tmp_pr[node]
			norm_sum += pr_index[node] # For debug
		#print "Ending: intermediate page rank is : " + str(norm_sum)
		#print "Rank for jeff is: " + str(round(pr_index['jeff.dasovich@enron.com'],8))
		it += 1
	rank_index = {}
	for node in pr_index:
		try:
			rank_index[pr_index[node]] += node
		except KeyError:
			rank_index[pr_index[node]] = node
	
	out = ""
	i = 0
	for item in sorted(rank_index.keys(), reverse = True)[0:int(_top)]:
		out += str(round(item,8)) + " " + rank_index[item] + "\n"
		if i == 9:
			saveToFile(out, "pr.txt")		
		i += 1
	saveToFile(out,'top'+_top+".txt")
	
	
	
				
def init_pr(node):
	global pr_index
	try:
		pr_index[node]
	except KeyError:
		pr_index[node] = 1 / sum_nodes				
				

def add_to_outlinks(node):
	global node_outlinks
	try:
		node_outlinks[node] += 1
	except KeyError:
		node_outlinks[node] = 1		

def add_node_to_inlinks(node,link):
	global node_inlinks
	try:
		node_inlinks[node] += [link]
	except KeyError:
			node_inlinks[node] = [link]
				
def add_node_to_sum(node):
	global nodes
	try:
		nodes[node]
	except KeyError:
		nodes[node] = 1

def find_top_links(_top):
	lines = readFile('top'+_top+'.txt').split('\n')
	people = []
	out = "digraph G {\n"
	for line in lines:
		if line != '':
			person = line.split()[1]
			people += [person]
	for pairs in itertools.product(people, repeat=2):
		if pairs[0] != pairs[1]:
			if links_to(pairs[0],pairs[1]) and num_links(pairs[0],pairs[1]) >= 0.01:
				out += "\""+pairs[0].split('@')[0]+"\""+ " -> " + "\""+pairs[1].split('@')[0]+"\""+  ";\n"
	out += "}"
	saveToFile(out,"graph.dot")
	os.system("dot -Tpng graph.dot > graph.png")
	
	
			
def links_to(node1,node2):
	# Has node1 sent emails to node2?
	if node1 in node_inlinks[node2]:
		return 1
	else:
		return 0	
		
def num_links(node1,node2):
	tot = len(node_inlinks[node2])
	num = float(node_inlinks[node2].count(node1)) / float(tot)
	return num
			
# Taken from Lab2Support
def readFile(filename):
	f = open(filename, 'r')
	try:
	    content = f.read()
	finally:
	    f.close()
	return content
	
def saveToFile(data, filename):
	d = dict()
	string = ''
	array = []
	s = set(array)	
	f = open(filename, 'w')
	try:
	    if type(data) == type(string):
	    	f.write(data)
	    if type(data) == type(array):
	    	for d in data:
			f.write(str(d) + '\n')
	    if type(data) == type(s):
		data = list(data)	    	
		for d in data:
			f.write(str(d) + '\n')
	    if type(data) == type(d):
		sortedData = sorted(data.items(), key=itemgetter(0))
		for sd in sortedData:
			f.write(str(sd) + '\n')
	finally:
	    f.close()

