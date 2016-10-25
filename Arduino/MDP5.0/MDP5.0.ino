#include <SharpIR.h>
#include "DualVNH5019MotorShield.h"
#include "PinChangeInt.h"
#include "RunningMedian.h"
DualVNH5019MotorShield md;

//DEFINING CONSTANT
#define motor_encoder_39 13 //right
#define motor_encoder_22 5 //left
#define sensor_L_pin A1
#define sensor_C_pin A2
#define sensor_R_pin A3
#define sensor_SL_pin A4
#define sensor_SR_pin A5
/*
#define motor_encoder_39 3 //right
#define motor_encoder_22 5 //left
#define sensor_L_pin 2
#define sensor_C_pin 1
#define sensor_R_pin 5
#define sensor_SL_pin 3
#define sensor_SR_pin 4
*/
//#define sensor_Long1_pin 5

#define SENSOR_OFFSET 4
#define dis_from_wall 12

//INITIATING VARIABLE
//model 1080 for GP2Y0A21Y
//model 20150 for GP2Y0A02Y
SharpIR sensor_L(sensor_L_pin,20,100,1080);
SharpIR sensor_C(sensor_C_pin,20,100,1080);
SharpIR sensor_R(sensor_R_pin,20,100,1080);
SharpIR sensor_SL(sensor_SL_pin,25,100,1080);
SharpIR sensor_SR(sensor_SR_pin,25,100,1080);

//RunningMedian sensor_R_median = RunningMedian(19);
//RunningMedian sensor_L_median = RunningMedian(19);


//declaring constant
float encoder_R_value = 0, encoder_L_value = 0;
float target_Tick = 0;
float brake_SpeedL, brake_SpeedR, str8_SpeedL, str8_SpeedR, rotate_SpeedL, rotate_SpeedR, L_offset;
double output;
double error = 0.0, integral = 0.0;
boolean resend = false;
//int prev_C_dis;

String sendStr, sendStr1, sendStr2, sendStr3;

void setup() {
  Serial.begin(9600);
  md.init();
  //Input of Encoder for M1 right
  pinMode(motor_encoder_22, INPUT);
  //Input of Encoder for M2 left
  pinMode(motor_encoder_39, INPUT);

  //initiating speed 
  
  str8_SpeedL = 400;    //250
  str8_SpeedR = 400;    //250
  rotate_SpeedL = 350;    //150
  rotate_SpeedR = 350;    //150
  brake_SpeedL = 400;    //250
  brake_SpeedR = 400;     //388
   /*
  str8_SpeedL = 200;    //250
  str8_SpeedR = 200;    //250
  rotate_SpeedL = 200;    //150
  rotate_SpeedR = 200;    //150
  brake_SpeedL = 200;    //250
  brake_SpeedR = 190;
  */
  PCintPort::attachInterrupt(motor_encoder_39, RightEncoderInc, HIGH);
  PCintPort::attachInterrupt(motor_encoder_22, LeftEncoderInc, HIGH);
}

