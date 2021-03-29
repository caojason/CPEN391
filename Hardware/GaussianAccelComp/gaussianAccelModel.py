import numpy as np

# This file is only for finding the correct of outputs for the Gaussian filter

kernel = np.array([
    [0.075114,0.123841,0.075114],
    [0.123841,0.204180,0.123841], 
    [0.075114,0.123841,0.075114]
    ])

img_arr = np.array([
    [1, 2, 3],
    [4, 50, 6],
    [7, 8, 9]
    ])

# row, col = 1 (middle value of 3x3 img)
r = 1
c = 1
sum = 0.0

for kr in range(-1, 2):
    for kc in range(-1, 2):
        pixel_row = r + kr
        pixel_col = c + kc
        if pixel_row < 0:
            pixel_row = 0
        elif pixel_row >= 3:
            pixel_row = 3 - 1
        if pixel_col < 0:
            pixel_col = 0
        elif pixel_col >= 3:
            pixel_col = 3 - 1
        sum += kernel[kr+1][kc+1] * img_arr[pixel_row][pixel_col]

print(sum)