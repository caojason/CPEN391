import os
import sys
import base64
sys.path.append(os.getcwd())
import modules.person_counter_opencv.people_counter as PC #may change depending on directory structure
import modules.database.population_database as PD #may change depending on directory structure
import modules.person_counter_opencv.face_detector as FD #may change depending on directory structure
import modules.database.mask_database as MD 

from flask import Flask, request, jsonify 
 
import calendar

from app import app


@app.route('/get_population_data/week')
def get_week(): 
    day = int(request.args["day"])
    month = int(request.args["month"])
    year = int(request.args["year"])
    weekday = int(request.args["weekday"])
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

# currently we don't need those functions
# @app.route('/get_population_data/year')
# def get_year():
#     year = request.args["year"]
#     location = request.args["location"]
#     report = PD.get_location_data_yearly(location, year)
#     MONTH_NAMES = [ 
#         "January", "Feburary","March", "April", "May", "June", "July", "August", "September",
#         "October", "November", "December"
#     ]

#     histogram = dict(zip(MONTH_NAMES, report))
#     return jsonify(histogram)
# 
# @app.route('/get_population_data/month')
# def get_month():
#     month = request.args["month"]
#     year = request.args["year"]
#     location = request.args["location"]
#     num_days = calendar.monthrange(year, month)[1]
#     dict_key = []
#     for i in range(1, num_days + 1):
#         dict_key.append(str(i))
#     report = PD.get_location_data_monthly(location, year, month)
#     histogram = dict(zip(dict_key, report))
#     return jsonify(histogram)

# @app.route('/get_population_data/day')
# def get_day(): 
#     month = request.args["month"]
#     year = request.args["year"]
#     location = request.args["location"]
#     day = request.args["day"]
#     dict_key = []
#     for i in range(1, 25):
#         dict_key.append(str(i))
#     report = PD.get_location_data_daily(location, year, month, day)
#     histogram = dict(zip(dict_key, report))
#     return jsonify(histogram)

# @app.route('/get_population_data/hour')
# def get_hour(): 
#     day = request.args["day"]
#     month = request.args["month"]
#     year = request.args["year"]
#     location = request.args["location"]
#     hour = request.args["hour"]

#     report = PD.get_location_data_hourly(location, year, month, day, hour)
#     dict_key = []
#     for i in range(1, 61):
#         dict_key.append(str(i))
#     histogram = dict(zip(dict_key, report))
#     return jsonify(histogram)

@app.route('/get_population_analysis')
def get_population_analysis():
    location = request.args["location"]
    year = request.args["year"]

    highest_weekday, highest_hour, highest_average, lowest_weekday, lowest_hour, lowest_average = PD.get_location_analysis(location, year)
    report = {
        "highest weekday" : highest_weekday,
        "highest hour" : highest_hour,
        "highest average" : highest_average, 
        "lowest weekday" : lowest_weekday, 
        "lowest hour" : lowest_hour, 
        "lowest average" : lowest_average
    }
    #returns the highest and lowest average ie Monday at 2 pm is the lowest. Average refers to average visitors on a monday 
    return jsonify(report)

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

        masks = FD.facemask_detector()

        #insert count as a new tuple inside the SQL database
        PD.insert_table_population(str(location), int(count))
        MD.insert_table_mask(str(location), int(count))

        #after completing analysis, delete the file to save disk space
        os.remove('/video/interval.mp4')

        return "video recieved and analyzed"


@app.route("/get_image_analysis", methods=["GET"])
def get_image():
    # get the actual path to image using mac address as file identifier
    mac_addr = request.args["macAddr"]
    path_to_image = os.path.join(os.getcwd(), "..", "Hardware", "imageProcessing", "3x3.png")
    print(path_to_image)
    encoded_image = ""
    with open(path_to_image, "rb+") as img_file:
        encoded_image = base64.b64encode(img_file.read())
    return encoded_image


