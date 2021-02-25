import mysql.connector 
import datetime 
import calendar

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
    sql = "INSERT INTO population_data (location, count, year, month, day, hour, minute, weekday) VALUES (%s, %d, %d, %d, %d, %d, %d, %d)"
    val = (location, count, now.year, now.month, now.day, now.hour, now.minute, weekday)

    cursor.execute(sql, val)
    db.commit()

def get_location_data_hourly(location, year, month, day, hour):
    db = connect_to_database()
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND hour = {} AND day = {} AND month = {} AND year = {}".format(location, hour, day, month, year)
    cursor.execute(sql)
    result = cursor.fetchall()
    report = [0] * 60
    for row in result:
        minute = row["minute"]
        count = row["count"]
        report[minute - 1] += count 
    return report 

def get_location_data_daily(location, year, month, day): 
    db = connect_to_database()
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day, month, year)
    cursor.execute(sql)
    result = cursor.fetchall()
    report = [0] * 24 
    for row in result: 
        hour = row["hour"]
        count = row["count"]
        report[hour - 1] += count 
    return report

def get_location_data_weekly(location, year, month, day, weekday):
    db = connect_to_database()

    cursor = db.cursor()
    report = [0] * 7 
    #first find the days before the current weekday. If today is wednesday, we search monday and tuesday. 
    for i in range(weekday):
        day_in_week = day - i 
        #after subtracting the day may either be in the current month or a previous month. 
        if day_in_week < 0: 
            if month > 1: 
                day_in_week = calendar.monthrange(year, month - 1)[1] + day_in_week
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} year = {}".format(location, day_in_week, month - 1, year)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row["weekday"]
                    count = row["count"]
                    report[weekday - 1] += count
            else:
                day_in_week = calendar.monthrange(year - 1, 12)[1] + day_in_week
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} year = {}".format(location, day_in_week, 12, year)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row["weekday"]
                    count = row["count"]
                    report[weekday - 1] += count 
        else:
            sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} year = {}".format(location, day_in_week, month, year)
            cursor.execute(sql)
            result = cursor.fetchall()
            for row in result:
                weekday = row["weekday"]
                count = row["count"]
                report[weekday - 1] += count 
    #then find the days proceeding the current weekday. If today is friday we seatch saturday and sunday. 
    for i in range(1, 8 - weekday): 
        day_in_week = day + i
        #after addition the day may either be in the current month or in the next month. 
        if day_in_week > calendar.monthrange(year, month)[1]: 
            if month < 12:
                day_in_week -= calendar.monthrange(year, month)[1]
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} year = {}".format(location, day_in_week, month + 1, year)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row["weekday"]
                    count = row["count"]
                    report[weekday - 1] += count 
            else:
                day_in_week -= calendar.monthrange(year, month)[1]
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} year = {}".format(location, day_in_week, 1, year + 1)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row["weekday"]
                    count = row["count"]
                    report[weekday - 1] += count 
        else:
            sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} year = {}".format(location, day_in_week, month, year)
            cursor.execute(sql)
            result = cursor.fetchall()
            for row in result:
                weekday = row["weekday"]
                count = row["count"]
                report[weekday - 1] += count  
    return report 

def get_location_data_monthly(location, year, month):
    db = mysql.connector.connect(
        host="localhost",
        user="admin", 
        password="torch",
        database="torch"
    )
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND month = {} AND year = {}".format(location, month, year)  
    cursor.execute(sql)
    result = cursor.fetchall()
    report = [0] * 31 
    for row in result: 
        day = row["day"]
        count = row["count"]
        report[day - 1] += count 
    #days in month vary. calendar.monthrange to check days in month (leapyear included). pop execess elements. 
    if calendar.monthrange(year, month)[1] == 28:
        return report[:-3]
    elif calendar.monthrange(year, month)[1] == 29:
        return report[:-2] 
    elif calendar.monthrange(year, month)[1] == 30: 
        return report[:-1]
    else: 
        return report

def get_location_data_yearly(location, year):
    db = connect_to_database()
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND year = {}".format(location, year)
    cursor.execute(sql)
    result = cursor.fetchall()
    report = [0] * 12 
    for row in result: 
        month = row["month"]
        count = row["count"]
        report[month - 1] += count 
    return report 


