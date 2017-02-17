/*

MIT License

Copyright (c) 2017 Grzegorz Hołdys

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

/*
 * Arduino Board Setup:
 * 
 * Flash Mode: DIO
 * Flash Frequency: 80MHz
 * Upload Using: Serial
 * CPU Frequency: 80MHz
 * Flash Size: 512K (no SPIFFS)
 * Reset Method: nodemcu
 * 
 */

#include <Servo.h>
#include <HX711.h>
#include <ESP8266WiFi.h>
#include "SmartBowlConfig.h"

const int LED_PIN = 5;                                // Thing's onboard, green LED
const int SERVO_SIGNAL_PIN = 4;
const int DATA_PIN = 2;
const int CLK_PIN = 14;

const float CALIBRATION_FACTOR = 2926;
const float ACCEPTABLE_SPREAD = 0.1;                  // [g] measured between buffer's max and min values
const float UPPER_DELTA_MARGIN = 0.5;                 // [g]
const float LOWER_DELTA_MARGIN = 0.5;                 // [g]
const float REFILL_TRIGGER_AMOUNT = 10.0;             // [g]
const int MINUTE_MILLIS = 60*1000;                    // [ms]
const int UPLOAD_INTERVAL_MILLIS = 30*MINUTE_MILLIS;  // [ms] post every 30 minutes
const int PREFERRED_PAUSE = 100;                      // [ms]
const int BUFFER_SIZE = 10;                           // with 100ms pause, this gives 1s of data
const int REFILL_INTERVAL_MILLIS = 2000;              // [ms] 2 seconds
const int CONNECTION_TIMEOUT = 5000;                  // [ms] 5 seconds

WiFiClient client;
HX711 Scale(DATA_PIN, CLK_PIN);
Servo servo; 

float buffer[BUFFER_SIZE];
int cursorIndex = 0;
int nextUploadTime = 0;             // [ms]
int nextRefillTime = 0;             // [ms]
int rationLeft = 0;                 // [g]
int rationPeriodEndTime = 0;        // [ms]
int numRefills = 0;
float currentAmount = 0.0;          // [g]
float amountConsumed = 0.0;         // [g]
float amountAdded = 0.0;            // [g]


void setup() {
  Serial.begin(9600);
  Serial.println("\nSmartBowl-Device v0.4.4");
  pinMode(LED_PIN, OUTPUT); // Set LED as output
  digitalWrite(LED_PIN, HIGH); // LED off    
  initWiFi();
  initHX711();
  initBuffer();
  initServo();
  nextUploadTime = millis(); // update ASAP
}

void initWiFi() {
  WiFi.forceSleepBegin(); 
}

void initHX711() {
  pinMode(LED_PIN, OUTPUT); // Set LED as output
  digitalWrite(LED_PIN, HIGH); // LED off    
  // Blink for 3 seconds before initializing HX711
  byte ledStatus = LOW;
  for ( int i=0; i<30; i++ ) {
    digitalWrite(LED_PIN, ledStatus); // Write LED high/low
    ledStatus = (ledStatus == HIGH) ? LOW : HIGH;
    delay(100);
  }
  digitalWrite(LED_PIN, HIGH); // LED off
  Serial.println("Initializing HX711");
  Scale.set_scale(CALIBRATION_FACTOR);
  Scale.tare();
}

void initBuffer() {
  Serial.println("Initializing Buffer");
  cursorIndex = 0;
  for ( int i=0; i<BUFFER_SIZE; i++ ) {
    buffer[i] = 0.0;
  }
}

void initServo() {
  Serial.println("Initializing Servo");
  servo.attach(SERVO_SIGNAL_PIN);  // attaches the servo on pin 9 to the servo object
  servo.write(180);
}

void connectWiFi() {
  // LED turns on when we enter, it'll go off when we successfully post.
  byte ledStatus = LOW;
  digitalWrite(LED_PIN, ledStatus);  
  Serial.println();
  Serial.print("Connecting to: ");
  Serial.println( WiFiSSID );

  WiFi.forceSleepWake();
  WiFi.mode(WIFI_STA);
  WiFi.begin(WiFiSSID, WiFiPSK);

  while (WiFi.status() != WL_CONNECTED) {
    // Blink the LED
    digitalWrite(LED_PIN, ledStatus); // Write LED high/low
    ledStatus = (ledStatus == HIGH) ? LOW : HIGH;
    delay(100);
  }
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP()); 
}

void disconnectWiFi() {
  Serial.println("Disconnecting...");
  WiFi.forceSleepBegin(); 
  digitalWrite(LED_PIN, HIGH);
}

void loop() {
  long t0 = millis();
  calculateAmountConsumedAndAdded();

  if ( t0 > nextUploadTime ) {
    connectWiFi();
    getConfig();
    uploadData();
    disconnectWiFi();
    nextUploadTime = t0+UPLOAD_INTERVAL_MILLIS;
  } else if ( t0 > nextRefillTime ) {    
    if ( currentAmount < REFILL_TRIGGER_AMOUNT ) {
      refill();
    }
    nextRefillTime = t0+REFILL_INTERVAL_MILLIS;
  } else if ( rationPeriodEndTime > 0 && t0 > rationPeriodEndTime ) {
    connectWiFi();
    getConfig();
    disconnectWiFi();
  } 
  long pauseMillis = PREFERRED_PAUSE-(millis()-t0);
  pause( pauseMillis );
}

