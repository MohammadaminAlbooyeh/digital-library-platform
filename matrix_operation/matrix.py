import numpy as np

class Matrix:
    def __init__(self, data):
        """
        Initialize a Matrix object with a 2D list or numpy array.
        
        :param data: 2D list or numpy array representing the matrix
        """
        if isinstance(data, list):
            self.data = np.array(data, dtype=float)
        elif isinstance(data, np.ndarray):
            self.data = data.astype(float)
        else:
            raise ValueError("Data must be a 2D list or numpy array")
        
        if self.data.ndim != 2:
            raise ValueError("Data must be 2D")
    
    def __str__(self):
        return str(self.data)
    
    def __repr__(self):
        return f"Matrix({self.data.tolist()})"
    
    def shape(self):
        """Return the shape of the matrix (rows, columns)."""
        return self.data.shape
    
    def add(self, other):
        """Add two matrices."""
        if not isinstance(other, Matrix):
            raise TypeError("Can only add Matrix objects")
        return Matrix(self.data + other.data)
    
    def subtract(self, other):
        """Subtract two matrices."""
        if not isinstance(other, Matrix):
            raise TypeError("Can only subtract Matrix objects")
        return Matrix(self.data - other.data)
    
    def multiply(self, other):
        """Multiply two matrices."""
        if not isinstance(other, Matrix):
            raise TypeError("Can only multiply Matrix objects")
        return Matrix(np.dot(self.data, other.data))
    
    def transpose(self):
        """Return the transpose of the matrix."""
        return Matrix(self.data.T)
    
    def determinant(self):
        """Calculate the determinant of the matrix."""
        if self.data.shape[0] != self.data.shape[1]:
            raise ValueError("Matrix must be square")
        return np.linalg.det(self.data)
    
    def inverse(self):
        """Return the inverse of the matrix."""
        if self.data.shape[0] != self.data.shape[1]:
            raise ValueError("Matrix must be square")
        return Matrix(np.linalg.inv(self.data))