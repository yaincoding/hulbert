#!/usr/bin/python3

w = open('corpus_sejong_all_convert.txt', 'w', encoding='utf-8')
with open('corpus_sejong_all.txt', 'r', encoding='utf-8') as f:
    tags = []
    for i, line in enumerate(f):
        line = line.strip()
        if len(line) > 0:
            eojeol, poses = line.split('\t')
            tags.append(poses)
        else:
            doc = ' '.join(tags)
            tags = []
            w.write(doc+'\n')

w.close()
