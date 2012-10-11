from nltk.corpus import words, wordnet as wn

def make_index():
	s = set(words.words('en'))
	index = {}
	for word in s:
		if len(word) > 2:
			word = str.lower(word)
			try:
				index[word[0]]
				try:
					index[word[0]][len(word)] += [word]
				except KeyError:
					index[word[0]][len(word)] = [word]
				except TypeError:
					print index[word[0]][len(word)]
			except KeyError:
				index[word[0]] = {len(word):[word]}
	return index
	
def get_synonyms(word):
	lst = []
	syns = wn.synsets(word)
	if(syns):
		for s in syns:
			s_set = wn.synset(s.name)
			lst += [s_set.name.split('.')[0]]
			hypo = s_set.hyponyms()
			hyper = s_set.hypernyms()
			if(hypo):
				for hyp in hypo:
					lst += [hyp.name.split('.')[0]]
			if(hyper):
				for hyp in hyper:
					lst += [hyp.name.split('.')[0]]
	return set(lst)

	

