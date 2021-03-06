import base64
import os
import sys
import json
import smtplib
sys.path.append(os.getcwd())
import modules.database.store_info_database as SD
import modules.database.user_database as UD
from app import app

from flask import request, jsonify
import json

@app.route("/create_store", methods=["POST"])
def create_store():
    data_json = request.get_json()
    print("create store read data: {0}".format(data_json))
    try:
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


@app.route("/check_exists", methods=["GET"])
def check_exist():
    macAddr = request.args["macAddr"]
    return jsonify(SD.check_if_exist(macAddr))


@app.route("/create_email",methods=["POST"])
def create_permission_link():
    data_json = request.get_json()
    print("create permission link data read {0}".format(data_json))
    data=json.loads(data_json["data"])

    subject = data["subject"]
    message = data["message"]
    ownerId = data["ownerId"]
    macAddr = data["macAddr"]
    email=data["email"]
    owner_email = UD.get_email(ownerId)
    uid=data_json["uid"]
    uid=StevenHash(int(uid))
    print("hashed uid {0}".format(uid))
    permissionLink="http://35.233.184.107:5000/give_permission?macAddr={}&uid={}".format(macAddr,uid)
    msg="Subject: \n"+subject+"\n\nUser {}".format(email)+" send you a request for viewing your store's analytic data. Here is his message: \n" + message+"\n\n Click the following link to give permission: "+permissionLink
    send_email(owner_email,msg)
    return "success"

@app.route("/give_permission",methods=["GET"])
def get_permission():
    uid = request.args["uid"] if "uid" in request.args else "\"\""
    uid=StevenUnHash(uid)
    print("unhashed uid: {0}".format(uid))
    macAddr=request.args["macAddr"] if "macAddr" in request.args else "\"\""
    favourite_list_str=UD.get_favorite_list(uid)
    if favourite_list_str != "":
        favourite_list_str=favourite_list_str[0]
    print("original {0}".format(favourite_list_str))
    indexOfAddr=favourite_list_str.find(macAddr)
    if indexOfAddr != -1:
        indexOfPermission=favourite_list_str.rfind("false",0,indexOfAddr)
        if indexOfPermission != -1:
            firstPart=favourite_list_str[0:indexOfPermission]
            secondPart=favourite_list_str[indexOfPermission:]
            secondPart=secondPart.replace("false","true",1)
            favourite_list_str=firstPart + secondPart
    
    print(favourite_list_str)
    UD.set_favorite_list(uid, favourite_list_str)

    return "You have successfully given permission"


def send_email(receiver, message):

    fromaddr = 'torchapp1@gmail.com'
    toaddrs  = receiver
    username = 'torchapp1@gmail.com'
    password = 'ko89010430'
    server = smtplib.SMTP('smtp.gmail.com:587')
    server.ehlo()
    server.starttls()
    server.login(username,password)
    server.sendmail(fromaddr, toaddrs, message)
    server.quit()

def StevenHash(number):
    
    return bin(number>>2)

def StevenUnHash(encoded):
    return int(encoded,2)<<2
