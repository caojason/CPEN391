import os
import sys
sys.path.append(os.getcwd())

import base64

from flask import Flask, request
from modules.database.init_database import init_torch_database

init_torch_database()

app = Flask(__name__)

import user_endpoint
import store_info_endpoint
import population_endpoint

@app.route("/")
def home():
    return "Torch"

@app.route("/test", methods = ["GET"])
def test():
    # get the actual path to image
    path_to_image = os.path.join(os.getcwd(), "..", "Hardware", "imageProcessing", "3x3.png")
    encoded_image = ""
    with open(path_to_image, "rb+") as img_file:
        encoded_image = base64.b64encode(img_file.read())
    return encoded_image


#usage :
# in serverside folder
# set FLASK_APP=app.py
# set FLASK_ENV=development
# flask run
# To run flask on the server
# cd Torch/serverside
# flask run --host=0.0.0.0
