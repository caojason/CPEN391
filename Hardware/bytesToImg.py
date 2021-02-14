import numpy
from PIL import Image

arr = numpy.zeros([636, 636, 4], dtype=numpy.uint8)

with open("out.txt", "rb+") as f:
    for i in range(636):
        for j in range(636):
            for d in range(4):
                # the file will be a byte stream
                b = f.read(1)
                arr[i][j][d] = int.from_bytes(b,byteorder="big",signed=False)

img = Image.fromarray(arr)
img.save("test.png")