from PIL import Image
import numpy

img = Image.open("logo.png")

arr = numpy.array(img)

with open("bytes.txt", "wb+") as f:
    f.write(arr)