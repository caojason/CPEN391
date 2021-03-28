import sys
import os
sys.path.append(os.getcwd())

import json
import numpy as np
from PIL import Image
import base64

from modules.database.init_database import init_torch_database
from pytest_mysql import factories

mysql_my_proc = factories.mysql_proc(port=3306)
mysql_my = factories.mysql("mysql_my_proc")

init_torch_database()

from app import app
app.testing = True

import modules.database.population_database as PD

def test_get_weekly_data():
    PD.insert_table_population("FF:FF:FF:FF:FF:FF", 100)

    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?location=FF:FF:FF:FF:FF:FF")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"100" in rv.data

def test_get_weekly_data_1():
    PD.insert_table_population("A", 100)

    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?location=FF:FF:FF:FF:FF:FF")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"0" in rv.data

def test_get_weekly_data_2():
    PD.insert_table_population("B", 100)

    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?location=B")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"100" in rv.data  
    
    PD.insert_table_population("B", 2)
    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?location=B")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"102" in rv.data  

def test_get_analysis_no_data():
    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_analysis?year=2021&location=B")
        assert rv.status_code == 200
        assert b"no data" in rv.data  

def test_get_analysis():
    PD.insert_table_population("A", 100)
    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_analysis?year=2021&location=A")
        assert rv.status_code == 200
        # assert b"highest" in rv.data  

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

def test_get_image():
    original_path = os.path.join("tests", "60x60.png")
    with open(original_path, "rb+") as img_file:
        encoded_image = base64.b64encode(img_file.read())

    compressed_img = compression(original_path)
    with open("compressed_img.txt", "wb+") as f:
        f.write(compressed_img)
        f.flush()
        compressed_img = f.read().decode("utf-8")

    with app.test_client() as testing_client:
        rv = testing_client.post("/upload_video", 
                    data=json.dumps({"location":"FF:FF:FF:FF:FF:FF","data":compressed_img}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_image_analysis?macAddr=FF:FF:FF:FF:FF:FF")
        assert rv.status_code == 200
        assert bytes(encoded_image, "utf-8") in rv.data