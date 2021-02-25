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

def test_create_store():
    with app.test_client() as testing_client:
        rv = testing_client.post("/create_store",
                    data=json.dumps({"encodedLogo":"","hasPermission":True,"latitude":49.2311,"longitude":-123.0082,"macAddr":"20:17:01:09:52:98","storeName":"test","storeOwnerId":"105960354998423944600"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_stores")
        print(rv.data)
        assert rv.status_code == 200

def test_create_store_with_malformed_info():
    with app.test_client() as testing_client:
        rv = testing_client.post("/create_store",
                    data=json.dumps({"encodedLogo":""}),
                    content_type="application/json")
        assert b"failed" in rv.data

def test_update_store_info():
    with app.test_client() as testing_client:
        # firstly create the store
        rv = testing_client.post("/create_store",
                    data=json.dumps({"encodedLogo":"","hasPermission":True,"latitude":49.2311,"longitude":-123.0082,"macAddr":"20:17:01:09:52:98","storeName":"test","storeOwnerId":"105960354998423944600"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_stores")
        assert rv.status_code == 200
        data1 = rv.data
        
        # now update the value
        rv = testing_client.post("/create_store",
                    data=json.dumps({"encodedLogo":"","hasPermission":True,"latitude":49.2311,"longitude":-123.0082,"macAddr":"20:17:01:09:52:98","storeName":"testUpdate","storeOwnerId":"105960354998423944600"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_stores")
        assert rv.status_code == 200
        data2 = rv.data
        assert data1 != data2

