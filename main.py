from serial import Serial

from threading import Thread

import firebase_admin
from firebase_admin import db
import serial
import re
import json
import time

# from genAI import getPromptFromData, getGeminiResponse

cred_obj = firebase_admin.credentials.Certificate('./fbconfig.json')
default_app = firebase_admin.initialize_app(cred_obj, {
        'databaseURL':"https://angelhack-370bd-default-rtdb.firebaseio.com/"
        })


matches = [] # required for threading
def check_match(client_data, dev_data):
    
    

    # if match, append to matches
    



ser = Serial(port="/dev/tty")

while True:
    ref = db.reference("/")
    data = ref.get() # Gets full firebase

    if ser.in_waiting:
        id = ser.readline() # PUT \n at the end of string in esp32
        
        clients = data["clients"]
        developers = data["developers"]
        
        threads = []
        if id in data["clients"]:
            for dev_id in developers:
                threads.append(Thread(target=check_match), args=[data["clients"][id], data["developers"][dev_id]])
            
        else:
            for client_id in clients:
                threads.append(Thread(target=check_match), args=[data["clients"][client_id], data["developers"][id]])

        for t in threads:
            t.start()

        for t in threads:
            t.join()

        for match in matches:
            ref = db.reference("/matches")
            ref.push({
                "client_id": match[0],
                "dev_id": match[1],
                "client_confirmed": 0,
                "dev_confirmed":0
            })

        continue


    for match in data["matches"]:
        if match["client_confirmed"] and match["dev_confirmed"]:
            ser.write(f"{match["client_id"]}, {match["dev_id"]}", 0)
            continue 

        if match["client_confirmed"] or match["dev_confirmed"]:
            ser.write(f"{match["client_id"]}, {match["dev_id"]}", 1)
            continue

        ser.write(f"{match["client_id"]}, {match["dev_id"]}", 2)

    time.sleep(1)
