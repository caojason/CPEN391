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

#insert tuple into user table. 
def insert_table_user(uid, email):
    

    db = connect_to_database()

    cursor = db.cursor()
    #check if the user already exists
    sql = "SELECT * FROM user_data WHERE user_gid={}".format(uid)
    cursor.execute(sql)

    if not cursor.fetchone():
        #insert only if the user does not exist
        sql = "INSERT INTO user_data (user_gid, email, favorite_list) VALUES (%s, %s, %s)"
        val = (uid, email, "")

        cursor.execute(sql, val)
        db.commit()

def get_email(uid):
    db = connect_to_database()

    cursor = db.cursor()
    sql = "SELECT email FROM user_data WHERE user_gid = {}".format(uid)
    cursor.execute(sql)

    result = cursor.fetchone()

    return result

def get_favorite_list(uid):
    db = connect_to_database()

    cursor = db.cursor()
    sql = "SELECT favorite_list FROM user_data WHERE user_gid = {}".format(uid)
    cursor.execute(sql)

    result = cursor.fetchone()

    if result is None:
        result = ""
    return result

def set_favorite_list(uid, favorite_list):
    db = connect_to_database()

    cursor = db.cursor()
    sql = "UPDATE user_data SET favorite_list=%s WHERE user_gid=%s"
    data = (favorite_list, uid)
    cursor.execute(sql, data)
    db.commit()