# Matrix Operation Engine

A Python library for performing various matrix operations such as addition, multiplication, inversion, and more.

## Features

- Basic matrix operations (addition, subtraction, multiplication)
- Advanced operations (determinant, inverse, eigenvalues)
- Efficient implementation using NumPy

## Installation

1. Clone the repository:
   ```

2. Install dependencies:
   ```
   pip install -r requirements.txt
   ```

## Usage

```python
from matrix_operation_engine import Matrix

# Create matrices
m1 = Matrix([[1, 2], [3, 4]])
m2 = Matrix([[5, 6], [7, 8]])

# Perform operations
result_add = m1.add(m2)
result_mul = m1.multiply(m2)

print("Addition:", result_add.data)
print("Multiplication:", result_mul.data)
```

## Testing

Run tests with:
```
python -m unittest discover tests
```

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.