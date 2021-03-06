from tensorflow.keras.applications.mobilenet_v2 import preprocess_input
from tensorflow.keras.preprocessing.image import img_to_array
from tensorflow.keras.models import load_model
from imutils.video import VideoStream
import numpy as np
import argparse
import imutils
import time
import cv2
import os
import datetime

proto_txt_path = os.path.join(os.getcwd(), "modules", "person_counter_opencv",'deploy.prototxt')
model_path = os.path.join(os.getcwd(), "modules", "person_counter_opencv",'res10_300x300_ssd_iter_140000.caffemodel')
face_detector = cv2.dnn.readNetFromCaffe(proto_txt_path, model_path)

mask_detector = load_model(os.path.join(os.getcwd(), "modules", "person_counter_opencv",'mask_detector.model'))

def facemask_detector():
    cap = cv2.VideoCapture(os.path.join(os.getcwd(), "modules", "person_counter_opencv",'mask.mp4'))
    count=0
    tempx=[]
    tempy=[]
    while True:
        ret, frame = cap.read()
        if frame is None:
            break
        frame = imutils.resize(frame, width=400)
        (h, w) = frame.shape[:2]
        blob = cv2.dnn.blobFromImage(frame, 1.0, (300, 300), (104, 177, 123))

        face_detector.setInput(blob)
        detections = face_detector.forward()

        faces = []
        bbox = []
        results = []

        for i in range(0, detections.shape[2]):
            confidence = detections[0, 0, i, 2]

            if confidence > 0.5:
                box = detections[0, 0, i, 3:7] * np.array([w, h, w, h])
                (startX, startY, endX, endY) = box.astype("int")
            


                face = frame[startY:endY, startX:endX]
                face = cv2.cvtColor(face, cv2.COLOR_BGR2RGB)
                face = cv2.resize(face, (224, 224))
                face = img_to_array(face)
                face = preprocess_input(face)
                face = np.expand_dims(face, axis=0)

                faces.append(face)
                bbox.append((startX, startY, endX, endY))

        if len(faces) > 0:
            results = mask_detector.predict(faces)

        for (face_box, result) in zip(bbox, results):
            (startX, startY, endX, endY) = face_box
            

            (mask, withoutMask) = result
            if startX not in tempx and startY not in tempy:
                tempx.append(startX)
                tempx.append(startX+1)
                tempx.append(startX-1)
                tempy.append(startY)
                tempy.append(startY+1)
                tempy.append(startY-1)
                tempy.append(startY+2)
                tempy.append(startY-2)
                tempx.append(startX+2)
                tempx.append(startX-2)
                tempx.append(startX+3)
                tempx.append(startX-3)
                tempy.append(startY+3)
                tempy.append(startY-3)
                tempx.append(startX+4)
                tempx.append(startX-4)
                tempy.append(startY+4)
                tempy.append(startY-4)
                tempx.append(startX+5)
                tempx.append(startX-5)
                tempy.append(startY+5)
                tempy.append(startY-5)
                if mask > withoutMask:
                    count=count+1
                
    return count


print(facemask_detector())
           

        
        