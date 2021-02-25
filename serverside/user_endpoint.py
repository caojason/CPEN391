import os
import sys
sys.path.append(os.getcwd())
import modules.database.user_database as UD

from app import app

from flask import Flask, request, jsonify
import json

def parse_data(form):
    value = form.to_dict()
    value = list(value.keys())[0]
    return json.loads(value)

@app.route("/create_user", methods = ["POST"])
def create_user():
    user_info_dict = parse_data(request.form)
    if user_info_dict["type"] == "user_info":
        uid = user_info_dict["uid"]
        email = user_info_dict["data"]
        UD.insert_table_user(uid, email)
        print("create user success")
        return "success"
    else:
        print("malformed type")
        return "failed"


@app.route("/favorite_list", methods = ["GET", "POST"])
def get_favorite_list():
    if request.method == "GET":
        uid = request.args["uid"]
        favorite_list = UD.get_favorite_list(uid)
        return jsonify(favorite_list)

    elif request.method == "POST":
        fav_lis_data = parse_data(request.form)
        uid = fav_lis_data["uid"]
        favorite_list = str(fav_lis_data["data"])
        UD.set_favorite_list(uid, favorite_list)
        return "success"

@app.route("/email", methods=["GET"])
def get_email():
    uid = request.args["uid"]
    return jsonify(UD.get_email(uid))
