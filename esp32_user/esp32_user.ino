#include "painlessMesh.h"

#define MESH_PREFIX "prefix"
#define MESH_PASSWORD "password"
#define MESH_PORT 5555

//27 26 33
const int ledPinRed = 33;    // 16 corresponds to GPIO16
const int ledPinGreen = 26;  // 16 corresponds to GPIO16
const int ledPinBlue = 27;   // 16 corresponds to GPIO16

// setting PWM properties
const int freq = 5000;
const int ledChannelRed = 0;
const int ledChannelGreen = 1;
const int ledChannelBlue = 2;
const int resolution = 8;

int color[5][3] = {
  { 255, 255, 255 },
  { 0, 255, 255 },
  { 255, 0, 255 },
  { 255, 255, 0 },
  { 0, 0, 0 },
};

painlessMesh mesh;
String nodeID;

int ledStatus = 0;
int prevLedStatus = 0;

void receivedCallback(uint32_t from, String &msg) {
  String lol = "Received from " + String(from) + " msg=" + msg;
  Serial.println(lol);
  if (msg.indexOf(nodeID) >= 0) {
    Serial.println("matched");
    ledStatus = msg.charAt(msg.length() - 1) - '0';
  }
}

void setup() {
  Serial.begin(9600);
  // configure LED PWM functionalitites
  ledcSetup(ledChannelRed, freq, resolution);
  ledcSetup(ledChannelGreen, freq, resolution);
  ledcSetup(ledChannelBlue, freq, resolution);

  // attach the channel to the GPIO to be controlled
  ledcAttachPin(ledPinRed, ledChannelRed);
  ledcAttachPin(ledPinGreen, ledChannelGreen);
  ledcAttachPin(ledPinBlue, ledChannelBlue);

  ledcWrite(ledChannelRed, 255);
  ledcWrite(ledChannelGreen, 255);
  ledcWrite(ledChannelBlue, 255);

  //mesh.setDebugMsgTypes( ERROR | MESH_STATUS | CONNECTION | SYNC | COMMUNICATION | GENERAL | MSG_TYPES | REMOTE ); // all types on
  mesh.setDebugMsgTypes(ERROR | STARTUP);  // set before init() so that you can see startup messages

  mesh.init(MESH_PREFIX, MESH_PASSWORD, MESH_PORT);
  nodeID = mesh.getNodeId();
  Serial.println("Connected to mesh with nodeID " + nodeID);

  mesh.onReceive(&receivedCallback);

  ledcWrite(ledChannelRed, 255);
  ledcWrite(ledChannelGreen, 255);
  ledcWrite(ledChannelBlue, 255);
}

void updateLED() {
  if (prevLedStatus != ledStatus) {
    Serial.println("Updated LED to " + String(ledStatus) + " from " + prevLedStatus);
    prevLedStatus = ledStatus;

    ledcWrite(ledChannelRed, color[ledStatus][0]);
    ledcWrite(ledChannelGreen, color[ledStatus][1]);
    ledcWrite(ledChannelBlue, color[ledStatus][2]);
  }
}

void loop() {
  mesh.update();
  updateLED();
}
