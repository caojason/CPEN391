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
    sql = "INSERT INTO population_data (location, count, year, month, day, hour, minute, weekday) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
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
        minute = row[6]
        count = row[1]
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
        hour = row[5]
        count = row[1]
        report[hour - 1] += count 
    return report

def get_location_data_weekly(location, year, month, day, weekday):
    db = connect_to_database()

    cursor = db.cursor()
    report = [0] * 7 
    #first find the days before the current weekday. If today is wednesday, we search monday and tuesday. 
    d = weekday
    for i in range(d):
        day_in_week = day - i 
        #after subtracting the day may either be in the current month or a previous month. 
        if day_in_week < 0: 
            if month > 1: 
                day_in_week = calendar.monthrange(year, month - 1)[1] + day_in_week
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day_in_week, month - 1, year)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row[7]
                    count = row[1]
                    report[weekday - 1] += count
            else:
                day_in_week = calendar.monthrange(year - 1, 12)[1] + day_in_week
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day_in_week, 12, year)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row[7]
                    count = row[1]
                    report[weekday - 1] += count 
        else:
            sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day_in_week, month, year)
            cursor.execute(sql)
            result = cursor.fetchall()
            for row in result:
                weekday = row[7]
                count = row[1]
                report[weekday - 1] += count 
    #then find the days proceeding the current weekday. If today is friday we seatch saturday and sunday. 
    for i in range(1, 8 - d): 
        day_in_week = day + i
        #after addition the day may either be in the current month or in the next month. 
        if day_in_week > calendar.monthrange(year, month)[1]: 
            if month < 12:
                day_in_week -= calendar.monthrange(year, month)[1]
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day_in_week, month + 1, year)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row[7]
                    count = row[1]
                    report[weekday - 1] += count 
            else:
                day_in_week -= calendar.monthrange(year, month)[1]
                sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day_in_week, 1, year + 1)
                cursor.execute(sql)
                result = cursor.fetchall()
                for row in result:
                    weekday = row[7]
                    count = row[1]
                    report[weekday - 1] += count 
        else:
            sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day_in_week, month, year)
            cursor.execute(sql)
            result = cursor.fetchall()
            for row in result:
                weekday = row[7]
                count = row[1]
                if weekday < 1: 
                    continue
                report[weekday - 1] += count  
    return report 

def get_location_data_monthly(location, year, month):
    db = connect_to_database()
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND month = {} AND year = {}".format(location, month, year)  
    cursor.execute(sql)
    result = cursor.fetchall()
    report = [0] * 31 
    for row in result: 
        day = row[4]
        count = row[1]
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
        month = row[3]
        count = row[1]
        report[month - 1] += count 
    return report 

#get the hour & weekday with the lowest and highest population peak in the year
def get_location_analysis(location, year):
    db = connect_to_database()
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND year = {}".format(location, year)
    cursor.execute(sql)
    result = cursor.fetchall()

    week_average = [0.0] * 7
    unique_weekdays = [[] for i in range(7)]
    
    for row in result: 
        weekday = row[7]
        count = row[1]
        day = row[4]
        month = row[3]
        #generate a date so we can count how many unique weekdays has passed. 
        date = "{}/{}".format(day, month)  
        if date not in unique_weekdays[weekday - 1]:
            unique_weekdays[weekday - 1].append(date)
        #sum up the total in the array element for the weekday 
        week_average[weekday - 1] += count 

    #divide weekday totals by the number of that weekday for the average. 
    for i in range(7):
        week_average[i] /= len(unique_weekdays[i])

    highest_average = max(week_average)
    highest_weekday = week_average.index(highest_average)

    lowest_average = min(week_average)
    lowest_weekday = week_average.index(lowest_average)

    daily_high = [0.0] * 24 
    daily_low = [0.0] * 24

    for row in result: 
        if row[7] == highest_weekday: 
            daily_high[row[5]] += row[1]
            
        if row[7] == lowest_weekday: 
            daily_low[row[5]] += row[1]
    
    highest_hour = daily_high.index(max(daily_high))
    lowest_hour = daily_low.index(min(daily_low))

    return (highest_weekday, highest_hour, highest_average, lowest_weekday, lowest_hour, lowest_average) 