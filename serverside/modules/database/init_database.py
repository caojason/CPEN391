import mysql.connector 


# to examine data base, use the following commands
# show databases;
# use database_name;
# show tables;
# select * from table_name;

#create the database
def database_init():

    try:
        db = mysql.connector.connect(
        host="localhost",
        user="admin", 
        password="torch"
        )

        cursor = db.cursor()
        cursor.execute("CREATE DATABASE torch")
    except:
        print("database exists")

#create the table to store user info
def create_table_user(): 

    db = mysql.connector.connect(
        host="localhost",
        user="admin", 
        password="torch",
        database="torch"
    )

    cursor = db.cursor()
    cursor.execute(
        "CREATE TABLE IF NOT EXISTS `user_data` ( `id` INT AUTO_INCREMENT PRIMARY KEY, `user_gid` VARCHAR(255), `email` VARCHAR(255), `favorite_list` TEXT)"
    )

#create the table to store location visation records
def create_table_population(): 

    db = mysql.connector.connect(
        host="localhost",
        user="admin", 
        password="torch",
        database="torch"
    )

    cursor = db.cursor()
    cursor.execute(
        "CREATE TABLE IF NOT EXISTS `population_data` ( `id` INT AUTO_INCREMENT PRIMARY KEY, `location` VARCHAR(20), `count` INT, `year` INT, `month` INT, `day` INT, `hour` INT, `minute` INT, `weekday` INT)"
    )

def create_table_store_info():
    db = mysql.connector.connect(
        host="localhost",
        user="admin", 
        password="torch",
        database="torch"
    )

    cursor = db.cursor()
    cursor.execute(
        "CREATE TABLE IF NOT EXISTS `store_info_data` ( `id` INT AUTO_INCREMENT PRIMARY KEY, `storeName` VARCHAR(20), `storeOwnerId` VARCHAR(255), `latitude` DOUBLE, `longitude` DOUBLE, `macAddr` VARCHAR(255), `encodedLogo` TEXT)"
    )

def create_table_mask():
    db = mysql.connector.connect(
        host="localhost",
        user="admin", 
        password="torch",
        database="torch"
    )

    cursor = db.cursor()
    cursor.execute(
        "CREATE TABLE IF NOT EXISTS `mask_data` ( `id` INT AUTO_INCREMENT PRIMARY KEY, `location` VARCHAR(20), `count` INT, `year` INT, `month` INT, `day` INT, `hour` INT, `minute` INT, `weekday` INT)"
    )

def init_torch_database():
    database_init()
    create_table_user()
    create_table_population()
    create_table_mask()
    create_table_store_info()
