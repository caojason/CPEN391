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

