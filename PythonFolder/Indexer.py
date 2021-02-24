print("CREATING INDEXES")
import re
import os
from whoosh.index import create_in
from whoosh.fields import Schema, TEXT, KEYWORD, ID, STORED
from whoosh.analysis import StemmingAnalyzer, LowercaseFilter, StopFilter

schema = Schema(from_addr=ID(stored=True),
                to_addr=ID(stored=True),
                subject=TEXT(stored=True),
                body=TEXT(analyzer=StemmingAnalyzer()|LowercaseFilter() | StopFilter() ),
                tags=KEYWORD)



INDEX_PATH=os.getcwd()+os.sep+"indexmain"
# INDEX_PATH="D:\\indexmain"
def createSearchableData(root):
    '''
    Schema definition: title(name of file), path(as ID), content(indexed
    but not stored),textdata (stored text content)
    '''
    schema = Schema(title=TEXT(stored=True), path=ID(stored=True), \
                    content=TEXT, textdata=TEXT(stored=True))
    if not os.path.exists(INDEX_PATH):
        os.mkdir(INDEX_PATH)

    # Creating a index writer to add document as per schema
    ix = create_in(INDEX_PATH, schema)
    writer = ix.writer()
    with open(root, 'r',encoding='utf-8') as f:
        contentnew = f.readlines()
        blogid=0
        for line in contentnew:
            stringtext = re.sub(r"""
                                [,.;4@#?!&$*]+  # Accept one or more copies of punctuation
                                \ *           # plus zero or more copies of a space,
                                """,
                                " ",  # and replace it with a single space
                                line, flags=re.VERBOSE)
            # print(cont)
            # stringtext=[x for x in stringtext.split(' ') if x!=' ' or x!='\n']
            # stringtext=[s for s in stringtext.split(' ') if len(s)>0]
            stringtext = [i for i in stringtext.split(' ') if len(i) > 0 and all(j not in ['\n', '\t'] for j in i)]

            if len(stringtext) > 0:
                blogid += 1
                print(blogid)
                writer.add_document(title=str(blogid),\
                                    content=line, textdata=line)
    f.close()
    writer.commit()
