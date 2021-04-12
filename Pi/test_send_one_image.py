from picamera import PiCamera
from time import sleep
from math import ceil
import serial
import base64
import requests
import json
import numpy as np

def compression(img_arr):
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

    if color[0] != compressed_arr[-4] and color[1] != compressed_arr[-3] and color[2] != compressed_arr[-2]:
        compressed_arr.append(color[0])
        compressed_arr.append(color[1])
        compressed_arr.append(color[2])
        compressed_arr.append(count)

    return np.array(compressed_arr, dtype=np.uint8)


# start of main code
try:
    # initialize serial port
    ser = serial.Serial()
    ser.baudrate = 115200
    ser.port = "/dev/rfcomm0"
    ser.open()
    print("Opened serial port")


    # capture image and store it in a np array
    width = 1920
    height = 1080
    depth = 3
    # the np array needs to have witdth that is a multiple of 32
    # and a hight that is a multipl of 16
    # here we calculate the size of the storage array
    storage_width = ceil(width / 32) * 32
    storage_height = ceil(height / 16) * 16

    camera = PiCamera()
    camera.resolution = (width, height)
    camera.framerate = 24
    sleep(2)
    print("Camera initialization complete")

    # array used to store the images sent back from the DE1
    processed_image = np.empty([height, width, depth], dtype=np.uint8)

    # number of images to send
    for i in range(1):

        image_data = np.empty((storage_height * storage_width * depth,), dtype=np.uint8)
        camera.capture(image_data, 'rgb')
        image_data = image_data.reshape((storage_height, storage_width, depth))
        image_data = image_data[:height, :width, :]

        # # send image to DE1-SoC
        for y in range(height):
            for x in range(width):
                for z in range(depth):
                    data_byte = bytes([int(image_data[y][x][z])])
                    ser.write(data_byte)
                    print("sent", data_byte, "at", x, y, z)


        # # wait for DE1-SoC to send image back
        
        x = 0
        y = 0
        z = 0
        x_carry = 0
        z_carry = 0
        while(not(x == width - 1 and y == height - 1 and z == depth - 1)):
            if(ser.in_waiting != 0):
                processed_image[y, x, z] = ser.read() * 2
                print("Received byte:", processed_image[y, x, z], "at", x, y, z)
                # increment the indices
                z += 1
                z_carry = z // depth
                z %= 3

                x += z_carry
                x_carry = x // width
                x %= width

                y += x_carry
                y %= height


        compressed_image = compression(image_data)
        encoded_image = base64.b64encode(compressed_image)
        macAddr = "20:18:11:20:33:41"

        requests.post("http://35.233.184.107:5000/upload_video", json={"location": macAddr, "data": encoded_image})

except KeyboardInterrupt:
    pass
finally:
    # pass
    ser.close()
