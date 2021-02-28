import numpy
from PIL import Image

HEIGHT = 60
WIDTH = 60
DEPTH = 3

arr = numpy.zeros([HEIGHT, WIDTH, DEPTH], dtype=numpy.uint8)

with open("out.txt", "rb+") as f:
    for i in range(HEIGHT):
        for j in range(WIDTH):
            for d in range(DEPTH):
                # the file will be a byte stream
                b = f.read(1)
                arr[i][j][d] = int.from_bytes(b,byteorder="big",signed=False)

img = Image.fromarray(arr)
img.save("test.png")