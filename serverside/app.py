import os
import sys
sys.path.append(os.getcwd())

from flask import Flask, request
from modules.database.init_database import init_torch_database

init_torch_database()

app = Flask(__name__)

import user_endpoint
import store_info_endpoint
#import population_endpoint

@app.route("/")
def home():
    return "Torch"

@app.route("/test", methods = ["POST"])
def test():
    print(request.get_data())
    print(request.form)
    return ""


#usage :
# in serverside folder
# set FLASK_APP=app.py
# set FLASK_ENV=development
# flask run
