// Import libraries (BLEPeripheral depends on SPI)

#include <SPI.h>
#include <BLEPeripheral.h>
#include "sha256.h"
#include <Entropy.h>
#include <EEPROM.h>
#include <DB.h>

// message types
const unsigned char REGISTRATION_REQ = 1;
const unsigned char KEY_EXCHANGE = 2;
const unsigned char LOCK_REQ = 3;
const unsigned char UNLOCK_REQ = 4;
const unsigned char ERROR_REGISTRATION_FAILED = 5;
const unsigned char ERROR_AUTHENTICATION = 6;
const unsigned char ERROR_WRONG_ID = 7;
const unsigned char ERROR_WRONG_COMMAND = 8;
const unsigned char STATUS_UNLOCKED = 0;
const unsigned char STATUS_LOCKED = 1;

// message parts length
#define TYPE_LENGTH 1
#define RANDOM_LENGTH 4
#define ID_LENGTH 1
#define HMAC_KEY_LENGTH 18
#define HMAC_LENGTH 14
#define MESSAGE_FIRST_PART_LENGTH TYPE_LENGTH + RANDOM_LENGTH + ID_LENGTH
#define MESSAGE_LENGTH 20


// ################### PIN ASSIGNMENTS ###################

// define pins (varies per shield/board)
#define BLE_REQ   6
#define BLE_RDY   7
#define BLE_RST   4
#define LED_PIN   13

// Pins of the hall sensor
// Left|Middle|Right referrer to front view of the cylinder lock
#define HALL_LEFT 0
#define HALL_MIDDLE 1
#define HALL_RIGHT 2

// Pins to interface with the Motor
#define MOTOR_PIN_A   5
#define MOTOR_PIN_B   8

// Button pin for syncing
#define BUTTON_PIN  3

// ################### END PIN ASSIGNMENTS ###################


#define TIMEOUT_MOTOR_TURN 3000 
#define OPERATION_OK       0
#define OPERATION_FAILED   1

// Possible values of lock_state
// Assumes lock cylinder opening to the right
#define LOCK_OPERATION_END      0b00000000
#define LOCK_OPERATION_OPENING  0b00000001
#define LOCK_OPERATION_CLOSING  0b00000000

// Touch
#define BUTTUN_PRESS_TIME_LIMIT 5000 // User has 5 seconds, or the registration fails

// DB
//#define DB_CREATE
DB db;

#define MY_TBL 1
#define ID_SEQ_LOCATION 0
uint8_t idSeq = 0;

struct UserRec {
  unsigned char id;
  uint8_t secretKey[HMAC_KEY_LENGTH];
} userRec;

// Current status of the cylinder updated via interrupts
// The first 3 bits represent the hall sensors reading
volatile byte lock_state = 0x01;
volatile byte previous_lock_state = 0x01;

// Flag to check if the user is manually operating the lock
volatile bool user_operation = false;
volatile bool app_operation = false;

/*----- BLE Utility -------------------------------------------------------------------------*/
// create peripheral instance, see pinouts above
BLEPeripheral            blePeripheral        = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);

// create service
BLEService               uartService          = BLEService("713d0000503e4c75ba943148f18d941e");

// create characteristic
BLECharacteristic    txCharacteristic = BLECharacteristic("713d0002503e4c75ba943148f18d941e", BLENotify, 20);
BLECharacteristic    rxCharacteristic = BLECharacteristic("713d0003503e4c75ba943148f18d941e", BLEWriteWithoutResponse, 20);
BLECharacteristic    statusCharacteristic = BLECharacteristic("713d0004503e4c75ba943148f18d941e", BLERead | BLENotify, 1);

/*--------------------------------------------------------------------------------------------*/

boolean doorsLocked = false;

