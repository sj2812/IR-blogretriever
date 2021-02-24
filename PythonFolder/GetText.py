import os
from lxml import etree
invalidfilecnt=0
path=Path where the blogs are donloaded
check=0
contentproid=[]
# if check<10:

for file in os.listdir(path):
    # check+=1
    try:
        if file.endswith(".xml"):
            #             print(file)
            try:
                with open(path + "//" + file) as f:
                    content = f.readlines()
            except:
                with open(path + "//" + file, encoding="utf-8") as f:
                    content = f.readlines()
            contentStr = ""
            for cont in content:
                contentStr += cont
            parser = etree.XMLParser(recover=True)
            root = (etree.fromstring(contentStr, parser=parser))
            for elem in root:
                if elem.tag == 'post':
                    if elem.text:
                        # fw.write(elem.text+" ")
                        contentproid.append(elem.text+" ")
                    else:
                        print("duplicate")
            # extractDates(root, dates)

    except:
        invalidfilecnt += 1
        print(file)
print(invalidfilecnt)
print(len(contentproid))
print(len(set(contentproid)))
with open('textmainfinal.txt','w',encoding="utf-8") as fw:
    for val in set(contentproid):
        fw.write(val)
# import collections
# print([item for item, count in collections.Counter(contentproid).items() if count > 1])



