print("Searching")
import operator
import re
import sys
from nltk.stem import PorterStemmer
from nltk.tokenize import word_tokenize
from colorama import Style
ps = PorterStemmer()
from sklearn.metrics.pairwise import cosine_similarity
import whoosh
from whoosh.qparser import QueryParser
from whoosh import scoring
from whoosh.index import open_dir
import whoosh.highlight as highlight
upf=highlight.UppercaseFormatter()
from IPython.core.display import display, HTML
from whoosh.query import FuzzyTerm
import nltk
from nltk.corpus import words, wordnet
from nltk.corpus import stopwords
nltk.download('stopwords')
nltk.download('words')
import numpy as np
import os
wordnet_lemmatizer = nltk.WordNetLemmatizer()
stop_words = set(stopwords.words('english'))
import gensim.downloader as api
INDEX_PATH=os.getcwd()+os.sep+"indexmain"
try:
    ix = open_dir(INDEX_PATH)
except:
    print("NO INDEXES FOUND")
    import Indexer as ser
    ser.createSearchableData(os.getcwd()+os.sep+"textmainfinal.txt")
    ix = open_dir(INDEX_PATH)
    print("SEARCHING")
glove_vectors = api.load("glove-twitter-25")

# print(ix.reader().doc_count())
uniqwords=[]
query_str = ""
# print(b'scott'=='scott')
for ter in ix.reader().all_terms():
    # print(ter[1].decode('utf-8'))
    uniqwords.append(ter[1].decode('utf-8'))

def getPreprocessedRelations(relation,querySide=False):
    tokenizer = nltk.RegexpTokenizer(r"\w+")
    word_tokens = tokenizer.tokenize(relation)
    preprocessed_relation = [word.replace('-', '') for word in word_tokens]
    filtered_relation = [w for w in preprocessed_relation if not w in stop_words]
    return filtered_relation

def getRelationsEmbeddings(model,relation):
  sum=np.zeros(25)
  cnt=0
  preprocessed_relation=getPreprocessedRelations(relation)
  for word in preprocessed_relation:
    if word in model.vocab:
      sum+=model[word]
      cnt+=1
  if(cnt!=0):
    sum=sum/cnt
  return sum

def getsynonymorantonym(term,negated=False):
        synonyms = []
        antonyms = []
        for syn in wordnet.synsets(term):
            for l in syn.lemmas():
                if not negated:
                    synonyms.append(term)
                    if wordnet_lemmatizer.lemmatize(l.name(), pos="n") in uniqwords:
                        synonyms.append(wordnet_lemmatizer.lemmatize(l.name(), pos="n"))
                if negated and l.antonyms():
                    antonyms.append(wordnet_lemmatizer.lemmatize(l.antonyms()[0].name(), pos="n"))
        if len(synonyms)>0:
            return set(synonyms)
        else:
            return set(antonyms)
# TODO
# change more than one whitespace to one
def getexpandedquery(userquery):

    suserq=""
    nextanto=False
    synonymsantonyms=[]
    userQt=userquery.split(" ")

    i=0
    while i<len(userQt):
        if userQt[i] in ["no","not","nor","neither"]:
            i=i+1
            nextanto = True

        else:
            nextanto = False
        intersynanto=getsynonymorantonym(userQt[i], nextanto)
        if not nextanto:
            suserq += userQt[i]+" "

        for t in intersynanto:
            if len(synonymsantonyms)<10:
                suserq +=t+" "
                synonymsantonyms.append(t)
            else:
                return suserq
        i+=1
    return suserq



def gethighlightedText(stringtext1,query_terms1):
    stringtext = maintext=stringtext1
    stemmedqterms=[]
    query_terms=query_terms1
    print(query_terms)
    for qt in query_terms:
        stemmedqterms.append(ps.stem(qt))
    print(stemmedqterms)
    #     stringtext=stringtext.translate(str.maketrans('\s', '\s', string.punctuation))
    #     stringtext=stringtext.replace('.',' ')
    stringtext = re.sub(r"""
                           [,-.;@#?!&$]+  # Accept one or more copies of punctuation
                           \ *           # plus zero or more copies of a space,
                           """,
                        " ",  # and replace it with a single space
                        stringtext, flags=re.VERBOSE)
    for textword in stringtext.split(' '):
        #         print(textword)
        if ps.stem(textword.lower()) in stemmedqterms:
            maintext = maintext.replace(textword, "\u0332".join(textword))
            #   maintext = maintext.replace(textword, "<strong>"+textword+"</strong>")
    return maintext



# Top 'n' documents as result

from whoosh import qparser



# for hit in results:
#     print(hit['title'])
#     print(gethighlightedText(hit["textdata"]))
query_terms =[]
def loadRetrieverObjects(query_str_fromfrontend,Num):
    query_str=query_str_fromfrontend
    topN = int(Num)
    print(topN)
    query_terms = [terms.lower() for terms in query_str.split(" ") if ps.stem(terms) in words.words()]
    print(query_terms)
    searcher = ix.searcher()
    expQuery = getexpandedquery(query_str)
    print(expQuery)
    query = qparser.QueryParser("content", ix.schema, group=qparser.OrGroup).parse(expQuery)

    results = searcher.search(query, limit=2 * topN)
    if (len(results) < 2 * topN):
        query = QueryParser("content", ix.schema, termclass=FuzzyTerm, group=qparser.OrGroup).parse(expQuery)
        results = searcher.search(query, limit=topN)
    semanticscoredict = {}
    queryvec = []
    queryvec.append(getRelationsEmbeddings(glove_vectors, expQuery))
    indexdict = {}
    hitscoredict={}
    for hit in results:
        docvec = []
        docvec.append(getRelationsEmbeddings(glove_vectors, hit['textdata']))
        indexdict[hit['title']] = hit['textdata']
        hitscoredict[hit['title']] = hit.score
        semanticscoredict[hit['title']] = cosine_similarity(queryvec, docvec)

    sorted_dict = sorted(semanticscoredict.items(), key=operator.itemgetter(1), reverse=True)

    topncounter = 0
    resultingtext=[]
    resultingtext.append("Expanded query is " + expQuery)

    for cfg in sorted_dict:
        if topncounter < topN:
            resultingtext.append(gethighlightedText(indexdict[cfg[0]],query_terms))
            print(gethighlightedText(indexdict[cfg[0]],query_terms))
            resultingtext.append("Score is "+str(hitscoredict[cfg[0]]))
            topncounter += 1
    return  resultingtext