void setup()
{  
  //Serial.begin(115200);
#if defined (__AVR_ATmega32U4__)
  //Wait until the serial port is available (useful only for the Leonardo)
  //As the Leonardo board is not reseted every time you open the Serial Monitor
  delay(3000);  //5 seconds delay for enabling to see the start up comments on the serial board
#endif

  // DB initialization
  #ifdef DB_CREATE
    db.create(MY_TBL,sizeof(userRec)); // Call just once, it overwrites
    EEPROM.write(ID_SEQ_LOCATION, 0);
  #else
    idSeq = EEPROM.read(ID_SEQ_LOCATION);
  #endif  
  db.open(MY_TBL);
 // Serial.println(idSeq);
  
  // Random library initialization
  Entropy.initialize();

  // Initialize digital pin for LED
  pinMode(LED_PIN, OUTPUT);
  
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  
  // Initialise pins for hall sensors
  pinMode(HALL_LEFT, INPUT_PULLUP);
  pinMode(HALL_MIDDLE, INPUT_PULLUP);
  pinMode(HALL_RIGHT, INPUT_PULLUP);
  
  // Initialise pins for the h-bridge
  pinMode(MOTOR_PIN_A, OUTPUT);
  pinMode(MOTOR_PIN_B, OUTPUT);
  
  // Initialise hall sensor interrupt detection
  attachInterrupt(1,interruptHallMiddle,CHANGE);

  /*----- BLE Utility ---------------------------------------------*/
  // set advertised local name and service UUID
  blePeripheral.setLocalName("BLE-Doors");
  blePeripheral.setAdvertisedServiceUuid(uartService.uuid());

  // add service and characteristic
  blePeripheral.addAttribute(uartService);
  blePeripheral.addAttribute(rxCharacteristic);
  blePeripheral.addAttribute(txCharacteristic);
  blePeripheral.addAttribute(statusCharacteristic);

  // assign event handlers for connected, disconnected to peripheral
  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  // assign event handler for RX characteristic
  rxCharacteristic.setEventHandler(BLEWritten, rxCharacteristicWritten);

  statusCharacteristic.setValue((doorsLocked) ? &STATUS_LOCKED : &STATUS_UNLOCKED, TYPE_LENGTH); 
  // begin initialization
  blePeripheral.begin();
  /*---------------------------------------------------------------*/
 // Serial.println(F("H-Lock started"));
}

void loop()
{
  // Poll peripheral
  blePeripheral.poll();
  
  // Check if user operates the lock manually
  if(user_operation) {
    // Read out current lock status
    if(previous_lock_state == LOCK_OPERATION_OPENING && lock_state == LOCK_OPERATION_END) {
        // Update BLE characteristic
        statusCharacteristic.setValue(&STATUS_UNLOCKED, TYPE_LENGTH); 
    } else if(previous_lock_state == LOCK_OPERATION_CLOSING && lock_state == LOCK_OPERATION_END) {
        // Update BLE characteristic
        statusCharacteristic.setValue(&STATUS_LOCKED, TYPE_LENGTH); 
    }
    // Unset the flag
    user_operation = false;
    
  }
}

void blePeripheralConnectHandler(BLECentral& central) {
  // central connected event handler
 // Serial.println(F("Connected to central"));
//  Serial.println(central.address());
}

void blePeripheralDisconnectHandler(BLECentral& central) {
  // central disconnected event handler
//  Serial.println(F("Disconnected from central"));
//  Serial.println(central.address());

  // Clear characteristics
  txCharacteristic.setValue(0,0);
  rxCharacteristic.setValue(0,0);
}

