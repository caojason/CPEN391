import cv2
from PIL import Image
import numpy as np
import os
from os.path import isfile, join

def convert_frames_to_video(pathIn,pathOut,fps):
    frame_array = []
    files = [f for f in os.listdir(pathIn) if isfile(join(pathIn, f))]
    #for sorting the file names properly
    files.sort(key = lambda x: int(x[0:2]))
    for i in range(len(files)):
        filename=pathIn +"/"+files[i]
        print(filename)
        #reading each files
        img = cv2.imread(filename)
        height, width, layers = img.shape
        size = (width,height)
        #inserting the frames into an image array
        frame_array.append(img)
    out = cv2.VideoWriter(pathOut,cv2.VideoWriter_fourcc(*'MP4V'), fps, size)
    for i in range(len(frame_array)):
        # writing to a image array
        out.write(frame_array[i])
    out.release()

def reconstruct(img_arr, start_x, start_y, R_value, G_value, B_value, height, width, count):
    current_count = 0
    for i in range(start_y, height):
        for j in range(start_x, width):
            if j == width - 1:
                start_x = 0
            if current_count == count:
                return i, j
            img_arr[i][j][0] = R_value
            img_arr[i][j][1] = G_value
            img_arr[i][j][2] = B_value
            current_count += 1
    return height, width

def decompression(compressedFilePath, outputFilePath):
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
        img.save(outputFilePath)
    os.remove(compressedFilePath)