import json
import requests
from pymongo import MongoClient
import appconfig
import datetime
import time

class UserDao:

    def list(self):
        """
            Retrieve the list of all the registered users on the database.

            If the database is unreachable, return None.
        """

        print("Listing all users on the database")

        client = MongoClient()
        client = MongoClient(appconfig.MONGO_HOST, appconfig.MONGO_PORT)

        if client is None:
            print('Could not connect to mongodb database [{0}:{1}]'.format(appconfig.MONGO_HOST,appconfig.MONGO_PORT))
            return None

        users_registered = []

        try:

            db     = client[appconfig.MONGO_DB_NAME]
            users  = db.users

            cursor = users.find({})

            for document in cursor:
                users_registered.append({
                    'c_id'                : document['c_id'],
                    'ip_addr'             : document['ip_addr'],
                    'port'                : document['port'],
                    'number_files_shared' : document['number_files_shared'],

                    'date_joined'         : document['date_joined'],
                    'last_heartbeat'      : document['last_heartbeat'],
                    'last_accessed'       : document['last_accessed']
                })

        except:
            print('Error when trying to fetch data from database {0}'.format(appconfig.MONGO_DB_NAME))
            return None


        return users_registered

    # ---------------------------------------------------------------------------------------------------------------------
    def register_new(self, c_id, hostname, ip_addr, port, number_files_shared):
        """
            Given client id details, create a new document in the database.
            If it is created, return the updated document leaf.

            If the record is not created, return None.
            If the database is unreachable, return None.
        """

        timestamp_now  = time.time()
        timestamp_str  = datetime.datetime.fromtimestamp(timestamp_now).strftime('%Y-%m-%d %H:%M:%S')
        today_date_str = timestamp_str

        new_user = {
            'c_id'                : c_id,
            'hostname'            : hostname,
            'ip_addr'             : ip_addr,
            'port'                : port,
            'number_files_shared' : number_files_shared,

            'date_joined'         : today_date_str,
            'last_heartbeat'      : today_date_str,
            'last_accessed'       : today_date_str,
            'hits'                : 0
        }
        print("Attempting to register new user: {0}".format(new_user))

        client = MongoClient()
        client = MongoClient(appconfig.MONGO_HOST, appconfig.MONGO_PORT)

        if client is None:
            print('Could not connect to mongodb database [{0}:{1}]'.format(appconfig.MONGO_HOST,appconfig.MONGO_PORT))
            return None

        user_created = None
        try:

            db     = client[appconfig.MONGO_DB_NAME]
            users   = db.users

            # Search for clientId first. If it exist, do not attempt creation
            existing_user = users.find_one({'c_id' : c_id})

            if existing_user is None:

                # Search for clientId first. If it exist, do not attempt creation
                user_id = users.insert_one(new_user).inserted_id

                if user_id is not None:
                    user_created = users.find_one({'_id' : user_id})

            else:
                # User was already on db.
                user_created = existing_user

        except e:
            print('Error when trying to fetch data from database {0}'.format(appconfig.MONGO_DB_NAME) % e)
            return None



        return user_created


    # ---------------------------------------------------------------------------------------------------------------------
    def register_heartbeat(self, c_id, number_files_shared):
        """
            Given a clientId, search for the document in the database. If it is found, try to update
            the fields [last_heartbeat,number_files_shared].

            On success, return the updated document leaf.

            If the record is not found, return None.
            If the database is unreachable, return None.
        """
        timestamp_now  = time.time()
        timestamp_str  = datetime.datetime.fromtimestamp(timestamp_now).strftime('%Y-%m-%d %H:%M:%S')
        today_date_str = timestamp_str

        print("Attempting to register heartbeat for user: {0}".format(c_id))

        client = MongoClient()
        client = MongoClient(appconfig.MONGO_HOST, appconfig.MONGO_PORT)

        if client is None:
            print('Could not connect to mongodb database [{0}:{1}]'.format(appconfig.MONGO_HOST,appconfig.MONGO_PORT))
            return None

        user_updated = None

        try:

            db           = client[appconfig.MONGO_DB_NAME]
            users        = db.users
            user_updated = users.find_one({'c_id' : c_id})

            if user_updated is not None:
                print('User found on database')
                users.find_one_and_update({'_id':user_updated['_id']}, { "$set" : {"last_heartbeat"     : today_date_str,
                                                                                  "number_files_shared": number_files_shared}})
        except Exception as e:
            print('Error when trying to fetch data from database {0}'.format(appconfig.MONGO_DB_NAME))
            print(str(e))
            return None


        return user_updated

    def get_server_connection_info(self, c_id):
        """
            Given a clientId, search for the document in the database. If it is found, sustract
            the connection info and update the fields [last_accessed,hits]

            On success, return the connection info.

            If the record is not found, return None.
            If the database is unreachable, return None.
        """
        timestamp_now  = time.time()
        timestamp_str  = datetime.datetime.fromtimestamp(timestamp_now).strftime('%Y-%m-%d %H:%M:%S')
        today_date_str = timestamp_str

        print("Attempting to look for host with clientId : {0}".format(c_id))

        client = MongoClient()
        client = MongoClient(appconfig.MONGO_HOST, appconfig.MONGO_PORT)

        if client is None:
            print('Could not connect to mongodb database [{0}:{1}]'.format(appconfig.MONGO_HOST,appconfig.MONGO_PORT))
            return None

        connection_info = None

        try:

            db           = client[appconfig.MONGO_DB_NAME]
            users        = db.users
            target_user  = users.find_one({'c_id' : c_id})

            print("Target user : {0}".format(str(target_user)))

            if target_user in [None]:
                print('User not found on database')
                return None

            else:
                print('User found on database')
                users.find_one_and_update({'_id':target_user['_id']}, { "$set" : {"last_accessed": today_date_str},
                                                                        "$inc": { "hits" : 1}})

                connection_info = {
                    'ip'        : target_user['ip_addr'],
                    'hostname'  : target_user['hostname'],
                    'port'      : target_user['port']
                }

        except Exception as e:
            print('Error when trying to fetch data from database {0}'.format(appconfig.MONGO_DB_NAME))
            print(str(e))
            return None

        print('Connection info retrieved: '.format(str(connection_info)))

        return connection_info
