import robotparser
from urllib import *
import TTSOpener
import re
from heapq import *
import time

BASE_URL = "http://ir.inf.ed.ac.uk"
SEED_URL = BASE_URL + "/tts/0787128/0787128.html"
AGENT_NAME = "TTS"

#Start with seed URL
frontier = [(1,SEED_URL)] # Give an initial priority to the seed
# visited_urls = {} # Dictionary approach
visited_urls = [] #list approach

request_delay = 0
parser = robotparser.RobotFileParser()
parser.set_url(BASE_URL+"/robots.txt")
parser.read()	
opener = TTSOpener.TTSOpener({})

output = open('tts_log.txt', 'w')
errors = open('error.log','w')
stats = open('stats.txt','a')

num_url_not_found = 0
num_url_no_content = 0
tot_num_pages = 0

def start():
	global frontier, visited_urls, errors, output, num_url_no_content, num_url_not_found, tot_num_pages, stats
	start_time = time.time()
	request_delay = extract_request_delay() # Follow website requirements on crawler access time	
	print("Request-delay is:" + str(request_delay) +"\r\n")
	# For delaying
	last_time = 0
	# Statistics
	
	tot_num_urls = 0
	num_distinct = 1 # Seed url as the first distinct page to be processed
	num_external = 0
	num_duplicates = 0
	num_not_crawlable = 0
	
	avg_num_urls = 0
	perc_urls_not_found = 0
	perc_duplicates = 0
	perc_distinct = 0
	perc_external = 0
	perc_no_content = 0
	try:
	
		while(frontier):
			tot_num_pages += 1		
			# Local (for page) statistics
			local_num_urls = 0
		
			current_url = heappop(frontier)[1]
			current_dir = get_page_dir(current_url)
		
			output.write(str(tot_num_pages) + ": URL: " + current_url)
			output.write(" Containing dir: " + current_dir + "\r\n")
			current_time = time.time()
			if(current_time - last_time < request_delay): # Don't delay the crawler on startup
				time.sleep(request_delay - (current_time - last_time))
			content = extract_content(current_url) # Grab the page contents
			last_time = time.time()
			if not(content == 0):	# The page contains contents
			
				urls = extract_urls(content) # Get a list of all found urls
				for url in urls: # Loop: process all urls found on page
					tot_num_urls += 1
					local_num_urls += 1
					# Add 1 to total number of urls; Add 1 to total number of urls for the current processed page
								
					has_root = has_root_path(url) # Check if url has root path
				
					priority = 0
				
					if has_root == 0:	# Is local, but must first add root path and current directory
						priority = get_url_priority(url)
						url = BASE_URL + current_dir + "/" + url
										
					elif has_root == 1:	# Has root path and is local
						priority = get_url_priority(get_page_name(url))* (-1)
					
					elif has_root == 2:	# Has root path but points to an external web page
						num_external += 1
						if(is_visited(url)):
							num_duplicates += 1
						else:
							num_distinct += 1
					
						# Check if url has been visited: if visited -> num_duplicates += 1; num_distinct += 1 otherwise;
						continue
					
					output.write("FOUND URL: " + url)	
					if(priority):	
						output.write(" PRIORITY: " + str(priority))
					else:
						output.write(" NOT LOCAL URL\r\n")
				
					# Do checks for visited and crawlability here, as well as enqueue data
					if(not is_visited(url)):
						num_distinct += 1
						output.write(" DISTINCT ")
						# visited_urls[url] = 1
						visited_urls += [url]
						if(is_crawlable(url)):
							output.write(" CRAWLABLE\r\n")
							enqueue(url, priority)
						else:
							output.write(" NOT CRAWLABLE\r\n")
							num_not_crawlable += 1
					else:
						output.write(" VISITED\r\n ")
						num_duplicates += 1
			
				avg_num_urls += local_num_urls
	except KeyboardInterrupt:
		pass
	end_time = time.time()
	run_time = end_time - start_time
	print("DONE! STATISTICS: \r\n")
	print("TIME: " + str(run_time) + " SECONDS\r\n")
	stats.write("DONE! STATISTICS: \r\n")
	stats.write("TIME: " + str(run_time) + " SECONDS\r\n")
	if(tot_num_pages):
		tot_pages = float(tot_num_pages)
		avg_num_urls = float(avg_num_urls) / tot_pages
		perc_urls_not_found = float(num_url_not_found) / tot_pages *100
		perc_duplicates = float(num_duplicates) / tot_pages *100
		perc_external = float(num_external) / tot_pages *100
		perc_distinct = float(num_distinct) / tot_pages *100
		perc_no_content = float(num_url_no_content) / tot_pages *100
		
		print("Total number of pages processed: " + str(tot_pages) + "\r\n")		
		print("Total number of urls retrieved: " + str(tot_num_urls) + "\r\n")
		print("Total number of distinct urls retrieved: " + str(num_distinct) + "\r\n")
		print("Total number of duplicate urls retrieved: " + str(num_duplicates) + "\r\n")
		print("Total number of external urls retrieved: " + str(num_external) + "\r\n")
		print("Total number of urls not found: " + str(num_url_not_found) + "\r\n")
		print("Total number of pages with no content: " + str(num_url_no_content) + "\r\n")
		print("Total number of prohibited pages: " + str(num_not_crawlable) + "\r\n")
		print("Average number of URLs per page: " + str(avg_num_urls) + "\r\n")
		print("Percentage of pages not found: " + str(perc_urls_not_found) +"\r\n")
		print("Percentage of urls already retrieved and processed: " + str(perc_duplicates) + "\r\n")
		print("Percentage of external urls: " + str(perc_external) + "\r\n")
		print("Percentage of pages with no content: " + str(perc_no_content) + "\r\n") 
		
		stats.write("Total number of pages processed: " + str(tot_pages) + "\r\n")		
		stats.write("Total number of urls retrieved: " + str(tot_num_urls) + "\r\n")
		stats.write("Total number of distinct urls retrieved: " + str(num_distinct) + "\r\n")
		stats.write("Total number of duplicate urls retrieved: " + str(num_duplicates) + "\r\n")
		stats.write("Total number of external urls retrieved: " + str(num_external) + "\r\n")
		stats.write("Total number of urls not found: " + str(num_url_not_found) + "\r\n")
		stats.write("Total number of pages with no content: " + str(num_url_no_content) + "\r\n")
		stats.write("Total number of prohibited pages: " + str(num_not_crawlable) + "\r\n")
		stats.write("Average number of URLs per page: " + str(avg_num_urls) + "\r\n")
		stats.write("Percentage of pages not found: " + str(perc_urls_not_found) +"\r\n")
		stats.write("Percentage of urls already retrieved and processed: " + str(perc_duplicates) + "\r\n")
		stats.write("Percentage of external urls: " + str(perc_external) + "\r\n")
		stats.write("Percentage of pages with no content: " + str(perc_no_content) + "\r\n") 
		
		
		
	
