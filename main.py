from serial import Serial

from threading import Thread

import firebase_admin
from firebase_admin import db
import serial
import re
import json
import time

import requests

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
    return None

ser = Serial(port="COM3")



while True:
    ref = db.reference("/")
    data = ref.get() # Gets full firebase

    if ser.in_waiting:
        
        id = ser.readline().decode('utf-8').strip() # PUT \n at the end of string in esp32
        print("NODE JOINED NET", id)
        key = getPhoneNumberFromId(id, data)
        if key is None:
            print("KEY NOT FOUND")
            continue

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
            try:
                mx = ref.get()
                print("MX", mx)
                index = len(mx)
                mx.append({
                "c_id": match[0],
                "d_id": match[1],
                "c_accept": False,
                "d_accept": False
            })
                ref.set(set(mx))
            except:
                mx = [{
                "c_id": match[0],
                "d_id": match[1],
                "c_accept": False,
                "d_accept": False
            }]
                ref.set(mx)
        matches.clear()

    ref = db.reference("/")
    data = ref.get() # Gets full firebase
    # print("hi", data["matches"])
    for match in data["matches"]:
        print(match)
        client_phone = match['c_id']
        dev_phone = match['d_id']

        client_id = data["Client"][client_phone]["nodeID"]
        dev_id = data["Developer"][dev_phone]["nodeID"]


        if match["c_accept"] and match["d_accept"]:
            print(client_id, dev_id, 3)
            aaa = f"{client_id}, {dev_id}, 2\r\n"

            ser.write(aaa.encode())
            continue 

        if match["c_accept"] or match["d_accept"]:
            print(client_id, dev_id, 2)
            aaa = f"{client_id}, {dev_id}, 3\r\n"
            ser.write(aaa.encode())
            continue

        print(client_id, dev_id, 1)
        aaa = f"{client_id}, {dev_id}, 1\r\n"

        ser.write(aaa.encode())
        


    time.sleep(1)
