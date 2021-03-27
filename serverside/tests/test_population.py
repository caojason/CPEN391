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

