/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;


import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.vision.VisionThread;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.*;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;

import java.lang.Math;
import edu.wpi.first.wpilibj.Timer;

import edu.wpi.first.wpilibj.command.TimedCommand;
import edu.wpi.first.wpilibj.command.WaitCommand;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot{
  
   
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  

  // Motor Declarations
  PWMVictorSPX motor_rightRear = new PWMVictorSPX(3);
  PWMVictorSPX motor_rightFront = new PWMVictorSPX(2);
  PWMVictorSPX motor_leftRear = new PWMVictorSPX(0);
  PWMVictorSPX motor_leftFront = new PWMVictorSPX(1);
  Spark motor_panelGrabber = new Spark(7);
  Spark motor_lift = new Spark(6);
  
 

  // Servo Declarations
  Servo cameraServoX = new Servo (4);
  Servo cameraServoY = new Servo (5);
  Servo linearAcServo = new Servo(8);
  
  // Controller Declarations
  Joystick controller = new Joystick(0);
  XboxController controller2 = new XboxController(1);

  // Speed Declarations
	SpeedControllerGroup mRight = new SpeedControllerGroup(motor_rightRear, motor_rightFront);
	SpeedControllerGroup mLeft = new SpeedControllerGroup(motor_leftRear, motor_leftFront);

  // Differential Drive Declaration
	DifferentialDrive myDrive = new DifferentialDrive(mRight, mLeft);

  // Variable Declarations
  private double speedMultiplier = 2.0;
  private int numberOfButton11Presses = 0;
  private double speedAdder = 0.0;
  private final double accelerateIncrement = 0.005;
  private int numberOfAButtonPresses = 0;
  private boolean liftTimerBoolean = true;
  private boolean rightBumperBoolean = false;
  private boolean hatchTimerBoolean = true;
  private boolean leftBumperBoolean = false;
  private int buttonAIdentifier = 0;
  private int test = 0;

    // Declarations for two stick driving on the xbox controller
    private double max_speed_Y = 0.75;
    private double max_speed_X = 0.5;


  // Button Declarations
  private JoystickButton button2 = new JoystickButton(controller, 2);
  private JoystickButton button3 = new JoystickButton(controller, 3);
  private JoystickButton button4 = new JoystickButton(controller, 4);

  //vision declarations
  private static GripPipeline myPipeline = new GripPipeline();
  static double[]centerX;
  static double lengthBetweenContours;

  // Wait Command Declarations
  private Timer liftTimer = new Timer();
  private Timer hatchTimer = new Timer();
  private Timer testTimer = new Timer();
  private Timer testTimer2 = new Timer();

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {      
      UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
      camera.setResolution(640, 480);
      camera.setFPS(15);

    // new stuff
    /*m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    
    CameraServer.getInstance().startAutomaticCapture();*/
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    robotMovement();
    cameraMovement();
    if(controller2.getXButtonPressed()){
      centerCamera();
    }
    //liftMotorControl();
    //hatchMotorControl();
    test();
    test2();
    
    /**
     * if(controller2.getXButton()){
      centerCamera();
      }
     */
    
    //testLinearActuator();
    //breakInMotors();

  }

  public void test2(){
    if(controller2.getRawButton(1)){
      motor_panelGrabber.setSpeed(-.85);
    }
    else if(controller2.getRawButton(4)){
      motor_panelGrabber.setSpeed(.85);
    }
    else{
      motor_panelGrabber.setSpeed(0);
    }
  }

  public void test(){


    if(controller2.getBumperPressed(Hand.kRight)){
      startTimer(testTimer);
      System.out.println("1.1");
      rightBumperBoolean =  true;
    }
    else if(controller2.getBumperPressed(Hand.kLeft)){
      startTimer(testTimer);
      System.out.println("1/2");
      leftBumperBoolean = true;
    }
    

    if(rightBumperBoolean){
      motor_lift.setSpeed(1);
      System.out.println("2");
      System.out.println(testTimer.get());

      if(testTimer.get() > 1.5){
        System.out.println("3");
        motor_lift.stopMotor();
        rightBumperBoolean = false;
        stopTimer(testTimer);
        resetTimer(testTimer);
      }

    }
    else if(leftBumperBoolean){
      motor_lift.setSpeed(.95);

      if(testTimer.get() > 2){
        motor_lift.stopMotor();
        leftBumperBoolean = false;
        stopTimer(testTimer);
        resetTimer(testTimer);
      }
    }
    else{
      motor_lift.stopMotor();
    }





    if(controller.getRawButton(7)){
      linearAcServo.set(1.0);
    }
    if(controller.getRawButton(8)){
      linearAcServo.set(0.2);
    }
  }

  private void startTimer(Timer t){
    t.start();
  }

  private void stopTimer(Timer t){
    t.stop();
  }

  private void resetTimer(Timer t){
    t.reset();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic(){
  }

  
  public void robotMovement(){
 /*
    * when trigger is pressed full speed is enabled (this can be mapped to any button)
    * when trigger is released robot moves at half speed
    */
      //jay is an epic gamer and this was his idea 
      if(button3.get()){
        //hold button 3 to keep robot slow
        speedMultiplier = 2.5;
      }
      else if(button4.get()){
        //hold button 4 to speed up "gradually"
        if(speedAdder < .5) //safety
          speedAdder += 0.005;
      }
      else{
        //default speed
        if(speedAdder > 0)
          speedAdder -= 0.005;
        speedMultiplier = 2;
      }
      myDrive.arcadeDrive(-1 * controller.getY()/speedMultiplier + speedAdder, controller.getAxis(Joystick.AxisType.kTwist)/speedMultiplier);
      //System.out.println(controller.getY()/speedMultiplier+speedAdder);  
  }


  public void liftMotorControl(){  

    if(controller2.getBumperPressed(Hand.kRight)){
      rightBumperBoolean = true;
      System.out.println("1");
    }

    
    if(liftTimerBoolean && rightBumperBoolean){
        liftTimer.start();
        
    }

    if(rightBumperBoolean){
      
      if(liftTimer.get() < 2){
        motor_lift.setSpeed(.7);
        liftTimerBoolean = false;
      }
      else{
        motor_lift.stopMotor();
        liftTimer.reset();
        liftTimerBoolean = true;
        rightBumperBoolean = false;
      }
      
    }

    

  }
  
  public void liftMotorv2(){
    
  }
  public void hatchMotorControl(){
    if(controller2.getAButtonPressed()){
      startTimer(hatchTimer);
      numberOfAButtonPresses += 1;
    }
    if(numberOfAButtonPresses % 2 == 1){
      buttonAIdentifier = 1;
    }
    else if(numberOfAButtonPresses % 2 == 0 && numberOfAButtonPresses != 0){
      buttonAIdentifier = 2;
    }

    if(buttonAIdentifier == 1){
      motor_panelGrabber.setSpeed(-.75);

      if(hatchTimer.get() > .75){
        motor_panelGrabber.stopMotor();
        buttonAIdentifier = 0;
        stopTimer(hatchTimer);
        resetTimer(hatchTimer);
      }
    }
    else if(buttonAIdentifier == 2){
      motor_panelGrabber.setSpeed(.75);

      if(hatchTimer.get() > .75){
        motor_panelGrabber.stopMotor();
        buttonAIdentifier = 0;
        stopTimer(hatchTimer);
        resetTimer(hatchTimer);
      }
    }
    else{
      motor_panelGrabber.stopMotor();
    }
   

  }


  public void moveCameraX(int speed){

    cameraServoX.setAngle(cameraServoX.getAngle()-speed);

  }


  public void moveCameraY(int speed){

    cameraServoY.setAngle(cameraServoY.getAngle()-speed);

  }
  

  public void centerCamera(){

    cameraServoX.setAngle(90);
    cameraServoY.setAngle(55);

  }


  public void cameraMovement(){

      switch(controller.getPOV()){
        //up  
        case 0:
          moveCameraY(2);
          break;
        //up right
        case 45:
          moveCameraX(2);
          moveCameraY(2);
          break;
        //right
        case 90:
          moveCameraX(2);
          break;
        //down right
        case 135:
          moveCameraX(2);
          moveCameraY(-2);
          break;
        //down
        case 180:
          moveCameraY(-3);
          break;
        //down left
        case 225:
          moveCameraX(-2);
          moveCameraY(-2);
          break;  
        //left
        case 270:
          moveCameraX(-3);
          break;        
        //up left
        case 315:
          moveCameraX(-2);
          moveCameraY(2);
          break;
      }    

  }

  public void loadCargo(){


  }

  public void liftHatch(){

  }

/**
 * public void breakInMotors(){

    if(controller.getRawButton(11)){
      numberOfButton11Presses++;
    }

    if(numberOfButton11Presses % 2 == 1){
      myDrive.arcadeDrive(.75,0);
    }
    else{
      myDrive.arcadeDrive(0, 0);
    }

  }
 */
  

}

    