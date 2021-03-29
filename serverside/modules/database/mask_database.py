import mysql.connector 
import datetime 
import calendar
import numpy

def connect_to_database():
    try:
        return mysql.connector.connect(
            host="localhost",
            user="admin", 
            password="torch",
            database="torch"
        )
    except:
        return mysql.connector.connect(
            host="34.82.123.84",
            user="admin", 
            password="torch",
            database="torch"
        )

#insert tuple into population table. 
def insert_table_population(location, count):
    
    now = datetime.datetime.now()
    weekday = datetime.datetime.today().weekday()

    db = connect_to_database()

    cursor = db.cursor()
    sql = "INSERT INTO mask_data (location, count, year, month, day, hour, minute, weekday) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    val = (location, count, now.year, now.month, now.day, now.hour, now.minute, weekday)

    cursor.execute(sql, val)
    db.commit()

def update_report(cursor, sql, report, weekday):
    cursor.execute(sql)
    result = cursor.fetchall()
    if result is None:
        return
    else:
        sum_and_count = numpy.zeros((24, 2), dtype=numpy.int)
        known_days = []
        for row in result:
            human_count = row[2]
            hour = row[6]
            sum_and_count[hour][0] += human_count
            if not (row[5] in known_days):
                known_days.append(row[5])
                sum_and_count[hour][1] += 1
        for i in range(24):
            if sum_and_count[i][1] != 0:
                report[weekday][i] = sum_and_count[i][0] / sum_and_count[i][1]


def get_location_data_weekly(location):

    month = datetime.datetime.today().month
    year = datetime.datetime.today().year
    db = connect_to_database()
    cursor = db.cursor()
    report = numpy.zeros((7, 24), dtype=numpy.int)
    
    for weekday in range(7):
        sql = "SELECT * FROM mask_data WHERE location = '{}' AND month = {} AND year = {} AND weekday = {}".format(location, month, year, weekday)
        update_report(cursor, sql, report, weekday)
    
    report = report.tolist()
    return report 

# def get_location_data_hourly(location, year, month, day, hour):
#     db = connect_to_database()
#     cursor = db.cursor()
#     sql = "SELECT * FROM mask_data WHERE location = '{}' AND hour = {} AND day = {} AND month = {} AND year = {}".format(location, hour, day, month, year)
#     cursor.execute(sql)
#     result = cursor.fetchall()
#     report = [0] * 60
#     for row in result:
#         minute = row[6]
#         count = row[1]
#         report[minute - 1] += count 
#     return report 

# def get_location_data_daily(location, year, month, day): 
#     db = connect_to_database()
#     cursor = db.cursor()
#     sql = "SELECT * FROM mask_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day, month, year)
#     cursor.execute(sql)
#     result = cursor.fetchall()
#     report = [0] * 24 
#     for row in result: 
#         hour = row[5]
#         count = row[1]
#         report[hour - 1] += count 
#     return report

# def get_location_data_monthly(location, year, month):
#     db = connect_to_database()
#     cursor = db.cursor()
#     sql = "SELECT * FROM mask_data WHERE location = '{}' AND month = {} AND year = {}".format(location, month, year)  
#     cursor.execute(sql)
#     result = cursor.fetchall()
#     report = [0] * 31 
#     for row in result: 
#         day = row[4]
#         count = row[1]
#         report[day - 1] += count 
#     #days in month vary. calendar.monthrange to check days in month (leapyear included). pop execess elements. 
#     if calendar.monthrange(year, month)[1] == 28:
#         return report[:-3]
#     elif calendar.monthrange(year, month)[1] == 29:
#         return report[:-2] 
#     elif calendar.monthrange(year, month)[1] == 30: 
#         return report[:-1]
#     else: 
#         return report

# def get_location_data_yearly(location, year):
#     db = connect_to_database()
#     cursor = db.cursor()
#     sql = "SELECT * FROM mask_data WHERE location = '{}' AND year = {}".format(location, year)
#     cursor.execute(sql)
#     result = cursor.fetchall()
#     report = [0] * 12 
#     for row in result: 
#         month = row[3]
#         count = row[1]
#         report[month - 1] += count 
#     return report 