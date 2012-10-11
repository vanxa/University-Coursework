import re
import math

graph_dat = 'graph.txt'
out_index = {} # Contains node => outlinks relations; for HUB
in_index = {} # Contains node => inlinks relations; for AUTH
node_index = {} # Contains node => {auth,hub} relations

auth_index = {} # Contains auth => [node] relations
hub_index = {} # Contains hub => [node] relations

iterations = 9

# Calculated using process_dat. If using the same data, can simply use the number than having to compute each time
sum_nodes = 86029#86293 

def do_all(its = iterations):
	print "Processing data..."
	process_dat()
	print "Calculating HITS"
	hits(its)
	print "Done"

def hits(its = iterations):
	global node_index
	auth_out = ""
	hub_out = ""
	auth_index = {}
	hub_index = {}
	it = 0
	while(it < its):
		best_auth = 0.0
		best_auth_node = ''
		best_hub = 0.0
		best_hub_node = ''
		#print "Starting iteration " + str(it+1)
		norm_hub = 0		
		for node in node_index:
			# HUB
			out_sum = 0
			try:
				for outlink in out_index[node]:
					out_sum += node_index[outlink][0]
				norm_hub += out_sum*out_sum
			except KeyError:
				pass
			node_index[node][1] = out_sum # Save Hub value for node
		# Normalize Hub values
		norm_hub = math.sqrt(norm_hub)
		norm_auth = 0
		for node in node_index:
			# AUTH
			in_sum = 0
			try:
				for inlink in in_index[node]:
					in_sum += node_index[inlink][1]
				norm_auth += in_sum*in_sum
			except KeyError: 
				pass
			node_index[node][0] = in_sum
		norm_auth = math.sqrt(norm_auth)
		for node in node_index:
			auth = node_index[node][0]
			# End of iteration, so can save everything in orginal index
			auth = auth / norm_auth
			node_index[node][0] = auth
				
			if auth > best_auth:
				best_auth = auth
				best_auth_node = node
		#print "Best auth score for this iteration has " + best_auth_node + " : " + str(round(best_auth,8))		
		
		for node in node_index:
			hub = node_index[node][1] / norm_hub
			node_index[node][1] = hub
			
			if hub > best_hub:
				best_hub = hub
				best_hub_node = node
		#print "Best hub score for this iteration has " + best_hub_node + " : " + str(round(best_hub,8))
		
		#print "Ending: Hubs/Auth values for jeff is: " + str(node_index['jeff.dasovich@enron.com'])
		# REACHES SANITY VALUE ON ITERATION 9
		it += 1
	
	for node in node_index:
		try:
			auth_index[node_index[node][0]] += [node]
		except KeyError:
			auth_index[node_index[node][0]] = [node]
		
		try:
			hub_index[node_index[node][1]] += [node]
		except KeyError:
			hub_index[node_index[node][1]] = [node]
	
	for hub in sorted(hub_index.keys(), reverse = True)[0:10]:
		hub_out += str(round(hub,8)) + " " + str(hub_index[hub][0]) +"\n"
	for auth in sorted(auth_index.keys(), reverse = True)[0:10]:
		auth_out += str(round(auth,8)) + " " + str(auth_index[auth][0]) + "\n"
	saveToFile(hub_out, "hubs.txt")
	saveToFile(auth_out, "auth.txt")	
			

def process_dat():
	global out_index, in_index
	#nodes = {}
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
				add_to_outindex(sender,recipient)
				add_to_inindex(recipient,sender)
				init_values(sender)
				init_values(recipient)
				#try:
				#	nodes[sender]
				#except KeyError:
				#	nodes[sender] = 1
				#try:
				#	nodes[recipient]
				#except KeyError:
				#	nodes[recipient] = 1
				
	#return len(nodes)
			
def init_values(node):
	global node_index
	try:
		node_index[node]
	except KeyError:
		val = 1/ float(sum_nodes)
		node_index[node] = [val,val]
		
def add_to_outindex(outnode,innode):
	global out_index
	try:
		out_index[outnode] += [innode]
	except KeyError:
		out_index[outnode] = [innode]
		
def add_to_inindex(innode,outnode):
	global in_index
	try:
		in_index[innode] += [outnode]
	except KeyError:
		in_index[innode] = [outnode]

# Taken from Lab2Support
def readFile(filename):
	f = open(filename, 'r')
	try:
	    content = f.read()
	finally:
	    f.close()
	return content

# Taken from Lab2Support	
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
	    