void loop() {
//FOR TESTING
//  while (1){
//  readSensors();
//  botCalibration();
//  moveForwardMM(600);
//  rotateRight(9000);
//  rotateLeft(9000);
//  alignAngle_without_alignDis();
//  }

  //brake_SpeedR = 388;
  
  char command_buffer[10];
  int i = 0, arg = 0;
  char readChar;
  /*---------------------------------------------------------------------------------------------------
                                 Establishing Serial Connection with RPi
  ---------------------------------------------------------------------------------------------------*/
    while (1){
      if (Serial.available()){
        readChar = Serial.read();
        command_buffer[i] = readChar;
        i++;
        if (readChar == ';'){
          i = 1;
          break;
        }
      }
    }  

//  Serial.println(command_buffer);
  //First character in array is the command
  char command = command_buffer[0];

  //Converts subsequent characters in the array into an integer
  while (command_buffer[i] != ';') {
    arg *= 10;
    arg = arg + (command_buffer[i] - 48);
    i++;
  }

//  command_buffer[1] = ;
  /*----------------------------------------------
  Command Legends:
  ----------------
  //W ---> Move Forward
  F ---> Move Forward in MM (used in Week 12)
  L ---> Rotate Left
  R ---> Rotate Right
  B ---> Break
  M ---> Read Sensor Values (Measurement)
  C ---> Calibrate Bot
  Q ---> Resend reply string
  T ---> Avoiding Obstacle In A Straight Line
 ----------------------------------------------*/
 
  switch ( command ) {
   /* case 'W':
      {
        if (arg == 0) moveForward(1);
        else moveForward(arg);
        sendStr1 = "pW";
        sendStr2 = arg;
        sendStr3 = "done";
        sendStr = sendStr1 + sendStr2 + sendStr3;
        Serial.println(sendStr);
        break;
      }
  */
    case 'F':
      {   
        if (arg == 0) arg = 1;
        moveForward(arg);
        sendStr1 = "F1;";
        sendStr2 = getSensorReading();
        sendStr = sendStr1 + sendStr2;
        Serial.println(sendStr);
        break;
      }
    case 'L':
      {
        if (arg == 0) arg = 9000;   
        rotateLeft(arg);  
        sendStr1 = "L1;";
        sendStr2 = getSensorReading();  
        sendStr = sendStr1 + sendStr2;
        Serial.println(sendStr);
        break;
      }
    case 'R':
      {
        if (arg == 0) arg = 9000;  
        rotateRight(arg);   
        sendStr1 = "R1;";
        sendStr2 = getSensorReading(); 
        sendStr = sendStr1 + sendStr2;
        Serial.println(sendStr);
        break;
      }
    case 'B':
    {
      md.setBrakes(400, 400);
      Serial.println("Braked");
      break;
    }
    case 'M':
      {
        readSensors();
        break;
      }
    case 'C':
      {
        botCalibration();
        sendStr = "C1;";
        Serial.println(sendStr);
        break;
      }

      case 'Q':
      {
        Serial.println(sendStr);
        break;
      }
    default:
      {
        Serial.println("Command Error");
        break;
      }
      memset(command_buffer, 0, sizeof(command_buffer));
  }
  
}

void RightEncoderInc() {
  encoder_R_value++;
}

void LeftEncoderInc() {
  encoder_L_value++;
}

double tuneWithPID()
{
  double drive, last_tick;
  double kp, ki, kd, p, i, d;

  //kp = 45;
  //kp = 25;
  kp = 40;
  ki = 0.1;
  kd = 0.01;

  error = encoder_L_value - encoder_R_value;
  integral += error;

  p = error * kp;
  i = integral * ki;
  d = (last_tick - encoder_L_value) * kd;
  drive = p + i + d;

  last_tick = encoder_L_value;
  
  return drive;
}

