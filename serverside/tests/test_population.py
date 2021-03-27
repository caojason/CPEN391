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

import modules.database.population_database as PD

def test_get_weekly_data():
    PD.insert_table_population("FF:FF:FF:FF:FF:FF", 100)

    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?day=26&month=3&year=2021&weekday=5&location=FF:FF:FF:FF:FF:FF")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"100" in rv.data

def test_get_weekly_data_1():
    PD.insert_table_population("A", 100)

    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?day=26&month=3&year=2021&weekday=5&location=FF:FF:FF:FF:FF:FF")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"0" in rv.data

def test_get_weekly_data_2():
    PD.insert_table_population("B", 100)

    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?day=26&month=3&year=2021&weekday=5&location=B")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"0" in rv.data  
    
    PD.insert_table_population("B", 2)
    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_data/week?day=26&month=3&year=2021&weekday=5&location=B")
        assert rv.status_code == 200
        print("get population data {0}".format(rv.data))
        assert b"102" in rv.data  

def test_get_analysis_no_data():
    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_analysis?year=2021&location=B")
        assert rv.status_code == 200
        assert b"no data" in rv.data  

def test_get_analysis():
    PD.insert_table_population("A", 100)
    with app.test_client() as testing_client:
        rv = testing_client.get("/get_population_analysis?year=2021&location=A")
        assert rv.status_code == 200
        # assert b"highest" in rv.data  
