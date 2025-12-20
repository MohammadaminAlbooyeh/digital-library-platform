import numpy as np
from .matrix import Matrix

def add_matrices(m1, m2):
    """Add two matrices."""
    return m1.add(m2)

def multiply_matrices(m1, m2):
    """Multiply two matrices."""
    return m1.multiply(m2)

def scalar_multiply(matrix, scalar):
    """Multiply a matrix by a scalar."""
    return Matrix(matrix.data * scalar)

def matrix_power(matrix, power):
    """Raise a matrix to a power."""
    if matrix.data.shape[0] != matrix.data.shape[1]:
        raise ValueError("Matrix must be square")
    return Matrix(np.linalg.matrix_power(matrix.data, power))

def eigenvalues(matrix):
    """Compute eigenvalues of a matrix."""
    if matrix.data.shape[0] != matrix.data.shape[1]:
        raise ValueError("Matrix must be square")
    return np.linalg.eigvals(matrix.data)

def eigenvectors(matrix):
    """Compute eigenvectors of a matrix."""
    if matrix.data.shape[0] != matrix.data.shape[1]:
        raise ValueError("Matrix must be square")
    return np.linalg.eig(matrix.data)