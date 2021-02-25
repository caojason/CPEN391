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
        rv = testing_client.post("/create_store",
                    data=json.dumps({"encodedLogo":"","hasPermission":True,"latitude":49.2311,"longitude":-123.0082,"macAddr":"20:17:01:09:52:98","storeName":"test","storeOwnerId":"105960354998423944600"}),
                    content_type="application/json")
        assert rv.status_code == 200