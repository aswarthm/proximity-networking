#include "painlessMesh.h"

#define MESH_PREFIX "prefix"
#define MESH_PASSWORD "password"
#define MESH_PORT 5555

painlessMesh mesh;
String nodeID;

int prevLedStatus = 0;
int ledStatus = 0;

void receivedCallback(uint32_t from, String &msg) {
  String lol = "Received from " + String(from) + " msg=" + msg;
  Serial.println(lol);
  if (msg.indexOf(nodeID) >= 0) {
    Serial.println("matched");
  }
}

void setup() {
  Serial.begin(9600);

  //mesh.setDebugMsgTypes( ERROR | MESH_STATUS | CONNECTION | SYNC | COMMUNICATION | GENERAL | MSG_TYPES | REMOTE ); // all types on
  mesh.setDebugMsgTypes(ERROR | STARTUP);  // set before init() so that you can see startup messages

  mesh.init(MESH_PREFIX, MESH_PASSWORD, MESH_PORT);
  nodeID = mesh.getNodeId();
  Serial.println("Connected to mesh with nodeID " + String(nodeID));

  mesh.onReceive(&receivedCallback);
}

void updateLED() {
  if (prevLedStatus != ledStatus) {
    Serial.println("Updated LED to " + String(ledStatus) + " from " + prevLedStatus);
    prevLedStatus = ledStatus;
  }
}

void loop() {
  mesh.update();
  updateLED();
}
