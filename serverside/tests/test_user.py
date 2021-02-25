import sys
import os

sys.path.append(os.getcwd())

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
                    data={"{uid:105960354998423944600,type:user_info,data:yuntaowu2000@gmail.com}": ""})
        assert rv.status_code == 200
        rv=testing_client.get("/email?uid=105960354998423944600")
        assert b"yuntaowu2000@gmail.com" in rv.data
