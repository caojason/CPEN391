from PIL import Image
import numpy

img = Image.open("logo.png")

arr = numpy.array(img)

if arr.shape[2] == 4:
    new_arr = numpy.zeros([arr.shape[0], arr.shape[1], 3], dtype=numpy.uint8)
    for i in range(arr.shape[0]):
        for j in range(arr.shape[1]):
            new_arr[i][j][0:3] = arr[i][j][0:3]
    arr = new_arr

with open("bytes.txt", "wb+") as f:
    f.write(arr)