void pause( int pauseMillis ) {
  if ( pauseMillis > 0 ) {
    if ( pauseMillis < PREFERRED_PAUSE ) {
      delay( pauseMillis );
    } else {
      delay( PREFERRED_PAUSE );
    }
  } else {
    yield();
  }
}

void calculateAmountConsumedAndAdded() {  
  float average, spread;
  
  buffer[cursorIndex++] = Scale.get_units();
  if ( cursorIndex >= BUFFER_SIZE ) {
      cursorIndex = 0;
  }
  
  calculateBufferAverageAndSpread( average, spread );  

  Serial.print("Average = ");  
  Serial.print(average);
  Serial.print("; Spread = ");  
  Serial.println(spread);
  
  if ( spread <= ACCEPTABLE_SPREAD ) {      
    if ( average > (currentAmount+UPPER_DELTA_MARGIN) || average < (currentAmount-LOWER_DELTA_MARGIN) ) {
      if ( average < currentAmount ) {
        amountConsumed += currentAmount-average;
        rationLeft -= amountConsumed;
      } else if ( average > currentAmount ) {
        amountAdded += average-currentAmount;
      }
      currentAmount = average;
      Serial.print("Current amount = ");
      Serial.println(currentAmount);
    }
  }
}

void calculateBufferAverageAndSpread( float &average, float &spread ) {
    float sum = 0.0, maxValue = 0.0, minValue = 9999.0, value;
    average = 0.0;
    spread = 0.0;
    
    for ( int i=0; i<BUFFER_SIZE; i++ ) {
        value = buffer[i];
        sum += value;
        maxValue = _max( maxValue, value );
        minValue = _min( minValue, value );
    }
    average = sum / BUFFER_SIZE;    
    spread = maxValue - minValue;
}

void uploadData() {
  Serial.println("Uploading data...");

  if (!client.connect(SERVER, 80)) {
    Serial.println(">>> Can't connect to service!");
    return;
  }
  
  // Assemble data entry ( <amount remaining>,<amount consumed>,<amount added> ):
  String data = String();
  data.concat(currentAmount);
  data.concat(",");
  data.concat(amountConsumed);
  data.concat(",");
  data.concat(amountAdded);
  data.concat(",");
  data.concat(numRefills);
  amountConsumed = 0.0;  // Resetting amountConsumed value
  amountAdded = 0.0;  // Resetting amountAdded value
  numRefills = 0;   // Resetting the number of refills
  
  // Send
  client.print("POST /dataSets/");
  client.print( DEVICE_ID );
  client.println(" HTTP/1.1");
  client.print("Host: ");
  client.println( SERVER );
  client.println("User-Agent: Arduino/1.0");
  client.println("Connection: close");
  client.println("Accept: text/plain");
  client.println("Content-Type: text/plain");
  client.print("Content-Length: ");
  client.println(data.length());
  client.println();
  client.println(data); 

  if ( responseAvailable() ) {
    while ( client.available() ) {
      String line = client.readStringUntil('\r');
      Serial.println(line);
    }
  }
}

void getConfig() {
  Serial.println("Getting config...");
  if (!client.connect(SERVER, 80)) {
    Serial.println(">>> Can't connect to service!");
    return;
  }

  String url = "/configurations/" + DEVICE_ID + "?fields=rationLeft,secondsLeft";
  
  client.print( "GET " );
  client.print( url );
  client.println( " HTTP/1.1" );
  client.print( "Host: " );
  client.println( SERVER );
  client.println("Connection: close");
  client.println();

  if ( responseAvailable() ) {
    bool headerComplete = false;
    bool responseOK = false;
    String responseMessage = "";
    String line;
    while ( client.available() ) {
      line = client.readStringUntil('\r');
      line.trim();
      if ( headerComplete ) {
        if ( responseOK ) {
          int commaIndex = line.indexOf(',');      
          String rationLeftStr = line.substring(0, commaIndex);
          String secondsLeftStr = line.substring(commaIndex+1, line.length());
          rationLeft = rationLeftStr.toInt();
          rationPeriodEndTime = millis() + secondsLeftStr.toInt()*1000;
        } else {
          Serial.println( responseMessage );
        }
      } else if ( line.length() == 0 ) {
        headerComplete = true;
      } else {
        // Look for status
        if ( line.indexOf("HTTP") == 0 && line.indexOf("200") > 0 ) {
          responseMessage = String(line);
          responseOK = true;
        }
      }
    }
  }
}

void refill() {
  if ( rationLeft <= 0 ) return;  // Do nothing if the whole ration has been consumed
  
  Serial.println("Refill!");
  if ( !servo.attached() ) {
    servo.attach(SERVO_SIGNAL_PIN);
  }
  servo.write(140);
  delay(140);
  servo.write(180);
  delay(300);
  if ( servo.attached() ) {
    servo.detach();
  }
  numRefills++;
}

bool responseAvailable() {
  unsigned long t0 = millis();
  while (client.available() == 0) {
    if (millis() - t0 > CONNECTION_TIMEOUT) {
      Serial.println(">>> Client Timeout!");
      client.stop();
      return false;
    }
  }
  return true;
}

