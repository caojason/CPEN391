import os
import sys
sys.path.append(os.getcwd())
import modules.database.store_info_database as SD

from app import app

from flask import request, jsonify
import json

@app.route("/create_store", methods=["POST"])
def create_store():
    data_json = request.get_json()
    print(data_json)
    try:
        print(data_json["data"])
        store_info_dict = json.loads(data_json["data"])
        encodedLogo = store_info_dict["encodedLogo"]
        latitude = store_info_dict["latitude"]
        longitude = store_info_dict["longitude"]
        macAddr = store_info_dict["macAddr"]
        storeName = store_info_dict["storeName"]
        storeOwnerId = store_info_dict["storeOwnerId"]
        SD.insert_table_store_info(encodedLogo, latitude, longitude, macAddr, storeName, storeOwnerId)
        print("create store success")
        return "success"
    except:
        print("create store failed")
        return "failed"


@app.route("/get_stores", methods=["GET"])
def get_store_list():
    uid = request.args["uid"] if "uid" in request.args else "\"\""
    return jsonify(SD.get_store_info_records(uid))