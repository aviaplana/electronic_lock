// Import libraries (BLEPeripheral depends on SPI)
#include <SPI.h>
#include <BLEPeripheral.h>
#include "sha256.h"
#include <Entropy.h>
#include <EEPROM.h>
#include <DB.h>
#include <ADCTouch.h>

// message types
const unsigned char REGISTRATION_REQ = 1;
const unsigned char KEY_EXCHANGE = 2;
const unsigned char LOCK_REQ = 3;
const unsigned char UNLOCK_REQ = 4;
const unsigned char ERROR_REGISTRATION_FAILED = 5;
const unsigned char ERROR_AUTHENTICATION = 6;
const unsigned char ERROR_WRONG_ID = 7;
const unsigned char ERROR_WRONG_COMMAND = 8;

// message parts length
#define TYPE_LENGTH 1
#define RANDOM_LENGTH 4
#define ID_LENGTH 1
#define HMAC_KEY_LENGTH 18
#define HMAC_LENGTH 14
#define MESSAGE_FIRST_PART_LENGTH TYPE_LENGTH + RANDOM_LENGTH + ID_LENGTH
#define MESSAGE_LENGTH 20

// define pins (varies per shield/board)
#define BLE_REQ   6
#define BLE_RDY   7
#define BLE_RST   4
#define LED_PIN   13
#define BUTTON_PIN  A3

// Touch
#define BUTTUN_PRESS_TIME_LIMIT 5000 // User has 5 seconds, or the registration fails
int touchRef0;

// DB
//#define DB_DEBUG
DB db;

#define MY_TBL 1
#define ID_SEQ_LOCATION 0
uint8_t idSeq = 0;

struct UserRec {
  unsigned char id;
  uint8_t secretKey[HMAC_KEY_LENGTH];
} *userRec;

/*----- BLE Utility -------------------------------------------------------------------------*/
// create peripheral instance, see pinouts above
BLEPeripheral            blePeripheral        = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);

// create service
BLEService               uartService          = BLEService("713d0000503e4c75ba943148f18d941e");

// create characteristic
BLECharacteristic    txCharacteristic = BLECharacteristic("713d0002503e4c75ba943148f18d941e", BLENotify, 20);
BLECharacteristic    rxCharacteristic = BLECharacteristic("713d0003503e4c75ba943148f18d941e", BLEWriteWithoutResponse, 20);
/*--------------------------------------------------------------------------------------------*/

boolean doorsLocked = false;

void setup()
{  
  Serial.begin(115200);
#if defined (__AVR_ATmega32U4__)
  //Wait until the serial port is available (useful only for the Leonardo)
  //As the Leonardo board is not reseted every time you open the Serial Monitor
  while(!Serial) {
  }
  delay(3000);  //5 seconds delay for enabling to see the start up comments on the serial board
#endif

  // DB initialization
  #ifdef DB_DEBUG
    db.create(MY_TBL,sizeof(userRec)); // Call just once, it overwrites
    EEPROM.write(ID_SEQ_LOCATION, 0);
  #else
    idSeq = EEPROM.read(ID_SEQ_LOCATION);
  #endif  
  db.open(MY_TBL);
  Serial.println(idSeq);
  
  // Random library initialization
  Entropy.initialize();

  // Initialize digital pin for LED
  pinMode(LED_PIN, OUTPUT);
  
  // Get touch reference value
  touchRef0 = ADCTouch.read(BUTTON_PIN, 500); 

  /*----- BLE Utility ---------------------------------------------*/
  // set advertised local name and service UUID
  blePeripheral.setLocalName("BLE-Doors");
  blePeripheral.setAdvertisedServiceUuid(uartService.uuid());

  // add service and characteristic
  blePeripheral.addAttribute(uartService);
  blePeripheral.addAttribute(rxCharacteristic);
  blePeripheral.addAttribute(txCharacteristic);

  // assign event handlers for connected, disconnected to peripheral
  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  // assign event handler for RX characteristic
  rxCharacteristic.setEventHandler(BLEWritten, rxCharacteristicWritten);

  // begin initialization
  blePeripheral.begin();
  /*---------------------------------------------------------------*/

  Serial.println(F("BLE UART Peripheral"));
}

void blePeripheralConnectHandler(BLECentral& central) {
  // central connected event handler
  Serial.print(F("Connected event, central: "));
  Serial.println(central.address());
}

void blePeripheralDisconnectHandler(BLECentral& central) {
  // central disconnected event handler
  Serial.print(F("Disconnected event, central: "));
  Serial.println(central.address());

  // Clear characteristics
  txCharacteristic.setValue(0,0);
  rxCharacteristic.setValue(0,0);
}

