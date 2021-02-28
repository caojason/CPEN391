import numpy as np
from PIL import Image

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

def reconstruct(img_arr, start_x, start_y, R_value, G_value, B_value, height, width, count):
    current_count = 0
    for i in range(start_y, height):
        for j in range(start_x, width):
            if i == 337:
                break
            if j == width - 1:
                start_x = 0
            if current_count == count:
                return i, j
            img_arr[i][j][0] = R_value
            img_arr[i][j][1] = G_value
            img_arr[i][j][2] = B_value
            current_count += 1
    return height, width


def decompression(compressedFilePath):
    with open(compressedFilePath, "rb+") as f:
        # read the first four bytes for the size
        width = f.read(2)
        width = int.from_bytes(width, byteorder="big", signed=False)
        height = f.read(2)
        height = int.from_bytes(height, byteorder="big", signed=False)
        img_arr = np.zeros((height, width, 3), dtype=np.uint8)
        start_x = 0
        start_y = 0
        while True:
            R_byte = f.read(1)
            G_byte = f.read(1)
            B_byte = f.read(1)
            count_byte = f.read(1)
            if not R_byte or not G_byte or not B_byte or not count_byte:
                break
            R_value = int.from_bytes(R_byte, byteorder="big", signed=False)
            G_value = int.from_bytes(G_byte, byteorder="big", signed=False)
            B_value = int.from_bytes(B_byte, byteorder="big", signed=False)
            count = int.from_bytes(count_byte, byteorder="big", signed=False)

            start_y, start_x = reconstruct(img_arr, start_x, start_y, R_value, G_value, B_value, height, width, count)
        
        img = Image.fromarray(img_arr)
        img.save("out.png")
        



def main():
    with open("compressed.txt", "wb+") as f:
        compressedImg = compression("C:\\Users\\yunta\\Desktop\\third_year\\CPEN391\\Torch\\Hardware\\logo.png")
        f.write(compressedImg)
        f.flush()
        f.close()
    
    decompression("compressed.txt")


main()