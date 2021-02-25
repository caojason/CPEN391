import os
import sys
sys.path.append(os.getcwd())
import modules.person_counter_opencv.people_counter as PC #may change depending on directory structure
import modules.database.population_database as PD #may change depending on directory structure

from flask import Flask, request, jsonify 
 
import calendar

from app import app


@app.route('/get_population_data/week')
def get_week(): 
    day = request.args["day"]
    month = request.args["month"]
    year = request.args["year"]
    weekday = request.args["weekday"]
    location = request.args["location"]
    report = PD.get_location_data_weekly(location, year, month, day, weekday)
    WEEKDAY_NAMES = [
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    ]
    histogram = dict(zip(WEEKDAY_NAMES, report))
    return jsonify(histogram) 

@app.route('/get_population_data/year')
def get_year():
    year = request.args["year"]
    location = request.args["location"]
    report = PD.get_location_data_yearly(location, year)
    MONTH_NAMES = [ 
        "January", "Feburary","March", "April", "May", "June", "July", "August", "September",
        "October", "November", "December"
    ]

    histogram = dict(zip(MONTH_NAMES, report))
    return jsonify(histogram)

@app.route('/get_population_data/month')
def get_month():
    month = request.args["month"]
    year = request.args["year"]
    location = request.args["location"]
    num_days = calendar.monthrange(year, month)[1]
    dict_key = []
    for i in range(1, num_days + 1):
        dict_key.append(str(i))
    report = PD.get_location_data_monthly(location, year, month)
    histogram = dict(zip(dict_key, report))
    return jsonify(histogram)

@app.route('/get_population_data/day')
def get_day(): 
    month = request.args["month"]
    year = request.args["year"]
    location = request.args["location"]
    day = request.args["day"]
    dict_key = []
    for i in range(1, 25):
        dict_key.append(str(i))
    report = PD.get_location_data_daily(location, year, month, day)
    histogram = dict(zip(dict_key, report))
    return jsonify(histogram)

@app.route('/get_population_data/hour')
def get_hour(): 
    day = request.args["day"]
    month = request.args["month"]
    year = request.args["year"]
    location = request.args["location"]
    hour = request.args["hour"]

    report = PD.get_location_data_hourly(location, year, month, day, hour)
    dict_key = []
    for i in range(1, 61):
        dict_key.append(str(i))
    histogram = dict(zip(dict_key, report))
    return jsonify(histogram)

#POST video feed 
@app.route('/upload_video', methods = ['GET', 'POST'])
def upload_video(): 
    if request.method == 'POST' : 

        #save the video section to the server
        f = request.files['file']
        f.save('/video/interval.mp4')
        
        #get location from http request
        location = request.form['location']

        #get the people count array 
        count = PC.people_counter()

        #insert count as a new tuple inside the SQL database
        PD.insert_table_population(str(location), int(count))

        #after completing analysis, delete the file to save disk space
        os.remove('/video/interval.mp4')

        return "video recieved and analyzed"