def extract_request_delay():
	global BASE_URL
	delay = 0
	text = opener.open(BASE_URL + "/robots.txt").read()
	tokens = re.split('[\\n]+',text)
	extract = [tok for tok in tokens if re.match("Crawl-delay",tok, re.IGNORECASE)]
	if(extract): # A Crawl-delay line has been found
		delay = int([tok for tok in re.split("\s",extract[0]) if re.match("[0-9]+",tok)][0])
	else: # No crawl-delay line, search for Request-rate
		extract = [tok for tok in tokens if re.match("Request-rate",tok,re.IGNORECASE)]
		if(extract): # Found Request-rate
			rate = [token for token in re.split("\s",extract[0]) if re.match("[0-9]+",token)][0]
			rate = re.split("\/",rate)
			pages = rate[0]
			seconds = rate[1]
			delay = float(seconds) / float(pages)	
	return delay

# Extracts the html code between the <!-- CONTENT --> and <!-- /CONTENT --> tag pair
def extract_content(url):
	global num_url_not_found, num_url_no_content, errors, tot_num_pages
	try:
		text = opener.open(url).read()
		content =re.search("<!--(\s)*CONTENT(\s)*-->[\s]*.*<!--(\s)*/CONTENT(\s)*-->",text, re.DOTALL)
		if(content):
			return content.group(0)
		else:
			errors.write("The given url: "+ url + " has no content, specified within the tag pair <!-- CONTENT --> <!-- /CONTENT -->\r\n")
			output.write("NO CONTENT\r\n")
			num_url_no_content += 1
			tot_num_pages -= 1 
			return 0
			
	except IOError: # Specified URL is not found on the site
		num_url_not_found += 1
		tot_num_pages -= 1 # Want to make sure that pages that were not found are not added up to the total number of retrieved pages
		errors.write("URL " + url + " was not found!\r\n")
		return 0 
	
		
