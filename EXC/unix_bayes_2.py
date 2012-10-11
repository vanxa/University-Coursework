import os
import re
import math
import time

# Data files
dat_l = 'large.txt'
dat_s = 'small.txt'
test_dat = 'test.txt'

# TRAIN DATA
train_num_words=0  # Computes the total number of words in the document
train_num_words_label = {0:0,1:0} # Computes the total number of words in the document based on their label (0 lowercase, 1 uppercase) 
train_num_features=0 # Computes the total number of features for all words in the document
train_num_features_label = {0:0,1:0} # Computes the total number of features for all words based on word label 
train_feat_freqs={} # Computes the frequency of each feature in the training data. Format is feature => {label_0:frequency, label_1:frequency}
train_feat_probs={} # Computes the feature likelihoods. Format is feature => {label_0:likelihood,label_1:likelihood}


# TEST DATA
test_words={} # Contains all words from test data, with corresponding features. Format is word => list_of_features
test_scores={} # Contains the computed scores for each word in the test data. Format is word => score (0 for lowercase, 1 for uppercase)

p_1=0.5 # Prior probability for label 1, set manually 
p_0=0.5 # Prior probability for label 0, set manually

# MAIN
def bayes(dat = 'large'):
	global p_1, p_0
	print "Starting..."
	start_time = time.time() # Times process
	train_dat = ''
	if dat == 'large':
		train_dat = dat_l
	else:	
		train_dat = dat_s
	train_txt = (preprocess(read_file(train_dat)).split()) # First, take the train text and pre-process it 
	extract_features(train_txt) # Extract all features and their frequency
	update_num_features() # Count the total number of features. Since I'm not looking for fast performance, I have this as a separate function for convenience
	update_freqs() # Normalizes the frequencies of all features, by considering the data skew regarding (there are more features seen with label 0, then with label 1)
	compute_likelihoods() # Computes the likelihoods for each features
	
	# Estimate the probabilities if necessary
	#p_1 = float(train_num_words_label[1]) / float(train_num_words)
	#p_0 = float(train_num_words_label[0]) / float(train_num_words)
	
	# Extract the training data
	test_txt = read_file(test_dat).split()
	
	# Extract the features 
	extract_features(test_txt,0)
	
	# Score the words
	do_score()
	print "Done: probabilities are: 0:%f , 1:%f" % (p_0,p_1)
	count, lst = diff(get_scores(test_scores,1),get_truth_scores(1))
	print "Number of label 1 words: %d\nNumber of matches: %d" % (len(get_scores(test_scores,1)), count)
	end_time = time.time()
	print "Execution time: %f"%(float(end_time-start_time)/60.0)
		
# Read contents from a file
def read_file(file_name):
	f = open(file_name, 'r')
	try:
		content = f.read()
	finally:
		f.close()
	return content	
	
# Updates the total number of features and the total number of features per label.
# NOTE: The total number of features ignores repeating features, thus it computes the length of the SET of all features
def update_num_features():
	global train_num_features, train_num_features_label
	train_num_features = len(train_feat_freqs)
	for feature in train_feat_freqs:
		train_num_features_label[0] += train_feat_freqs[feature][0]	
		train_num_features_label[1] += train_feat_freqs[feature][1]

# Updates the frequencies for each feature. This is to ensure that the perceived data skew does not bias the scoring towards the class (label) with more available data
def update_freqs():
	global train_feat_freqs
	for feature in train_feat_freqs:
		# Takes into account the total number of features with different label. Thus, a feature seen twice with label 1 from a total of 4 documents (words) labelled 1 would
		# have the same weight as a feature seen four times with label 0 from a total of 8 documents (words) labelled 0.
		# I've put the total number of features per label instead of the total number of words per label, in order to make the Hadoop equivalent easier to process
		# while observing very similar (if not better) results. 
		train_feat_freqs[feature][0] *= float(train_num_features_label[1]) / float(train_num_features)
		train_feat_freqs[feature][1] *= float(train_num_features_label[0]) / float(train_num_features)
		