void rxCharacteristicWritten(BLECentral& central, BLECharacteristic& characteristic) {
  unsigned char len = rxCharacteristic.valueLength();
  const unsigned char *message = rxCharacteristic.value();
  Serial.print(F("didCharacteristicWritten, Length: ")); 
  Serial.println(len, DEC);
  unsigned char i = 0;
  while(i<len)
  {
    Serial.write(message[i++]);
  }
  Serial.println();

  if(message[0] == REGISTRATION_REQ){
      registerUser();
  } else if (len == 20){
    // Secure message format
    if(loadUser(message[TYPE_LENGTH + RANDOM_LENGTH])){
      if(checkMessageSecurity(message)){
          switch(message[0]){
            case LOCK_REQ:
              lockDoors();
            break;
            case UNLOCK_REQ:
              unlockDoors();
            break;
            default:{
              txCharacteristic.setValue(&ERROR_WRONG_COMMAND, TYPE_LENGTH);
            }
          }
      }else{
        txCharacteristic.setValue(&ERROR_AUTHENTICATION, TYPE_LENGTH);
      }
    } else{
      txCharacteristic.setValue(&ERROR_WRONG_ID, TYPE_LENGTH);
    }
  }else{
    txCharacteristic.setValue(&ERROR_WRONG_COMMAND, TYPE_LENGTH);
  }
//  Sha256.initHmac((const uint8_t*)"bla", 4);
//  char m[] = {'b', 'l', 'a', 'b', 'l', 'a', '\0'};
//  char m[] = "blabla";
//  Sha256.print(m);
//  printHash(Sha256.resultHmac());
}

boolean loadUser(unsigned char id){
  // Already loaded?
  if(userRec == NULL || userRec->id != id){
    for (int i = 1; i <= db.nRecs(); i++){
      db.read(i, DB_REC userRec);
      if(userRec->id == id){
        return true;
      }
    }
    // Did not find it
    return false;
  }
  return true;
}

void printHash(uint8_t* hash) {
  int i;
  for (i=0; i<32; i++) {
    Serial.print("0123456789abcdef"[hash[i]>>4]);
    Serial.print("0123456789abcdef"[hash[i]&0xf]);
  }
  Serial.println();
}

void loop()
{
  // poll peripheral
  blePeripheral.poll();
//  BLECentral central = blePeripheral.central();
//
//  if (central) 
//  {
//    while (central.connected()) 
//    { 
//      if ( Serial.available() )
//      {
//        delay(5);
//        unsigned char len = 0;
//        len = Serial.available(); 
//        char val[len];
//        Serial.readBytes(val, len);
//        txCharacteristic.setValue((const unsigned char *)val, len);
//      }
//    }
//  }
}

void registerUser(){
  // In 5 seconds the user should press the button or the registration fails
  unsigned long startTime = millis();
  unsigned long validBeforeTime = startTime + BUTTUN_PRESS_TIME_LIMIT;
  boolean pressed = false;
  do{
    pressed = ADCTouch.read(BUTTON_PIN)-touchRef0 > 40;
  }while(!pressed && millis() <= validBeforeTime);
  
  if(!pressed){
    txCharacteristic.setValue(&ERROR_REGISTRATION_FAILED, TYPE_LENGTH);
    return;
  }

  // Create secret key
  uint8_t secretKey[HMAC_KEY_LENGTH];
  for(int i = 0; i < HMAC_KEY_LENGTH; i++){
    secretKey[i] = Entropy.randomByte();
  }

  // Add to database
  userRec = new UserRec();
  userRec->id = ++idSeq;
  EEPROM.write(ID_SEQ_LOCATION, idSeq); // Save increased id sequence in EEPROM
  memcpy(userRec->secretKey, secretKey, HMAC_KEY_LENGTH);
  db.append(DB_REC userRec);
  
  // Send the key back
  unsigned char message[MESSAGE_LENGTH];
  message[0] = KEY_EXCHANGE;
  message[1] = userRec->id;
  memcpy(message + TYPE_LENGTH + ID_LENGTH, secretKey, HMAC_KEY_LENGTH);
  txCharacteristic.setValue(message, MESSAGE_LENGTH);
}

boolean checkMessageSecurity(const unsigned char* message){
  if(sizeof(message) == 20){
    unsigned char messageFirstPart[MESSAGE_FIRST_PART_LENGTH];
    memcpy(messageFirstPart, message, MESSAGE_FIRST_PART_LENGTH);
    const unsigned char* messageHMACPart = message + MESSAGE_FIRST_PART_LENGTH;
    return memcmp(messageHMACPart, calculateHMAC(userRec->secretKey, messageFirstPart), HMAC_LENGTH) == 0;
  }
  return false;
}

uint8_t* calculateHMAC(uint8_t* key, unsigned char* messageFirstPart){
  Sha256.initHmac(key, HMAC_KEY_LENGTH);
  Sha256.print((char*)messageFirstPart);
  return Sha256.resultHmac();
}

void unlockDoors(){

  if(doorsLocked){
    swithchLED(false);
    delay(500);
    swithchLED(true);
    delay(500);
    swithchLED(false);
    delay(500);
    swithchLED(true);
    delay(500);
    swithchLED(false);

    doorsLocked = false;  
  }
}

void lockDoors(){
  if(!doorsLocked){
    swithchLED(true);
    delay(500);
    swithchLED(false);
    delay(500);
    swithchLED(true);
    delay(500);
    swithchLED(false);
    delay(500);
    swithchLED(true);

    doorsLocked = true;    
  }
}

void swithchLED(boolean enable){
  digitalWrite(LED_PIN, (enable) ? HIGH : LOW);   // turn the LED on (HIGH is the voltage level)
}

