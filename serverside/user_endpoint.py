import os
import sys
sys.path.append(os.getcwd())
import modules.database.user_database as UD

from app import app

from flask import request, jsonify

@app.route("/create_user", methods = ["POST"])
def create_user():
    user_info_dict = request.get_json()
    try:
        uid = user_info_dict["uid"]
        email = user_info_dict["data"]
        UD.insert_table_user(uid, email)
        print("create user success")
        return "success"
    except:
        return "failed"


@app.route("/favorite_list", methods = ["GET", "POST"])
def get_favorite_list():
    if request.method == "GET":
        uid = request.args["uid"]
        favorite_list = UD.get_favorite_list(uid)
        return str(favorite_list[0]) if favorite_list != "" else ""

    elif request.method == "POST":
        fav_list_data = request.get_json()
        uid = fav_list_data["uid"]
        favorite_list = str(fav_list_data["data"])
        UD.set_favorite_list(uid, favorite_list)
        return "success"

@app.route("/email", methods=["GET"])
def get_email():
    uid = request.args["uid"]
    return jsonify(UD.get_email(uid))
