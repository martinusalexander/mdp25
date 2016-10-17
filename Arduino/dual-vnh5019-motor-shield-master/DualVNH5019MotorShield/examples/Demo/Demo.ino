#include "DualVNH5019MotorShield.h"

DualVNH5019MotorShield md;

int pin = 3;

 unsigned long duration;

void stopIfFault()
{
  if (md.getM1Fault())
  {
    Serial.println("M1 fault");
    while(1);
  }
  if (md.getM2Fault())
  {
    Serial.println("M2 fault");
    while(1);
  }
}

void setup()
{
  Serial.begin(115200);
  Serial.println("Dual VNH5019 Motor Shield");
  md.init();
  
  pinMode(pin, INPUT);
  //attachInterrupt(digitalPinToInterrupt(pin), print1, HIGH);
}

void print1()
{
  duration = pulseIn(pin, HIGH);
  //String stringOne =  String(duration);
  //Serial.println("pulse is " + stringOne); 
  Serial.println(duration); 
 }

void loop()
{

  duration = pulseIn(pin, HIGH);
  //String stringOne =  String(duration);
  //Serial.println("pulse is " + stringOne); 
  Serial.println(duration); 
  
  for (int i = 0; i <= 400; i++)
  {
    md.setM1Speed(i);
    md.setM2Speed(i);
    stopIfFault();
    if (i%200 == 100)
    {
      Serial.print("M1 current: ");
      Serial.println(md.getM1CurrentMilliamps());
    }
    //delay(2);
  }
  
}
