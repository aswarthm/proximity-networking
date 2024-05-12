from serial import Serial

from threading import Thread

import firebase_admin
from firebase_admin import db
import serial
import re
import json
import time

from genAI import getGeminiResponse

cred_obj = firebase_admin.credentials.Certificate('./fbconfig.json')
default_app = firebase_admin.initialize_app(cred_obj, {
        'databaseURL':"https://angelhack-370bd-default-rtdb.firebaseio.com/"
        })


matches = [] # required for threading
def check_match (client_id, client_data, dev_id, dev_data):
    prompt = "look at the profiles of the developer and client and check if the 2 profiles are a match. respond with yes or no. do not explain"
    
    prompt += "Client requirements: \n"
    prompt += client_data["technicalExpertise"] + "\n"

    prompt += "Developer skills: \n"
    prompt += dev_data["technicalExpertise"]
    
    ans = getGeminiResponse(prompt)
    print(ans, "**************\n")
    if "yes" in ans.lower():
        matches.append((client_id, dev_id))


def getPhoneNumberFromId(id, data):
    for ph_no, client_data in data["Client"].items():
        if client_data["nodeID"] == id:
            return ph_no
        
    for ph_no, dev_data in data["Developer"].items():
        if dev_data["nodeID"] == id:
            return ph_no
        

ser = Serial(port="/dev/tty")

while True:
    ref = db.reference("/")
    data = ref.get() # Gets full firebase

    if ser.in_waiting:
        id = ser.readline() # PUT \n at the end of string in esp32
        
        key = getPhoneNumberFromId(id, data)
        if key is None:
            print("KEY NOT FOUND")

        clients = data["Client"]
        developers = data["Developer"]
        
        threads = []
        if key in clients:
            for dev_key in developers:
                threads.append(Thread(target=check_match, args=[key, clients[key], dev_key, developers[dev_key]]))
            
        elif key in developers:
            for client_key in clients:
                threads.append(Thread(target=check_match, args=[client_key, clients[client_key], key, developers[key]]))

        for t in threads:
            t.start()

        for t in threads:
            t.join()

        print("Matches: ", matches)
        for match in matches:
            ref = db.reference("/matches")
            ref.push({
                "c_id": match[0],
                "d_id": match[1],
                "c_accept": False,
                "d_accept": False
            })

        
    ref = db.reference("/")
    data = ref.get() # Gets full firebase

    for k, match in data["matches"].items():
        if match["c_accept"] and match["d_accept"]:
            ser.write(f"{match['client_id']}, {match['dev_id']}, 2")
            continue 

        if match["c_accept"] or match["d_accept"]:
            ser.write(f"{match['client_id']}, {match['dev_id']}, 1")
            continue

        ser.write(f"{match['client_id']}, {match['dev_id']}, 0")

    time.sleep(1)
