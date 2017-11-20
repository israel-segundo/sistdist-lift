from flask import Flask, render_template, request, url_for, redirect, flash,jsonify
#from services.hudsonservice import HudsonDataSource
from controllers.usercontroller import UserController
import appconfig
import json
import requests
import pprint

# Initialize flask application
app = Flask(__name__)

@app.route("/")
def welcome():
  return "Welcome to lift server"

@app.route("/users")
def users():
    user_controller = UserController()
    user_lst        = user_controller.list_users()

    print(user_lst)
    return user_lst

@app.route('/register', methods=['POST'])
def register_connection():
    return UserController().register_connection(request)


@app.route('/heartbeat', methods=['POST'])
def register_client_heartbeat():
    return UserController().register_client_heartbeat(request)


@app.route('/getServerConnectionInfo', methods=['POST'])
def get_server_connection_info():
    return UserController().get_server_connection_info(request)



app.secret_key = "MRDRMRDRMRDRMRDRMRDRMRDRMRDRMRDR"

# Run the app
if __name__ == '__main__':
  app.run(
    host="0.0.0.0",
    port=int(appconfig.MS_APP_PORT)
    ,debug=True
  )