void rxCharacteristicWritten(BLECentral& central, BLECharacteristic& characteristic) {
  unsigned char len = rxCharacteristic.valueLength();
  const unsigned char *message = rxCharacteristic.value();
//  Serial.print(F("didCharacteristicWritten, Length: ")); 
//  Serial.println(len, DEC);
//  unsigned char i = 0;
//  while(i<len)
//  {
//    Serial.write(message[i++]);
//  }
//  Serial.println();

  if(message[0] == REGISTRATION_REQ){
      registerUser();
  } else if (len == 20){
    // Secure message format
    if(loadUser(message[TYPE_LENGTH + RANDOM_LENGTH])){
      if(checkMessageSecurity(message)){
          switch(message[0]){
            case LOCK_REQ:
              app_operation = true;
              lockDoors();
            break;
            case UNLOCK_REQ:
              app_operation = false;
              unlockDoors();
            break;
            default:{
              txCharacteristic.setValue(&ERROR_WRONG_COMMAND, TYPE_LENGTH);
              feedback(false);
            }
          }
      }else{
        txCharacteristic.setValue(&ERROR_AUTHENTICATION, TYPE_LENGTH);
        feedback(false);
      }
    } else{
      txCharacteristic.setValue(&ERROR_WRONG_ID, TYPE_LENGTH);
      feedback(false);
    }
  }else{
    txCharacteristic.setValue(&ERROR_WRONG_COMMAND, TYPE_LENGTH);
    feedback(false);
  }
  app_operation = false;
}

boolean loadUser(unsigned char id){
  // Already loaded?
  if(userRec.id != id){
    for (int i = 1; i <= db.nRecs(); i++){
      db.read(i, DB_REC userRec);
      if(userRec.id == id){
          statusCharacteristic.setValue((doorsLocked) ? &STATUS_LOCKED : &STATUS_UNLOCKED, TYPE_LENGTH); 

        return true;
      }
    }
    // Did not find it
    return false;
  }
    statusCharacteristic.setValue((doorsLocked) ? &STATUS_LOCKED : &STATUS_UNLOCKED, TYPE_LENGTH); 

  return true;
}

void registerUser(){
  digitalWrite(LED_PIN, HIGH);
  // In 5 seconds the user should press the button or the registration fails
  unsigned long startTime = millis();
  unsigned long validBeforeTime = startTime + BUTTUN_PRESS_TIME_LIMIT;
  boolean pressed = true;
  do{
    pressed = digitalRead(BUTTON_PIN); 
  }while(pressed && millis() <= validBeforeTime);
  
  if(pressed){
    txCharacteristic.setValue(&ERROR_REGISTRATION_FAILED, TYPE_LENGTH);
    feedback(false);
    return;
  } else {
    feedback(true);
  }

  // Create secret key
  uint8_t secretKey[HMAC_KEY_LENGTH];
  for(int i = 0; i < HMAC_KEY_LENGTH; i++){
    secretKey[i] = Entropy.randomByte();
  }

  // Add to database
  userRec.id = ++idSeq;
  EEPROM.write(ID_SEQ_LOCATION, idSeq); // Save increased id sequence in EEPROM
  memcpy(userRec.secretKey, secretKey, HMAC_KEY_LENGTH);
  db.append(DB_REC userRec);
  
  // Send the key back
  unsigned char message[MESSAGE_LENGTH];
  message[0] = KEY_EXCHANGE;
  message[1] = userRec.id;
  memcpy(message + TYPE_LENGTH + ID_LENGTH, secretKey, HMAC_KEY_LENGTH);
  txCharacteristic.setValue(message, MESSAGE_LENGTH);
}

boolean checkMessageSecurity(const unsigned char* message){ 
    unsigned char messageFirstPart[MESSAGE_FIRST_PART_LENGTH];
    memcpy(messageFirstPart, message, MESSAGE_FIRST_PART_LENGTH);
    const unsigned char* messageHMACPart = message + MESSAGE_FIRST_PART_LENGTH;
    
    return memcmp(messageHMACPart, calculateHMAC(userRec.secretKey, messageFirstPart), HMAC_LENGTH) == 0;
}

uint8_t* calculateHMAC(uint8_t* key, unsigned char* messageFirstPart){
  Sha256.initHmac(key, HMAC_KEY_LENGTH);

  for (int i=0; i<MESSAGE_FIRST_PART_LENGTH; i++) {
      Sha256.write(messageFirstPart[i]);
  }
  return Sha256.resultHmac();
}

