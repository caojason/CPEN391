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
                    data=json.dumps({"uid":"105960354998423944600","data":"{\"encodedLogo\":\"\",\"hasPermission\":true,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"test1\",\"storeOwnerId\":\"105960354998423944600\"}"}),
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
                    data=json.dumps({"uid":"105960354998423944600","data":"{\"encodedLogo\":\"\",\"hasPermission\":true,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"test\",\"storeOwnerId\":\"105960354998423944600\"}"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_stores")
        assert rv.status_code == 200
        data1 = rv.data
        
        # now update the value
        rv = testing_client.post("/create_store",
                    data=json.dumps({"uid":"105960354998423944600","data":"{\"encodedLogo\":\"\",\"hasPermission\":true,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"testMyStore\",\"storeOwnerId\":\"105960354998423944600\"}"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_stores")
        assert rv.status_code == 200
        data2 = rv.data
        assert data1 != data2


def test_check_if_store_exist1():
    #this time the store does not exist yet
    with app.test_client() as testing_client:
        rv = testing_client.get("/check_exists?macAddr=00:00:00:00:00:00")
        assert rv.status_code == 200
        assert b"\"\"" in rv.data

def test_check_if_store_exist2():
    # this time create a store first
    with app.test_client() as testing_client:
        rv = testing_client.post("/create_store",
                    data=json.dumps({"uid":"105960354998423944600","data":"{\"encodedLogo\":\"\",\"hasPermission\":true,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"test\",\"storeOwnerId\":\"105960354998423944600\"}"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/check_exists?macAddr=20:17:01:09:52:98")
        assert rv.status_code == 200
        assert b"[\"105960354998423944600\"]" in rv.data

def test_create_permission_link():
    with app.test_client() as testing_client:
      #create a user 
        rv = testing_client.post("/create_user",
                data=json.dumps({"uid":"105960354998423944600","type":"user_info","data":"yuntaowu2000@gmail.com"}),
                content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/email?uid=105960354998423944600")
        assert b"yuntaowu2000@gmail.com" in rv.data
     
      #send a random favorite list
        rv = testing_client.post("/favorite_list",
                data=json.dumps({"uid":"105960354998423944600","type":"Favorites","data":"[{\"encodedLogo\":\"\",\"hasPermission\":false,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"testMyStore\",\"storeOwnerId\":\"105960354998423944600\"}]"}),
                content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/favorite_list?uid=105960354998423944600")
        data1=rv.data
        print(data1)
        #send a message
        rv = testing_client.get("/give_permission?uid=101101111100111111011001101011110000001001101011000011111011001100000&macAddr=20:17:01:09:52:98")
        assert rv.status_code == 200
        rv=testing_client.get("/favorite_list?uid=105960354998423944600")
        data2=rv.data
        print(data2)
        assert data1 !=data2
        
def test_create_permission_link_with_longerString():
    with app.test_client() as testing_client:
      #create a user 
        rv = testing_client.post("/create_user",
                data=json.dumps({"uid":"105960354998423944600","type":"user_info","data":"yuntaowu2000@gmail.com"}),
                content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/email?uid=105960354998423944600")
        assert b"yuntaowu2000@gmail.com" in rv.data
     
      #send a random favorite list
        rv = testing_client.post("/favorite_list",
                data=json.dumps({"uid":"105960354998423944600","type":"Favorites","data":"[{\"encodedLogo\":\"\",\"hasPermission\":false,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"testmy\",\"storeOwnerId\":\"105960354998423944600\"},{\"encodedLogo\":\" \",\"hasPermission\":false,\"latitude\":10.0,\"longitude\":12.0,\"macAddr\":\"FF:FF:FF:FF:FF:AB\",\"storeName\":\"test1\",\"storeOwnerId\":\"testid1\"}]"}),
                content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/favorite_list?uid=105960354998423944600")
        data1=rv.data
        print(data1)
        #send a message
        rv = testing_client.get("/give_permission?uid=101101111100111111011001101011110000001001101011000011111011001100000&macAddr=FF:FF:FF:FF:FF:AB")
        assert rv.status_code == 200
        rv=testing_client.get("/favorite_list?uid=105960354998423944600")
        data2=rv.data
        print(data2)
        assert data1 !=data2
 
def test_send_email():
        with app.test_client() as testing_client:
        #create user    
         rv = testing_client.post("/create_user",
                    data=json.dumps({"uid":"105960354998423944600","type":"user_info","data":"yuntaowu2000@gmail.com"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv=testing_client.get("/email?uid=105960354998423944600")
        assert b"yuntaowu2000@gmail.com" in rv.data
        #create store
         # firstly create the store
        rv = testing_client.post("/create_store",
                    data=json.dumps({"uid":"105960354998423944600","data":"{\"encodedLogo\":\"\",\"hasPermission\":true,\"latitude\":49.2311,\"longitude\":-123.0082,\"macAddr\":\"20:17:01:09:52:98\",\"storeName\":\"test\",\"storeOwnerId\":\"105960354998423944600\"}"}),
                    content_type="application/json")
        assert rv.status_code == 200
        rv = testing_client.get("/get_stores")
        assert rv.status_code == 200
     
        #send email 
        rv = testing_client.post("/create_email",
                data=json.dumps({"uid":"105960354998423944600","data":"{\"macAddr\":\"20:17:01:09:52:98\",\"ownerId\":\"105960354998423944600\",\"email\":\"yuntaowu2000@gmail.com\",\"subject\":\"Give me Permission\",\"message\":\"This is Steven Huang\"}"}),
                content_type="application/json")
        assert rv.status_code==200        
        
        