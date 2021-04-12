from picamera import PiCamera
from time import sleep
from PIL import Image
import numpy as np

def compression(imgPath):
    img = Image.open(imgPath)
    img_arr = np.array(img, dtype=np.uint8)
    compressed_arr = []
    color = img_arr[0][0]

    count = 0

    # the first 4 bytes will be the size of the image width, height
    compressed_arr.append(img_arr.shape[1] // 256)
    compressed_arr.append(img_arr.shape[1] % 256)
    compressed_arr.append(img_arr.shape[0] // 256)
    compressed_arr.append(img_arr.shape[0] % 256)

    for i in range(img_arr.shape[0]):
        for j in range(img_arr.shape[1]):
            newColor = img_arr[i][j]
            if count == 255:
                #we don't want the count to be larger than 255
                compressed_arr.append(color[0])
                compressed_arr.append(color[1])
                compressed_arr.append(color[2])
                compressed_arr.append(count)
                #get the new color
                color = newColor
                count = 1
            elif newColor[0] == color[0] and newColor[1] == color[1] and newColor[2] == color[2]:
                count += 1
            else:
                #store the values
                compressed_arr.append(color[0])
                compressed_arr.append(color[1])
                compressed_arr.append(color[2])
                compressed_arr.append(count)
                #get the new color
                color = newColor
                count = 1
    if len(compressed_arr) != 0:
        compressed_arr.append(color[0])
        compressed_arr.append(color[1])
        compressed_arr.append(color[2])
        compressed_arr.append(count)
    return np.array(compressed_arr, dtype=np.uint8)


camera = PiCamera()

camera.start_preview()
sleep(5)
camera.capture("test.jpg")
camera.stop_preview()


with open("compressed.txt", "wb+") as f:
    compressedImg = compression("/home/pi/Documents/CPEN 391/Torch/Pi/test.jpg")
    f.write(compressedImg)
    f.flush()
    f.close()