# Computes the likelihoods for each feature. Takes care of the 0 probability problem, by normalization
def compute_likelihoods():
	global train_feat_probs
	for feature in train_feat_freqs:
		likelihood_0 = math.pow(float(train_feat_freqs[feature][0] + 1) / float(train_feat_freqs[feature][0] + train_feat_freqs[feature][1] + 2), math.log1p(float(train_num_features) / float(train_feat_freqs[feature][0] + train_feat_freqs[feature][1])))
		likelihood_1 = math.pow(float(train_feat_freqs[feature][1] + 1) / float(train_feat_freqs[feature][0] + train_feat_freqs[feature][1] + 2), math.log1p(float(train_num_features) / float(train_feat_freqs[feature][0] + train_feat_freqs[feature][1])))
		train_feat_probs[feature] = {0:likelihood_0,1:likelihood_1}
	
## Extracts features from a set of words. is_train flag determines if features are extracted for the training data, or for the real data	
def extract_features(words, is_train = 1):
	for word in words:
		if not word.isdigit(): # Ignore numbers-only words as they're considered irrelevant
			if len(word) > 3:
				feature_lst = []+[word]+[word[0:2]]+[word[0:3]]+[word[-2:]]+[word[-3:]] # Take needed features: length 5
			elif len(word) == 3:
				feature_lst = []+[word]+[word[0:2]]+[word[-2:]] # length 3
			else:
				feature_lst = []+[word] # length 1
			if(not is_train):
				add_test_word(word, feature_lst) # updates the real_word_index list
			else:
				label = check_label(word)
				update_word_count(word,label)  # updates number of words seen with label
				for feature in feature_lst:
					if not feature.isdigit():
					#update_feature_count(label)
						add_to_f_index(feature.lower(), label) # will add feature to the train_featutre freq index
			
def add_test_word(word, feature_lst):
	global test_words
	test_words[word] = feature_lst
	
def do_score():
	global test_scores
	for word in test_words:
		score_0 = p_0
		score_1 = p_1
		for feature in test_words[word]:
			if feature in train_feat_freqs:
				score_0 *= train_feat_probs[feature][0]
				score_1 *= train_feat_probs[feature][1]
		if score_0 > score_1:
			test_scores[word] = 0
		else:
			test_scores[word] = 1

def update_word_count(word,label):
	global train_num_words, train_num_words_label, train_words
	#try:
	#	train_words[word]
	#except KeyError:
	#	train_words[word] = 1
	train_num_words += 1
	train_num_words_label[label] += 1
	
def add_to_f_index(feature,label):
	global train_feat_freqs
	try:
		train_feat_freqs[feature]
		try:
			train_feat_freqs[feature][label] += 1
		except KeyError:
			train_feat_freqs[feature][label] = 1
	except KeyError:
		train_feat_freqs[feature] = {label:1,1-label:0}	
		
def preprocess(txt):
	return re.sub('\`|\!|\'|\"|\#|\$|\%|\^|}|{|;|\.|\,|:|\[|\]|\(|&|\)|(\\\)|\/|\+|\?|\-|\=|\~|\*|\@|\||_',' ',txt)	
	
def check_label(word):
	label = 0 #Initialize as lower case
	for ch in word:
		if not ch.isdigit() and ch.isupper():
			label = 1
			break
	return label
	
def get_scores(scores, label):
	lst = []
	for word in scores.items():
		if word[1] == label:
			lst += [word[0]]
	return lst
	
def get_truth_scores(label):
	txt = read_file('test-truth.txt').split()
	return [tok.lower() for tok in txt if check_label(tok)==label]
	
def diff(score1,score2):
	count = 0
	lst = []
	for tok1 in score1:
		for tok2 in score2:
				if tok1==tok2:
					lst += [tok1]
					count += 1
	return count, lst	
