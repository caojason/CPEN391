import cv2
import datetime
import imutils
import numpy as np
import sys
import os
sys.path.append(os.getcwd())
from modules.person_counter_opencv.centroidtracker import CentroidTracker


protopath = os.path.join(os.getcwd(), "modules", "person_counter_opencv", "MobileNetSSD_deploy.prototxt")
modelpath = os.path.join(os.getcwd(), "modules", "person_counter_opencv", "MobileNetSSD_deploy.caffemodel")
detector = cv2.dnn.readNetFromCaffe(prototxt=protopath, caffeModel=modelpath)
tracker=CentroidTracker(maxDisappeared=80,maxDistance=90)



CLASSES = ["background", "aeroplane", "bicycle", "bird", "boat",
           "bottle", "bus", "car", "cat", "chair", "cow", "diningtable",
           "dog", "horse", "motorbike", "person", "pottedplant", "sheep",
           "sofa", "train", "tvmonitor"]

def non_max_suppression_fast(boxes, overlapThresh):
    try:
        if len(boxes) == 0:
            return []

        if boxes.dtype.kind == "i":
            boxes = boxes.astype("float")

        pick = []

        x1 = boxes[:, 0]
        y1 = boxes[:, 1]
        x2 = boxes[:, 2]
        y2 = boxes[:, 3]

        area = (x2 - x1 + 1) * (y2 - y1 + 1)
        idxs = np.argsort(y2)

        while len(idxs) > 0:
            last = len(idxs) - 1
            i = idxs[last]
            pick.append(i)

            xx1 = np.maximum(x1[i], x1[idxs[:last]])
            yy1 = np.maximum(y1[i], y1[idxs[:last]])
            xx2 = np.minimum(x2[i], x2[idxs[:last]])
            yy2 = np.minimum(y2[i], y2[idxs[:last]])

            w = np.maximum(0, xx2 - xx1 + 1)
            h = np.maximum(0, yy2 - yy1 + 1)

            overlap = (w * h) / area[idxs[:last]]

            idxs = np.delete(idxs, np.concatenate(([last],
                                                   np.where(overlap > overlapThresh)[0])))

        return boxes[pick].astype("int")
    except Exception as e:
        print("Exception occurred in non_max_suppression : {}".format(e))



def people_counter(path):
    #define the video file here, or put 0 to use your webcam.
    cap = cv2.VideoCapture(path)
    MaxLpc=0
    fps_start_time = datetime.datetime.now()
    fps = 0
    total_frames = 0
    object_id_list=[]
    counter=0
    OPC=0
    while True:
        ret, frame = cap.read()
        if frame is None:
            break
        frame = imutils.resize(frame, width=600)
        total_frames = total_frames + 1

        (H, W) = frame.shape[:2]

        blob = cv2.dnn.blobFromImage(frame, 0.007843, (W, H), 127.5)

        detector.setInput(blob)
        person_detections = detector.forward()
        rects=[]
        for i in np.arange(0, person_detections.shape[2]):
            confidence = person_detections[0, 0, i, 2]
            if confidence > 0.5:
                idx = int(person_detections[0, 0, i, 1])

                if CLASSES[idx] != "person":
                    continue

                person_box = person_detections[0, 0, i, 3:7] * np.array([W, H, W, H])
                (startX, startY, endX, endY) = person_box.astype("int")
                rects.append(person_box)
                
        boundingbox=np.array(rects)
        boundingbox=boundingbox.astype(int)
        rects=non_max_suppression_fast(boundingbox,0.3)
        objects=tracker.update(rects)
        for (objectId,bbox) in objects.items():
            x1,y1,x2,y2=bbox
            x1=int(x1)
            y1=int(y1)
            x2=int(x2)
            y2=int(y2)
            if objectId not in object_id_list:
                object_id_list.append(objectId)
             

           
            cv2.rectangle(frame,(x1,y1),(x2,y2),(0,0,255),2)
            text="ID: {}".format(objectId)
            cv2.putText(frame,text,(x1,y1-5),cv2.FONT_HERSHEY_COMPLEX_SMALL,0.8,(0,0,255),1)
            
        counter=counter+1    
        fps_end_time = datetime.datetime.now()
        time_diff = fps_end_time - fps_start_time
        if time_diff.seconds == 0:
            fps = 0.0
        else:
            fps = (total_frames / time_diff.seconds)

        fps_text = "FPS: {:.2f}".format(fps)
        lpc_count=len(objects)
        OPC=len(object_id_list)
        lpc_txt="LPC:{}".format(lpc_count)
        cv2.putText(frame,lpc_txt,(5,55),cv2.FONT_HERSHEY_COMPLEX_SMALL,1,(0,0,255),1)
        timenow=datetime.datetime.now()
        timeText="{}".format(timenow)
        cv2.putText(frame, timeText, (5, 15), cv2.FONT_HERSHEY_COMPLEX_SMALL, 1, (0, 0, 255), 1)
        cv2.putText(frame, fps_text, (5, 35), cv2.FONT_HERSHEY_COMPLEX_SMALL, 1, (0, 0, 255), 1)
        if MaxLpc < lpc_count:
            MaxLpc=lpc_count
            cv2.imwrite("outPut.png",frame)
       
      
       
        key = cv2.waitKey(1)
        if key == ord('q'):
            break
    
    cv2.destroyAllWindows()
    return OPC

print(people_counter("output.jpg"))
