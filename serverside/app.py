import os
import sys
sys.path.append(os.getcwd())

from flask import Flask
from modules.database.init_database import init_torch_database

init_torch_database()

app = Flask(__name__)

import user_endpoint
import store_info_endpoint
import population_endpoint

@app.route("/")
def home():
    return "Torch"


#usage :
# in serverside folder
# set FLASK_APP=app.py
# set FLASK_ENV=development
# flask run