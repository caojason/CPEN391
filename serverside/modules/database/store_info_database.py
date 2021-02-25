from flask.json import jsonify
import mysql.connector 

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

def insert_table_store_info(encodedLogo, latitude, longitude, macAddr, storeName, storeOwnerId):
    
    if encodedLogo == "":
        encodedLogo = " " 

    db = connect_to_database()
    # check if the store (unique mac address) exists
    cursor = db.cursor()
    sql = "SELECT * FROM store_info_data WHERE macAddr={}".format("\""+macAddr+"\"")
    cursor.execute(sql)

    if not cursor.fetchone():
        # insert
        sql = "INSERT INTO store_info_data (encodedLogo, latitude, longitude, macAddr, storeName, storeOwnerId) VALUES (%s, %s, %s, %s, %s, %s)"
        val = (encodedLogo, latitude, longitude, macAddr, storeName, storeOwnerId)
        cursor.execute(sql, val)
        db.commit()
    else:
        # update the existing value
        sql = "UPDATE store_info_data SET encodedLogo=%s, latitude=%s, longitude=%s, storeName=%s, storeOwnerId=%s WHERE macAddr=%s"
        val = (encodedLogo, latitude, longitude, storeName, storeOwnerId, macAddr)
        cursor.execute(sql, val)
        db.commit()

def get_store_info_records(uid):
    # get the store info whose owner is not the query owner
    # the returned store info should not have location as -1 -1 (set as private)
    
    db = connect_to_database()
    # check if the store (unique mac address) exists
    cursor = db.cursor()
    sql = "SELECT * FROM store_info_data WHERE (storeOwnerId<>{} AND NOT(latitude={} AND longitude={})) LIMIT {};".format(uid, -1, -1, 10)
    cursor.execute(sql)

    all_stores = cursor.fetchall()

    return all_stores