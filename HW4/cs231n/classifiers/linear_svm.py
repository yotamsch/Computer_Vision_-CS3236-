import numpy as np
from random import shuffle
from past.builtins import xrange

def svm_loss_naive(W, X, y, reg):
  """
  Structured SVM loss function, naive implementation (with loops).

  Inputs have dimension D, there are C classes, and we operate on minibatches
  of N examples.

  Inputs:
  - W: A numpy array of shape (D, C) containing weights.
  - X: A numpy array of shape (N, D) containing a minibatch of data.
  - y: A numpy array of shape (N,) containing training labels; y[i] = c means
    that X[i] has label c, where 0 <= c < C.
  - reg: (float) regularization strength

  Returns a tuple of:
  - loss as single float
  - gradient with respect to weights W; an array of same shape as W
  """
  dW = np.zeros(W.shape) # initialize the gradient as zero
  # compute the loss and the gradient
  num_classes = W.shape[1]
  num_train = X.shape[0]
  loss = 0.0
  for i in xrange(num_train):
    scores = X[i].dot(W)
    correct_class_score = scores[y[i]]
    for j in xrange(num_classes):
      if j == y[i]:
        continue
      margin = scores[j] - correct_class_score + 1 # note delta = 1
      if margin > 0:
        loss += margin
        dW[:,j] += X[i]
        dW[:,y[i]] -= X[i]

  # Right now the loss is a sum over all training examples, but we want it
  # to be an average instead so we divide by num_train.
  loss /= num_train

  # Add regularization to the loss.
  loss += reg * np.sum(W * W)

  # Normalizing the dW
  dW /= num_train
  # regularization 
  dW += reg * 2 * W

  return loss, dW


def svm_loss_vectorized(W, X, y, reg):
  """
  Structured SVM loss function, vectorized implementation.

  Inputs and outputs are the same as svm_loss_naive.
  """
  loss = 0.0
  num_train = X.shape[0]
  num_classes = W.shape[1]

  scores = X.dot(W) # calculate the Matrix of scores
  margin = np.maximum(0, scores - np.tile(scores[np.arange(num_train),y], (num_classes,1)).T + 1) # notice delta=1

  margin[np.arange(num_train), y] = 0 # a trick to 0 out the y (which will be equal to delta)
  dW_indicator = np.zeros(margin.shape)
  dW_indicator[margin > 0] = 1
  dW_indicator[np.arange(num_train), y] = dW_indicator.sum(axis=1) * -1
  dW = X.T.dot(dW_indicator)


  loss = margin.sum()
  loss /= num_train # average the loss
  loss += reg * np.sum(W * W) # regularization 

  dW /= num_train # normalize the dW
  dW += reg * 2 * W # regularization 

  return loss, dW
