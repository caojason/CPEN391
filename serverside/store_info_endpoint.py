import os
import sys
sys.path.append(os.getcwd())
import modules.database.store_info_database as SD

from app import app

from flask import request, jsonify

@app.route("/create_store", methods=["POST"])
def create_store():
    return "success"