void moveForward(int gridNum) {        //temporary treat gridNum as disctance in cm 

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;

  float cmDis = gridNum * 10;

  if (cmDis<= 10) {
    target_Tick = cmDis * 54.8 //54.5 // 49.3 //
    ; //55.4
    brake_SpeedR = 377;   //381
  }
  else if(cmDis<=20) target_Tick = cmDis * 56.6; //52.0;    //done
  else if(cmDis<=30) target_Tick = cmDis * 58;   //55.5;  //done
  else if(cmDis<=40) target_Tick = cmDis * 58.5;  //done
  else if(cmDis<=50) target_Tick = cmDis * 58.7;  //done
  else if(cmDis<=60) target_Tick = cmDis * 58.9;  //done
  else if(cmDis<=70) target_Tick = cmDis * 58.9;  //
  else if(cmDis<=80) target_Tick = cmDis * 59;  //done
  else if(cmDis<=90) target_Tick = cmDis * 59;  //
  else if(cmDis<=100) target_Tick = cmDis * 59;   //
  else if(cmDis<=110) target_Tick = cmDis * 59;
  else if(cmDis<=120) target_Tick = cmDis * 59;   //   max
  else if(cmDis<=130) target_Tick = cmDis * 59;
  else if(cmDis<=140) target_Tick = cmDis * 59;  
  else if(cmDis<=150) target_Tick = cmDis * 59;
  else if(cmDis<=160) target_Tick = cmDis * 59; 
  else if(cmDis<=170) target_Tick = cmDis * 59;  
  else target_Tick = cmDis * 58.9;

    while (encoder_L_value < 200 )
  {
    output = tuneWithPID(); 
    md.setSpeeds(300 + output, 300 - output);
    //md.setSpeeds(150 + output, 150 - output);
  }

  while (encoder_L_value < target_Tick - 200)
  {
    output = tuneWithPID();
    md.setSpeeds(str8_SpeedR + output, str8_SpeedL - output);
  }
  
  while (encoder_L_value < target_Tick)
  {
    output = tuneWithPID();
    //md.setSpeeds(200 + output, 200 - output);
    md.setSpeeds(150 + output, 150 - output);
  }
  md.setBrakes(brake_SpeedR, brake_SpeedL);

  delay(80);
  md.setBrakes(0, 0);
}
/*
void moveForward(int gridNum) {       
  
  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;

  int cmDis = 10 * gridNum;

  if (cmDis<= 10) {
    target_Tick = cmDis * 39; //55.4
    brake_SpeedR = 381;   //381
  }
  else if(cmDis<=20) target_Tick = cmDis * 58.5;    //done
  else if(cmDis<=30) target_Tick = cmDis * 58.5;  //done
  else if(cmDis<=40) target_Tick = cmDis * 59.0;  //done
  else if(cmDis<=50) target_Tick = cmDis * 59.3;  //done
  else if(cmDis<=60) target_Tick = cmDis * 59.3;  //done
  else if(cmDis<=70) target_Tick = cmDis * 59.3;  //done
  else if(cmDis<=80) target_Tick = cmDis * 59.3;  //done
  else if(cmDis<=90) target_Tick = cmDis * 59.3;  //done
  else if(cmDis<=100) target_Tick = cmDis * 59.3;   //
  else if(cmDis<=110) target_Tick = cmDis * 59.3;
  else if(cmDis<=120) target_Tick = cmDis * 59.3;   //done   max
  else if(cmDis<=130) target_Tick = cmDis * 59.3; 
  else if(cmDis<=140) target_Tick = cmDis * 59.3;  
  else if(cmDis<=150) target_Tick = cmDis * 59.3;
  else if(cmDis<=160) target_Tick = cmDis * 59.3; 
  else if(cmDis<=170) target_Tick = cmDis * 59.3;  
  else target_Tick = cmDis * 59.5;

  while (encoder_R_value < 100 )
  {
    output = tuneWithPID();
    md.setSpeeds(100 + output, 100 - output);
  }
  
    while (encoder_R_value < 180 )
  {
    output = tuneWithPID(); 
    md.setSpeeds(200 + output, 200 - output);
  }

 while (encoder_R_value < 260 )
  {
    output = tuneWithPID();
    md.setSpeeds(300 + output, 300 - output);
  }

  while (encoder_R_value < target_Tick - 200)
  {
    output = tuneWithPID();
    md.setSpeeds(str8_SpeedL + output, str8_SpeedR - output);
  }
  
  while (encoder_R_value < target_Tick)
  {
    output = tuneWithPID();
    md.setSpeeds(200 + output, 200 - output);
  }

  md.setBrakes(brake_SpeedL, brake_SpeedR);
  
  delay(80);
  md.setBrakes(0, 0);
}
*/
void moveBackward(int gridNum) {   

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;

  int cmDis = 10 * gridNum;
  
  target_Tick = cmDis * 60 - 127;

  while (encoder_L_value < 150 )
    {
      output = tuneWithPID();
      md.setSpeeds(-(100 + output), -(100 - output));
    }
  
    while (encoder_L_value < 350 )
  {
    output = tuneWithPID();
    md.setSpeeds(-(200 + output), -(200 - output));
  }

 while (encoder_L_value < 600 )
  {
    output = tuneWithPID();
    md.setSpeeds(-(250 + output), -(250 - output));
  }

  while (encoder_L_value < target_Tick )
  {
    output = tuneWithPID();
    md.setSpeeds(-(str8_SpeedL + output), -(str8_SpeedR - output));
  }
  
  md.setBrakes(400, 400);
}

