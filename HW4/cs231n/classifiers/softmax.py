import numpy as np
from random import shuffle
from past.builtins import xrange

def softmax_loss_naive(W, X, y, reg):
  """
  Softmax loss function, naive implementation (with loops)

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
  # Initialize the loss and gradient to zero.
  loss = 0.0
  dW = np.zeros_like(W)

  num_train = X.shape[0]
  num_classes = W.shape[1]

  for i in np.arange(num_train):
    scores = X[i].dot(W)
    scores -= np.max(scores) # normalization
    p = np.exp(scores) / np.sum(np.exp(scores)) # calculate the prob function
    loss += -np.log(p[y[i]]) # calculate the addition to the overall loss
    for j in np.arange(num_classes):
      if j == y[i]:
        dW[:,j] += (p[y[i]] - 1) * X[i] # the gradient where i == j
      else:
        dW[:,j] += p[j] * X[i] # the gradient where i != j

  # normalize and regularization on the loss
  loss /= num_train
  loss += reg * np.sum(W * W)
  
  # normalize the gradient
  dW /= num_train
  dW += reg * 2 * W

  return loss, dW


def softmax_loss_vectorized(W, X, y, reg):
  """
  Softmax loss function, vectorized version.

  Inputs and outputs are the same as softmax_loss_naive.
  """
  # Initialize the loss and gradient to zero.
  loss = 0.0
  dW = np.zeros_like(W)

  num_train = X.shape[0]
  num_classes = W.shape[1]

  # get the scores and normalize them
  scores = X.dot(W)
  scores -= np.tile(np.max(scores,axis=1), (num_classes,1)).T
  
  # get the p function 
  p = np.exp(scores) / np.tile(np.sum(np.exp(scores),axis=1), (num_classes,1)).T

  # calculate the loss
  # NOTE: For some reason, the first attemp threw "devide by 0 on log" error which i couldn't solve. So I made the equivalant calculation.
  # loss = np.sum(-1 * np.log(p[np.arange(num_train), y]))
  loss = np.sum(-scores[np.arange(num_train), y] + np.log(np.sum(np.exp(scores), axis=1 )))

  # normalize and regularization on the loss
  loss /= num_train
  loss += reg * np.sum(W * W)

  p_masked = p
  p_masked[np.arange(num_train), y] -= 1

  # calculate the gradient based on the mask
  dW = X.T.dot(p_masked)

  # normalize the gradient
  dW /= num_train
  dW += reg * 2 * W 

  return loss, dW

