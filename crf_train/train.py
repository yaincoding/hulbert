#!/usr/bin/python3

import argparse

from utils import Corpus
from transformer import BaseFeatureTransformer, TrigramFeatureTransformer
from trainer import Trainer


def train(corpus_path, save_path, feature_transformer):
    corpus = Corpus(corpus_path)
    if feature_transformer == "base":
        featureTransformer = BaseFeatureTransformer()
    else:
        featureTransformer = TrigramFeatureTransformer()
    trainer = Trainer(
        sentence_to_xy = featureTransformer,
        max_iter = 300,
        l1_cost = 0,
        l2_cost = 1,
        verbose = True
    )

    trainer.train(corpus)

    trainer._save_as_json(save_path)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--corpus_path", help="corpus file path")
    parser.add_argument("--save_path", help="path to save trained model")
    parser.add_argument("--feature_transformer", help="type of feature transformer to train model")
    args = parser.parse_args()

    corpus_path = args.corpus_path
    save_path = args.save_path
    feature_transformer = args.feature_transformer

    train(corpus_path, save_path, feature_transformer)
