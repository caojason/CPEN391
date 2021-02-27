import os
import sys
import json
import smtplib
sys.path.append(os.getcwd())
import modules.database.store_info_database as SD
import modules.database.user_database.py as UD
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


@app.route("/request",method=["GET"])
def create_permission_link():
    data_json = request.get_json()
    request_info=json.loads(data_json["data"])
    email=request_info["email"]
    message=request_info["message"]
    ownerId=request_info["ownerId"]
    uid=UD.get_uid(email)
    storeInfo=SD.get_store_info_records(ownerId)
    macAddr=storeInfo["macAddr"]
    favourite_list_str=UD.get_favorite_list(uid)
    favourite_list=json.loads(favourite_list_str)
    uid=hash(uid)
    macAddr=hash(macAddr)
    permissionLink="/give_permission?macAddr={macAddr}&request_user_id={uid}"
    send_email(email,"owner's email","user's password", message+"please click the link to give permission:"+permissionLink)
    for y in favourite_list.values():   #I don't know what the list look like, so I pretend the value in the dictionary has the storeInfo 
        if y["macAddr"] == macAddr:
            y["permission"] == True
        
    toStr=str(favourite_list)
    UD.set_favorite_list(uid, toStr)


def send_email(from,to,message,password):
    server=smtplib.SMTP_SSL("smtp.gmail.com",465)
    server.login(from,password)
    server.sendmail(from,to,message)
    server.quit()


