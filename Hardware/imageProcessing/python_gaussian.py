import cv2
import matplotlib.pyplot as plt
import numpy as np

#this file is only for testing the correctness of our Gaussian filter

img = cv2.imread("noise.png")
img_arr = np.array(img)

out = cv2.GaussianBlur(img, (3,3), 1)
out_arr = np.array(out)

kernel = np.array([
    [0.075114,0.123841,0.075114],
    [0.123841,0.204180,0.123841], 
    [0.075114,0.123841,0.075114]
    ])

test_arr = np.zeros(img_arr.shape, dtype=np.uint8)

for r in range(img_arr.shape[0]):
    for c in range(img_arr.shape[1]):
        for d in range(img_arr.shape[2]):
            sum = 0.0
            for kr in range(-1, 2):
                for kc in range(-1, 2):
                    pixel_row = r + kr
                    pixel_col = c + kc
                    if pixel_row < 0:
                        pixel_row = 0
                    elif pixel_row >= img_arr.shape[0]:
                        pixel_row = img_arr.shape[0] - 1
                    if pixel_col < 0:
                        pixel_col = 0
                    elif pixel_col >= img_arr.shape[1]:
                        pixel_col = img_arr.shape[1] - 1
                    sum += kernel[kr+1][kc+1] * img_arr[pixel_row][pixel_col][d]
            test_arr[r][c][d] = np.round(sum)

fig = plt.figure()
ax1 = fig.add_subplot(121)
ax2 = fig.add_subplot(122)
ax1.imshow(img_arr)
ax2.imshow(out_arr)
plt.show()