int rotateLeft(int angle) {

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;
  angle = angle/100;
  
  if (angle <= 5) target_Tick = angle * 5.5;//5.2
  else if (angle <= 10) target_Tick = angle * 6.3;
  else if (angle <= 15) target_Tick = angle * 6.4;
  else if (angle <= 30) target_Tick = angle * 7.7; //7.72
  else if (angle <= 45) target_Tick = angle * 8.01; //8.635
  else if (angle <= 60) target_Tick = angle * 8.3;
  else if (angle <= 90) target_Tick = angle * 8.66;//47; //8.65
  else if (angle <=180 ) target_Tick = angle * 9.75;    //tune 180
  else if (angle <=360 ) target_Tick = angle * 9.37;
  else if (angle <= 720) target_Tick = angle * 9.15;
  else if (angle <= 900) target_Tick = angle * 9.16;
  else if (angle <= 1080) target_Tick = angle * 9.06;
  else target_Tick = angle * 9.0;

  while (encoder_L_value < target_Tick*0.2 )
  {
    output = tuneWithPID();
    md.setSpeeds(150 + output, -(150 - output));
  }

  while (encoder_L_value < target_Tick*0.7) 
  {
    output = tuneWithPID();
    md.setSpeeds(rotate_SpeedR + output, -(rotate_SpeedL - output));
  }
  
  while (encoder_L_value < target_Tick) 
  {
    output = tuneWithPID();
    md.setSpeeds(150 + output, -(150 - output));
  }
  
  md.setBrakes(400, 400); 
  delay(80);
  md.setBrakes(0, 0);
}

int rotateRight(int angle) {
  
  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;
  angle = angle/100;

  if (angle <= 5) target_Tick = angle * 5.7;//5.2
  else if (angle <= 10) target_Tick = angle * 6.3;
  else if (angle <= 15) target_Tick = angle * 6.4;
  else if (angle <= 30) target_Tick = angle * 7.7; //7.72
  else if (angle <= 45) target_Tick = angle * 8.01; //8.635
  else if (angle <= 60) target_Tick = angle * 8.3;
  else if (angle <= 90) target_Tick = angle * 8.464;//7; //8.643 //8.61
  else if (angle <=180 ) target_Tick = angle * 9.75;    //tune 180
  else if (angle <=360 ) target_Tick = angle * 9.37;
  else if (angle <= 720) target_Tick = angle * 9.15;
  else if (angle <= 900) target_Tick = angle * 9.16;
  else if (angle <= 1080) target_Tick = angle * 9.06;
  else target_Tick = angle * 9.0;
  

  while (encoder_L_value < target_Tick*0.2  )
  {
    output = tuneWithPID();
    md.setSpeeds(-(150 + output), 150 - output);
  }
  
  while (encoder_L_value < target_Tick*0.7) {
    output = tuneWithPID();
    md.setSpeeds(-(rotate_SpeedL + output), rotate_SpeedR - output);
  }

  while (encoder_L_value < target_Tick) {
    output = tuneWithPID();
    md.setSpeeds(-(150 + output), 150 - output);
  }
  md.setBrakes(400,400);
  delay(80);
  md.setBrakes(0, 0);
}

void moveForward_without_ramp(double cmDis) {        //temporary treat gridNum as disctance in cm 

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;
  
//  target_Tick = cmDis * 60 - 127;
  target_Tick = cmDis * 55.2;

  while (encoder_L_value < target_Tick )
  {
    output = tuneWithPID();
//    delay(3);
    md.setSpeeds(100 + output, 100 - output);
  }
  
  md.setBrakes(400, 400); //400, 385
  
  delay(10);
  //md.setBrakes(0, 0);
}

void moveBackward_without_ramp(double cmDis) {        //temporary treat gridNum as disctance in cm 

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;
  
//  target_Tick = cmDis * 60 - 127;
  target_Tick = cmDis * 55.2;

  while (encoder_L_value < target_Tick )
  {
    output = tuneWithPID();
//    delay(3);
    md.setSpeeds(-(100 + output), -(100 - output));
  }
  
  md.setBrakes(400, 385);

  delay(10);

}

