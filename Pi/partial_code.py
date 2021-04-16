import base64
import requests
import numpy as np
from PIL import Image
import json

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
    return np.array(compressed_arr, dtype=np.uint8)


def main():
    compressed = compression("image file")
    encoded = base64.b64encode(compressed)
    macAddr = "FF:FF:FF:FF:FF:FF" # you should be able to get this by bluetooth connection with DE1, it is the mac address of the DE1 board.
    requests.post("http://35.233.184.107:5000/upload_video", json={"location": macAddr, "data": encoded})
    # if it is not working, use the following
    data = json.dumps({"location": macAddr, "data": encoded})
    headers = {'Content-type': 'application/json'}
    requests.post("http://35.233.184.107:5000/upload_video", data=data, headers=headers)