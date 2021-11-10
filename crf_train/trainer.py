try:
    import pycrfsuite
except:
    print('Failed to import python-crfsuite')

import json
import os
from collections import namedtuple
from transformer import BaseFeatureTransformer
from utils import get_process_memory
from utils import check_dirs

Feature = namedtuple('Feature', 'idx count')

class Trainer:
    def __init__(self, sentence_to_xy=None, min_count=3,
        l2_cost=1.0, l1_cost=1.0, scan_batch_size=200000,
        max_iter=300, verbose=True):

        if sentence_to_xy is None:
            sentence_to_xy = BaseFeatureTransformer()

        if verbose:
            print('use {}'.format(sentence_to_xy.__class__))

        self.sentence_to_xy = sentence_to_xy
        self.min_count = min_count
        self.l2_cost = l2_cost
        self.l1_cost = l1_cost
        self.scan_batch_size = scan_batch_size
        self.max_iter = max_iter
        self.verbose = verbose

    def scan_features(self, sentences, sentence_to_xy,
        min_count=2, scan_batch_size=1000000):

        def trim(counter, min_count):
            counter = {
                feature:count for feature, count in counter.items()
                # memorize all words no matter how the word occured.
                if (count >= min_count) or (feature[:4] == 'x[0]' and not ', ' in feature)
            }
            return counter

        def print_status(i, counter):
            info = '{} sent, {} features, mem={:f} Gb'.format(
                i, len(counter), get_process_memory())
            print('\r[CRF tagger] scanning from {}'.format(info), end='')

        counter = {}

        for i, sentence in enumerate(sentences):
            # remove infrequent features
            if (i % scan_batch_size == 0):
                counter = trim(counter, min_count)
            # transform sentence to features
            sentence_, _ = sentence_to_xy(sentence)
            # count
            for features in sentence_:
                for feature in features:
                    counter[feature] = counter.get(feature, 0) + 1
            # print status
            if self.verbose and i % 10000 == 0:
                print_status(i, counter)

        # last removal of infrequent features
        counter = trim(counter, min_count)

        if self.verbose:
            print_status(i, counter)
            print(' done.')

        return counter

    def train(self, sentences, model_path=None):

        features = self.scan_features(
            sentences, self.sentence_to_xy,
            self.min_count, self.scan_batch_size)

        # feature encoder
        self._features = {
            # wrapping feature idx and its count
            feature:Feature(idx, count) for idx, (feature, count) in
            # sort features by their count in decreasing order
            enumerate(sorted(features.items(), key=lambda x:-x[1]
            ))
        }

        # feature id decoder
        #self._idx2feature = [
        #    feature for feature in sorted(
        #        self._features, key=lambda x:self._features[x].idx)
        #]

        # temporal file
        if (model_path is None) or (not model_path):
            model_path_ = '_pycrfsuite_model'
        else:
            abspath = os.path.abspath(model_path)
            dirname = os.path.dirname(abspath)
            basename = os.path.basename(abspath).rsplit('.', 1)[0]
            model_path_ = '{}/_{}'.format(dirname, basename)

        # train model using python-crfsuite
        self._train_pycrfsuite(sentences, model_path_)

        # summary
        self._parse_coefficients(model_path_)

        if model_path:
            self._save_as_json(model_path)

    def _train_pycrfsuite(self, sentences, model_path):

        def print_status(i):
            info = 'from {} sents, mem = {:f} Gb'.format(
                i, get_process_memory())
            print('\r[CRF tagger] appending features {}'.format(info), end='')

        trainer = pycrfsuite.Trainer(verbose=self.verbose)

        for i, sentence in enumerate(sentences):

            if self.verbose and i % 2000 == 0:
                print_status(i)

            # transform sentence to features
            x, y = self.sentence_to_xy(sentence)

            # use only conformed feature
            x = [[xij for xij in xi if xij in self._features] for xi in x]

            trainer.append(x, y)

        if self.verbose:
            print_status(i)
            print(' done.\n[CRF tagger] begin training')

        # set pycrfsuite parameters
        params = {
            'feature.minfreq':max(0,self.min_count),
            'max_iterations':max(1, self.max_iter),
            'c1':max(0, self.l1_cost),
            'c2':max(0, self.l2_cost)
        }

        # do train
        trainer.set_params(params)
        trainer.train(model_path)

    def _parse_coefficients(self, model_path):

        if self.verbose:
            print('[CRF tagger] parse trained coefficients', end='')

        # load pycrfsuite trained model
        tagger = pycrfsuite.Tagger()
        tagger.open(model_path)

        # state feature coeffitient
        debugger = tagger.info()
        self.state_features = debugger.state_features
        # transition coefficient
        self.transitions = debugger.transitions

        if self.verbose:
            print(' done')

    def _save_as_json(self, json_path):
        # concatenate key that formed as tuple of str
        state_features_json = {
            ' -> '.join(state_feature):coef
            for state_feature, coef in self.state_features.items()
        }

        # concatenate key that formed as tuple of str
        transitions_json = {
            ' -> '.join(transition):coef
            for transition, coef in self.transitions.items()
        }

        # re-group parameters
        params = {
            'state_features': state_features_json,
            'transitions': transitions_json
            #'idx2feature': self._idx2feature,
            #'features': self._features
        }

        # save
        with open(json_path, 'w', encoding='utf-8') as f:
            json.dump(params, f, ensure_ascii=False, indent=2)
