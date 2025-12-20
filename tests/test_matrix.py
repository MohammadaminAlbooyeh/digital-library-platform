import unittest
import numpy as np
from matrix_operation import Matrix

class TestMatrix(unittest.TestCase):
    
    def setUp(self):
        self.m1 = Matrix([[1, 2], [3, 4]])
        self.m2 = Matrix([[5, 6], [7, 8]])
        self.m3 = Matrix([[1, 0], [0, 1]])  # Identity matrix
    
    def test_addition(self):
        result = self.m1.add(self.m2)
        expected = Matrix([[6, 8], [10, 12]])
        self.assertTrue(np.allclose(result.data, expected.data))
    
    def test_subtraction(self):
        result = self.m2.subtract(self.m1)
        expected = Matrix([[4, 4], [4, 4]])
        self.assertTrue(np.allclose(result.data, expected.data))
    
    def test_multiplication(self):
        result = self.m1.multiply(self.m2)
        expected = Matrix([[19, 22], [43, 50]])
        self.assertTrue(np.allclose(result.data, expected.data))
    
    def test_transpose(self):
        result = self.m1.transpose()
        expected = Matrix([[1, 3], [2, 4]])
        self.assertTrue(np.allclose(result.data, expected.data))
    
    def test_determinant(self):
        det = self.m1.determinant()
        expected = -2.0
        self.assertAlmostEqual(det, expected)
    
    def test_inverse(self):
        result = self.m3.inverse()
        self.assertTrue(np.allclose(result.data, self.m3.data))

if __name__ == '__main__':
    unittest.main()