# Gets a list of urls, as specified by the <a href='...'> </a> tag. Looks for tokens in the content which contain the keyword href
def extract_urls_1(content_text):
	return [hrefs for hrefs in re.split("\s",content_text) if re.search("href",hrefs)]
	
# Gets a list of urls, as specified by the <a href='...'> </a> tag.
def extract_urls_2(content_text):
	content_text = re.split("\s",content_text)
	urls = []
	for anchor in range(len(content_text)):
		if(content_text[anchor] == "<a"):
			for href in range(anchor,len(content_text)):
				if(content_text[href] == "</a>"):
					break
				if(re.search("href",content_text[href])):
					urls += [content_text[href]]
					break
	return urls

# A yet another version of extract_urls: this one takes additional measures in case that there are whitespaces withing the "href=[...]" statement, as in: "href = ' www.example.com'" A potential problem with this url description might be sql injections 	
def extract_urls_3(content_text):
	contents = re.split("<", content_text.replace(' ',''))
	urls = []
	for url in contents:
		if(re.search("href=(\"|\').*(\"|\')",url)):
			urls += [tidy_url(url)]
	return urls
	
# A yet another version :) This version takes care of random whitespaces, makes sure that the "href" statement belongs to an anchor tag, and not to a link tag for example, and extracts only <a> tags that contain "href" statements	
def extract_urls(content_text):
	return [tidy_url(url) for url in re.findall("<\s*a[^>]*href[^>]*\.[^>]*",content_text, re.IGNORECASE)]

# Tidy up the url for processing
def tidy_url(url):
	href = re.search("href(\s)*=(\s|\'|\")*[^' '|^\'|^\"]*",url, re.IGNORECASE).group(0).replace(' ','') # Want to take the href=(')(")example.html part of the anchor tag
	if("\'" in href or "\"" in href): # Remove of first ' or " if it's there
		return re.search("(?<=[\'|\"]).*\.[^\"|^\']*",href,re.IGNORECASE).group(0)
	else:
		return re.search("(?<==).*\.[^' '|^\'|^\"]*",url,re.IGNORECASE).group(0)
	
# Extracts the priority of the url by taking its numerical name
def get_url_priority(url):
	split = re.split("\.",url)[0]
	return int(re.search("[0-9]+",split).group(0))*(-1)
	
# Extracts the page name of the url. This must be done in case the url is specified with its root path of type http://ir.inf.ed.ac.uk/path_to_directory/page_name
def get_page_name(url): 
	url_strip = strip_root(url) # will get /path_to_directory/page_name 
	return re.search("[a-z0-9]+\..*",url).group(0)
		
# Extracts the directory of the page. This must be done to determine the current directory we're in		
def get_page_dir(url):
	url_strip = strip_root(url)
	return re.split("\/[a-z0-9]+\..*",url_strip)[0]
		
# Strips the http://ir.inf.ed.ac.uk (BASE_URL) part of the url		
def strip_root(url):
	res = re.split(BASE_URL,url)[1]
	return res
		
# Check if the url has root path and if so, check if it's local
def has_root_path(url):
	if(re.match("(\s)*http(s)?://", url)): # Has root path
		if(re.search("(www)?[a-z0-9]*.inf.ed.ac.uk/",url)): # Is local (use search here, because BASE_URL is specified without using www; however, the url http://www.ir.inf.ed.ac.uk for example, may be valid
			return 1
		else: # Is external link
			return 2
	else: # Doesn't have root path and is local
		return 0
	
def enqueue(url, priority):
	global frontier
	heappush(frontier, (priority, url))
	
def is_visited(url):
	if url in visited_urls:
		return 1
	else:
		return 0	
	#try:
	#	if visited_urls[url] == 1:
	#		return 1
	#except KeyError:
	#	return 0

# Check if the URL can be crawled, as specified in robots.txt	
def is_crawlable(path):
	if re.match("http:",path):
		return parser.can_fetch(AGENT_NAME,path)
	else:
		path = URL_BASE + path
		return parser.can_fetch(AGENT_NAME,path)		
