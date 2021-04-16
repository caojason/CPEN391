import numpy as np

def compression(img):
    compressed_arr = []
    color = img[0][0]

    count = 0

    # the first 4 bytes will be the size of the image width, height
    compressed_arr.append(img.shape[1] // 256)
    compressed_arr.append(img.shape[1] % 256)
    compressed_arr.append(img.shape[0] // 256)
    compressed_arr.append(img.shape[0] % 256)

    for i in range(img.shape[0]):
        for j in range(img.shape[1]):
            newColor = img[i][j]
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