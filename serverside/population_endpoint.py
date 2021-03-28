import os
import sys
import base64
sys.path.append(os.getcwd())
import modules.person_counter_opencv.people_counter as PC
import modules.database.population_database as PD
import modules.person_counter_opencv.face_detector as FD 
import modules.database.mask_database as MD 
from modules.person_counter_opencv.video_conversion import convert_frames_to_video, decompression

from flask import Flask, request, jsonify 

from app import app


@app.route('/get_population_data/week')
def get_week(): 

    location = request.args["location"]
    report = PD.get_location_data_weekly(location)
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

@app.route('/get_population_analysis')
def get_population_analysis():
    location = request.args["location"]
    year = request.args["year"]

    try:
        highest_weekday, highest_hour, highest_average, lowest_weekday, lowest_hour, lowest_average = PD.get_location_analysis(location, year)
    except:
        return "no data"

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

LOCATION_IMAGES_MAP = {}
DEFAULT_FILE_PATH = "image"

#POST video feed 
@app.route('/upload_video', methods = ['POST'])
def upload_video(): 
    global LOCATION_IMAGES_MAP
    global DEFAULT_FILE_PATH
    #should be a byte stream here instead of a file
    #wait for the hardware part to be done first
    data_json = request.get_json()
    macAddr = data_json["location"]
    img_bytes = data_json["data"]
    store_path = macAddr.replace(":", "_")

    if not macAddr in LOCATION_IMAGES_MAP.keys():
        file_name = "00.png"
        LOCATION_IMAGES_MAP[macAddr] = [file_name]
    elif len(LOCATION_IMAGES_MAP[macAddr]) < 10:
        file_name = "0{0}.png".format(len(LOCATION_IMAGES_MAP[macAddr]))
        LOCATION_IMAGES_MAP[macAddr].append(file_name)
    else:
        file_name = "{0}.png".format(len(LOCATION_IMAGES_MAP[macAddr]))
        LOCATION_IMAGES_MAP[macAddr].append(file_name)
    
    folder_path = os.path.join("~/", DEFAULT_FILE_PATH, store_path)
    if not os.path.exists(folder_path):
        os.umask(0)
        os.makedirs(folder_path, 0o777)

    compressed_file_path = os.path.join(folder_path, file_name.replace("png", "txt"))
    image_file_path = os.path.join(folder_path, file_name)

    with open(compressed_file_path, "wb+") as f:
        f.write(base64.b64decode(img_bytes))
        f.flush()
   
    decompression(compressed_file_path, image_file_path)
    print(os.path.getsize(image_file_path))
    convert_frames_to_video(folder_path, folder_path + "/output.mp4", 1)

    #used for people counter
    # #get the people count array 
    # count = PC.people_counter()

    # masks = FD.facemask_detector()

    # #insert count as a new tuple inside the SQL database
    # PD.insert_table_population(str(macAddr), int(count))
    # MD.insert_table_mask(str(macAddr), int(count))

    # #after completing analysis, delete the file to save disk space
    # os.remove(image_file_path + "output.mp4")

    return "image received"
        


@app.route("/get_image_analysis", methods=["GET"])
def get_image():
    # get the actual path to image using mac address as file identifier
    global DEFAULT_FILE_PATH

    mac_addr = request.args["macAddr"]
    store_path = mac_addr.replace(":", "_")
    path_to_image = os.path.join("~/", DEFAULT_FILE_PATH, store_path, "output.jpg")
    if not os.path.exists(path_to_image):
        return ""
    print(path_to_image)
    encoded_image = ""
    with open(path_to_image, "rb+") as img_file:
        encoded_image = base64.b64encode(img_file.read())
    return encoded_image


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
