import json
import requests
from flask import jsonify, request

from models.userdao import UserDao

class UserController:

    def list_users(self):
        users_list = UserDao().list()
        print(users_list)
        return jsonify(users_list)

    def get_server_connection_info(self, request):
        response = {}
        payload  = request.get_json(silent=True)

        #
        # Validate parameters
        #
        expected_fields = [
            {'name' :'clientGUID'       , 'type' : 'string' }
        ]

        for expected_field in expected_fields:

            field_name = expected_field['name']

            if field_name not in payload:
                return self.build_response_json('error','Expected field [{0}] is not present on payload'.format(expected_field))

            field_value = payload[field_name]

            if field_value in [None,""]:
                return self.build_response_json('error','Field [{0}] should contain a valid value'.format(expected_field))

        #
        #   Get connection info from database
        #
        user_dao        = UserDao()
        connection_info = user_dao.get_server_connection_info(c_id=payload['clientGUID'])

        if connection_info is None:
            return self.build_response_json('error','Could not retrieve connection info of clientId {0}.'.format(payload['clientGUID']))

        return self.build_response_json('success',connection_info)



    def register_client_heartbeat(self, request):
        response = {}
        payload  = request.get_json(silent=True)

        #
        # Validate parameters
        #
        expected_fields = [
            {'name' :'clientGUID'       , 'type' : 'string' },
            {'name' :'numberFilesShared', 'type' : 'int' }
        ]

        for expected_field in expected_fields:

            field_name = expected_field['name']

            if field_name not in payload:
                return self.build_response_json('error','Expected field [{0}] is not present on payload'.format(expected_field))

            field_value = payload[field_name]

            if field_value in [None,""]:
                return self.build_response_json('error','Field [{0}] should contain a valid value'.format(expected_field))


        #
        #   Register data into database
        #
        user_dao     = UserDao()
        updated_user = user_dao.register_heartbeat(c_id=payload['clientGUID'], number_files_shared=payload['numberFilesShared'])

        if(updated_user in [None]):
            return self.build_response_json('error','Cannot register heartbeat of client {0}.'.format(payload['clientGUID']))

        return self.build_response_json('success','Heartbeat for client ID [{0}] registered successfully'.format(payload['clientGUID']))

    def register_connection(self,request):

        response = {}
        payload  = request.get_json(silent=True)

        #
        # Validate parameters
        #
        expected_fields = [
            {'name' :'clientGUID'       , 'type' : 'string' },
            {'name' :'ip_addr'       , 'type' : 'string' },
            {'name' :'hostname'       , 'type' : 'string' },
            {'name' :'port'             , 'type' : 'int' },
            {'name' :'numberFilesShared', 'type' : 'int' }
        ]

        for expected_field in expected_fields:

            field_name = expected_field['name']

            if field_name not in payload:
                return self.build_response_json('error','Expected field [{0}] is not present on payload'.format(expected_field))

            field_value = payload[field_name]

            if field_value in [None,""]:
                return self.build_response_json('error','Field [{0}] should contain a valid value'.format(expected_field))


            # TODO: implement type validation


        #
        #   Register data into database
        #
        user_dao = UserDao()
        origin   = request.headers.get('X-Forwarded-For', request.remote_addr)

        new_user = user_dao.register_new(c_id=payload['clientGUID'], hostname=payload['hostname'],ip_addr=payload['ip_addr'], port=payload['port'], number_files_shared=payload['numberFilesShared'])

        if(new_user in [None]):
            return self.build_response_json('error','Client {0} could not be registered in database. Check with admin'.format(payload['clientGUID']))

        return self.build_response_json('success','Connection ID [{0}] registered successfully'.format(payload['clientGUID']))


    def build_response_json(self, status, message ):
        return jsonify({'status':status, 'message': message})
