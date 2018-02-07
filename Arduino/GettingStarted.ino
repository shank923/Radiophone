/*
* Arduino Wireless Communication Tutorial
*     Example 1 - Transmitter Code
*                
* by Dejan Nedelkovski, www.HowToMechatronics.com
* 
* Library: TMRh20/RF24, https://github.com/tmrh20/RF24/
*/
#include <SPI.h>

#include "RH_NRF24.h"
// Singleton instance of the radio driver
RH_NRF24 nrf24;
String s;
uint8_t text[RH_NRF24_MAX_MESSAGE_LEN] = "";
void setup() 
{
  Serial.begin(9600);
  while (!Serial) 
    ; // wait for serial port to connect. Needed for Leonardo only
  if (!nrf24.init())
    Serial.println("init failed");
  // Defaults after init are 2.402 GHz (channel 2), 2Mbps, 0dBm
  if (!nrf24.setChannel(1))
    Serial.println("setChannel failed");
  if (!nrf24.setRF(RH_NRF24::DataRate2Mbps, RH_NRF24::TransmitPower0dBm))
    Serial.println("setRF failed");    
 else
 Serial.println("all is okay");
}

void loop()
{
if(Serial.available())
{
  s = Serial.readString();
  s.toCharArray(text, RH_NRF24_MAX_MESSAGE_LEN);
  if(text != "")
  {
  nrf24.send(text, sizeof(text));
  nrf24.waitPacketSent();
  }
}
   else {
    // Wait for a message
    char buf[RH_NRF24_MAX_MESSAGE_LEN];
    uint8_t len = sizeof(buf);
    while (nrf24.waitAvailableTimeout(200) && nrf24.recv(buf, &len))
    {   
        Serial.println(buf);      
    }         
  }
}