void unlockDoors(){

  if(doorsLocked){
    // European doors mostly have two lockturns
    // Only continue if previous operation was successful
    if(unlockTurn() == OPERATION_OK) {
      if(unlockTurn() == OPERATION_OK) {
        doorsLocked = false;
        statusCharacteristic.setValue(&STATUS_UNLOCKED, TYPE_LENGTH);
      }
    }
  } else {
    //statusCharacteristic.setValue(&STATUS_LOCKED, TYPE_LENGTH);
    feedback(false); 
  }
}

void lockDoors(){
  if(!doorsLocked){
    // European doors mostly have two lockturns
    // Only continue if previous operation was successful
    if(lockTurn() == OPERATION_OK){
      if(lockTurn() == OPERATION_OK) {
        doorsLocked = true;
        statusCharacteristic.setValue(&STATUS_LOCKED, TYPE_LENGTH);    
      }
    } 
  } else {
    //statusCharacteristic.setValue(&STATUS_UNLOCKED, TYPE_LENGTH);
    feedback(false); 
  }
}


boolean lockTurn() {
  unsigned long start_time = millis();
  unsigned long delta_t = 0;
  motorTurnLeft();
  
  // TODO: What happens if mills() is to slow and lock_state changes too fast?
  lock_state = 0x02;
  do {
    delta_t = millis() - start_time;
    if (delta_t > TIMEOUT_MOTOR_TURN) {
      feedback(false);
      motorStop();
      return OPERATION_FAILED;
    }
  }  while (lock_state != LOCK_OPERATION_END);
  
  motorStop();
  feedback(true);
  return OPERATION_OK;
}

boolean unlockTurn() {
  digitalWrite(LED_PIN, HIGH);
  unsigned long start_time = millis();
  unsigned long delta_t = 0;
  motorTurnRight();
  
  // TODO: What happens if mills() is to slow and lock_state changes too fast?
  lock_state = 0x02;
  do {
    delta_t = millis() - start_time;
    if(delta_t > TIMEOUT_MOTOR_TURN) {
      feedback(false);
      motorStop();
      return OPERATION_FAILED;
      
    }
  }  while (lock_state != LOCK_OPERATION_END);
  motorStop();
  feedback(true);
  return OPERATION_OK;
}


/* Motor helper functions */

void motorStop() {
  digitalWrite(MOTOR_PIN_A,LOW);
  digitalWrite(MOTOR_PIN_B,LOW);
}

void motorTurnLeft() {
  digitalWrite(MOTOR_PIN_A,HIGH);
  digitalWrite(MOTOR_PIN_B,LOW);
}

void motorTurnRight() {
  digitalWrite(MOTOR_PIN_A,LOW);
  digitalWrite(MOTOR_PIN_B,HIGH);
}

/* Hall switches interrupt routine */

void interruptHallMiddle()
{
  previous_lock_state = lock_state;
  //lock_state ^= 0b00000010;
  lock_state = int(digitalRead(HALL_LEFT)) | (int(digitalRead(HALL_MIDDLE)) << 1) | (int(digitalRead(HALL_RIGHT)) << 2);
  if(app_operation == false) {
    user_operation = true;
  } 
  
}

void feedback(boolean ok)
{
  int i;
  
  if (ok) {
    for (i = 0; i < 2; i++) {
      delay(500);
      digitalWrite(LED_PIN, LOW);
      delay(500);
      digitalWrite(LED_PIN, HIGH);
    }
    delay(500);
  }
  else {
    for (i = 0; i < 4; i++) {
      delay(150);
      digitalWrite(LED_PIN, LOW);
      delay(150);
      digitalWrite(LED_PIN, HIGH);
    }
    delay(150);
  }
     
  digitalWrite(LED_PIN, LOW);
}
