import mysql.connector 
import datetime 
import pytz
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
    
    now = datetime.datetime.now(pytz.timezone('US/Pacific'))

    db = connect_to_database()

    cursor = db.cursor()
    sql = "INSERT INTO population_data (location, count, year, month, day, hour, minute, weekday) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    val = (location, count, now.year, now.month, now.day, now.hour, now.minute, now.weekday())

    cursor.execute(sql, val)
    db.commit()

def update_report(cursor, sql, report, weekday):
    cursor.execute(sql)
    result = cursor.fetchall()
    if result is None:
        return
    else:
        sum_and_count = numpy.zeros((24, 2), dtype=numpy.int)
        for row in result:
            human_count = row[2]
            hour = row[6]
            sum_and_count[hour][0] += human_count
            sum_and_count[hour][1] += 1
        for i in range(24):
            if sum_and_count[i][1] != 0:
                report[weekday][i] = int(sum_and_count[i][0] / sum_and_count[i][1])


def get_location_data_weekly(location):

    current_time = datetime.datetime.now(pytz.timezone('US/Pacific'))
    print(current_time)
    
    month = current_time.month
    year = current_time.year

    db = connect_to_database()
    cursor = db.cursor()
    report = numpy.zeros((7, 24), dtype=numpy.int)
    
    for weekday in range(7):
        sql = "SELECT * FROM population_data WHERE location = '{}' AND month = {} AND year = {} AND weekday = {}".format(location, month, year, weekday)
        update_report(cursor, sql, report, weekday)
    
    report = report.tolist()
    return report 

#get the hour & weekday with the lowest and highest population peak in the year
def get_location_analysis(location):
    year = datetime.datetime.now(pytz.timezone('US/Pacific')).year
    
    db = connect_to_database()
    cursor = db.cursor()
    sql = "SELECT * FROM population_data WHERE location = '{}' AND year = {}".format(location, int(year))
    cursor.execute(sql)
    result = cursor.fetchall()

    if result is None:
        return "no data"

    week_average = [0.0] * 7
    unique_weekdays = [[] for _ in range(7)]
    
    for row in result: 
        weekday = row[8]
        count = row[2]
        day = row[5]
        month = row[4]
        #generate a date so we can count how many unique weekdays has passed. 
        date = "{}/{}".format(day, month)  
        if date not in unique_weekdays[weekday]:
            unique_weekdays[weekday].append(date)
        #sum up the total in the array element for the weekday 
        week_average[weekday] += count 

    #divide weekday totals by the number of that weekday for the average. 
    for i in range(7):
        if len(unique_weekdays[i]) > 0:
            week_average[i] /= len(unique_weekdays[i])

    highest_average = max(week_average)
    #102
    highest_weekday = week_average.index(highest_average) + 1 
    lowest_average = min(week_average)
    lowest_weekday = week_average.index(lowest_average) + 1

    if highest_average == 0.0 and lowest_average == 0.0 and highest_weekday == lowest_weekday:
        return "no data"
    elif lowest_weekday == highest_weekday:
        print("error all weekdays equally populated")

    daily_high = [0.0] * 24 
    daily_low = [0.0] * 24

    for row in result: 
        if row[8] == highest_weekday: 
            daily_high[row[6]] += row[2]     
        elif row[8] == lowest_weekday: 
            daily_low[row[6]] += row[2]
    
    highest_hour = daily_high.index(max(daily_high))
    lowest_hour = daily_low.index(min(daily_low))

    return (highest_weekday, highest_hour, highest_average, lowest_weekday, lowest_hour, lowest_average) 

# def get_location_data_hourly(location, year, month, day, hour):
#     db = connect_to_database()
#     cursor = db.cursor()
#     sql = "SELECT * FROM population_data WHERE location = '{}' AND hour = {} AND day = {} AND month = {} AND year = {}".format(location, hour, day, month, year)
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
#     sql = "SELECT * FROM population_data WHERE location = '{}' AND day = {} AND month = {} AND year = {}".format(location, day, month, year)
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
#     sql = "SELECT * FROM population_data WHERE location = '{}' AND month = {} AND year = {}".format(location, month, year)  
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
#     sql = "SELECT * FROM population_data WHERE location = '{}' AND year = {}".format(location, year)
#     cursor.execute(sql)
#     result = cursor.fetchall()
#     report = [0] * 12 
#     for row in result: 
#         month = row[3]
#         count = row[1]
#         report[month - 1] += count 
#     return report 
