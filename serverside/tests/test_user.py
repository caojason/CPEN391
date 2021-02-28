import sys
import os
sys.path.append(os.getcwd())

import json

from modules.database.init_database import init_torch_database
from pytest_mysql import factories

mysql_my_proc = factories.mysql_proc(port=3306)
mysql_my = factories.mysql("mysql_my_proc")

init_torch_database()

from app import app
app.testing = True

def default_test():
    with app.test_client() as testing_client:
        rv=testing_client.get("/")
        assert b"Torch" in rv.data

def test_create_user():
    with app.test_client() as testing_client:
        rv = testing_client.post("/create_user",
                    data=json.dumps({"uid":"105960354998423944600","type":"user_info","data":"yuntaowu2000@gmail.com"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/email?uid=105960354998423944600")
        assert b"yuntaowu2000@gmail.com" in rv.data

def test_invalid_create_user():
    # GET should fail
    with app.test_client() as testing_client:
        rv = testing_client.get("/create_user")
        assert rv.status_code != 200

def test_favorite_list():
    with app.test_client() as testing_client:
        # first setup user
        rv = testing_client.post("/create_user",
                    data=json.dumps({"uid":"105960354998423944600","type":"user_info","data":"yuntaowu2000@gmail.com"}),
                    content_type="application/json")
        assert rv.status_code == 200

        #send a random favorite list
        rv = testing_client.post("/favorite_list",
                    data=json.dumps({"uid":"105960354998423944600","type":"Favorites","data":"[]"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/favorite_list?uid=105960354998423944600")
        assert b"[]" in rv.data