int rotateLeft_without_ramp(double angle) {

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;

  if (angle <= 90) target_Tick = angle * 8.645; //8.635
  else if (angle <=180 ) target_Tick = angle * 9.75;    //tune 180
  else if (angle <=360 ) target_Tick = angle * 9.37;
  else if (angle <= 720) target_Tick = angle * 9.15;
  else if (angle <= 900) target_Tick = angle * 9.16;
  else if (angle <= 1080) target_Tick = angle * 9.06;
  else target_Tick = angle * 9.0;
  
  while (encoder_L_value < target_Tick ) 
  {
    output = tuneWithPID();
    md.setSpeeds(100 + output, -(100 - output));
  }
  md.setBrakes(400, 400); 
  delay(10);
  md.setBrakes(0, 0);
}

int rotateRight_without_ramp(double angle) {

  encoder_R_value = 0;
  encoder_L_value = 0;
  int output = 0;
  error = 0;
  integral = 0;

  //90 - 8.8
  //360 - 8.96
  
  //target_Tick = angle * 8.78;
  //target_Tick = angle * 8.6;
  target_Tick = angle * 5.2;

  while (encoder_L_value < target_Tick) 
  {
    //HACK: I don't know what's the problem. But this way fixed it.
    //But we can't do this because the String will be sent to the rPi.
    Serial.print("");
    output = tuneWithPID();
    md.setSpeeds(-(100 + output), 100 - output);
  }

  /*while (encoder_L_value < target_Tick*0.2  )
  {
    output = tuneWithPID();
    md.setSpeeds(-(100 + output), 100 - output);
  }*/
  md.setBrakes(400,400);
  delay(10);
  md.setBrakes(0, 0);

}

void readSensors()
{
//  FOR DEBUGGING
//  Serial.print(":");
//  Serial.print(getMedianDistance(sensor_L) + SENSOR_OFFSET);
//  Serial.print(":");
//  Serial.print(getMedianDistance(sensor_C) + SENSOR_OFFSET);
//  Serial.print(":");
//  Serial.print(getMedianDistance(sensor_R) + SENSOR_OFFSET);
//  Serial.print(":");
//  Serial.print(getMedianDistance(sensor_SL) + SENSOR_OFFSET);
//  Serial.print(":");
//  Serial.print(getMedianDistance(sensor_SR) + SENSOR_OFFSET);
//  Serial.print(":");
//  Serial.print("\n");
  
//Serial.print("p");
  Serial.print("M");
  Serial.print(gridsAwayL(getMedianDistance(sensor_L) + SENSOR_OFFSET));
  Serial.print(";");
  Serial.print(gridsAwayC(getMedianDistance(sensor_C) + SENSOR_OFFSET));
  Serial.print(";");
  Serial.print(gridsAwayR(getMedianDistance(sensor_R) + SENSOR_OFFSET));
  Serial.print(";");
  Serial.print(gridsAwaySL(getMedianDistance(sensor_SL) + SENSOR_OFFSET));
  Serial.print(";");
  Serial.print(gridsAwaySR(getMedianDistance(sensor_SR) + SENSOR_OFFSET));
  Serial.print(";");
  Serial.print("\n");

}

String getSensorReading()
{
  String result = "M";
  result += gridsAwayL(getMedianDistance(sensor_L) + SENSOR_OFFSET);
  result += ";";
  result += gridsAwayC(getMedianDistance(sensor_C) + SENSOR_OFFSET);
  result += ";";
  result += gridsAwayR(getMedianDistance(sensor_R) + SENSOR_OFFSET);
  result += ";";
  result += gridsAwaySL(getMedianDistance(sensor_SL) + SENSOR_OFFSET);
  result += ";";
  result += gridsAwaySR(getMedianDistance(sensor_SR) + SENSOR_OFFSET);
  result += ";";
  return result;
}



int calDistance(SharpIR sensor)
{
  return (sensor.distance());
}

int getMedianDistance(SharpIR sensor)
{
  RunningMedian sample = RunningMedian(9);
  for (int i = 0; i < 9; i ++)
  {
    sample.add(calDistance(sensor)); 
  }
  return sample.getMedian();
}

int gridsAwayL(int dis)
{
  if (abs(dis) < 17) return 0;
  else if (abs(dis) < 27) return 1;
  else if (abs(dis) < 39) return 2;
  else return 3;
}

int gridsAwayC(int dis)
{
  if (abs(dis) < 17) return 0;
  else if (abs(dis) < 27) return 1;
  else if (abs(dis) < 39) return 2;    //44
  else return 3;
}

int gridsAwayR(int dis)
{
  if (abs(dis) < 17) return 0;
  else if (abs(dis) < 27) return 1;
  else if (abs(dis) < 39) return 2;
  else return 3;
}

int gridsAwaySL(int dis)
{
  if (abs(dis) < 16) return 0;
  else if (abs(dis) < 25) return 1;   //31
  else if (abs(dis) < 40) return 2;
  else return 3;
}

int gridsAwaySR(int dis)
{
  if (abs(dis) < 16) return 0;          //18
  else if (abs(dis) < 26) return 1;     //27  //37
  else if (abs(dis) < 40) return 2;     //40  //54
  else return 3;
}

void botCalibration()
{
  closeAvoidance();
  alignAngle();
  alignAngle_without_alignDis();
  moveForward_without_ramp(1.5);  //1.3
}

int getSteadyValue(int pin)
{
  RunningMedian sample = RunningMedian(200);
  sample.add(analogRead(pin)); 
  return sample.getAverage(10);
}

void closeAvoidance()
{
  int first_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET;
  int first_L_dis = getMedianDistance(sensor_L) + SENSOR_OFFSET;
  if ( (first_L_dis > 14) || (first_R_dis > 14) ) moveBackward_without_ramp(2);
}

void alignAngle(){
  int sensor_L_dis, sensor_R_dis;
  int first_R_dis, first_L_dis;
  first_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET;
  first_L_dis = getMedianDistance(sensor_L) + SENSOR_OFFSET;
  while(1)
  {  
    sensor_L_dis = getMedianDistance(sensor_L) + SENSOR_OFFSET;
    sensor_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET;
    int error = sensor_L_dis - sensor_R_dis;

    if (error >= 1){
      rotateRight_without_ramp(0.5);
    }
    else if (error <= -1){
      rotateLeft_without_ramp(0.5);
    }
    else break;
  }

  if (first_L_dis < first_R_dis){
    rotateLeft_without_ramp(2);
  }
  
  sensor_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET;
  if (sensor_R_dis > dis_from_wall || sensor_R_dis < dis_from_wall) {
    alignDistance();
  }
}

void alignAngle_without_alignDis()
{
  int sensor_L_dis, sensor_R_dis;
  while(1)
  {  
    sensor_L_dis = getMedianDistance(sensor_L) + SENSOR_OFFSET;
    sensor_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET;
    int error = sensor_L_dis - sensor_R_dis;
    
    if (error >= 1){
      //Serial.println("Right");
      rotateRight_without_ramp(0.5);
    }
    else if (error <= -1){
      //Serial.println("Left");
      rotateLeft_without_ramp(0.5);
    }
    else break;
  }
}

void alignDistance(){
  while(1){
    int sensor_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET +1;
    md.setSpeeds(80,80);
    if (sensor_R_dis > dis_from_wall) moveForward_without_ramp(0.8);
    else if (sensor_R_dis < dis_from_wall) moveBackward_without_ramp(0.8);
    else break;
  }
  md.setBrakes(400,400);

  while(1){
    int sensor_L_dis = getMedianDistance(sensor_L) + SENSOR_OFFSET;
    md.setSpeeds(80,80);
    if (sensor_L_dis > dis_from_wall) moveForward_without_ramp(0.8);
    else if (sensor_L_dis < dis_from_wall) moveBackward_without_ramp(0.8);
    else break;
  }
  md.setBrakes(400,400);
 
  int sensor_L_dis = getMedianDistance(sensor_L) + SENSOR_OFFSET;
  int sensor_R_dis = getMedianDistance(sensor_R) + SENSOR_OFFSET;
  int error = sensor_L_dis - sensor_R_dis; 
  if (error >= 1 || error <= -1) {
  alignAngle();
  }

}

