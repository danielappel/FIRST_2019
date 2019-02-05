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

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import frc.robot.grip.GripPipeline;
import org.opencv.core.Rect;
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
  
  private static GripPipeline myPipeline = new GripPipeline();
  // Motor Declarations
  PWMVictorSPX motor_rightRear = new PWMVictorSPX(3);
  PWMVictorSPX motor_rightFront = new PWMVictorSPX(2);
  PWMVictorSPX motor_leftRear = new PWMVictorSPX(0);
  PWMVictorSPX motor_leftFront = new PWMVictorSPX(1);
  Spark motor_panelGrabber = new Spark(6);
  Spark motor_lift = new Spark(7);
 

  // Servo Declarations
  Servo cameraServoX = new Servo (4);
  Servo cameraServoY = new Servo (5);
  
  // Controller Declarations
  Joystick controller = new Joystick(0);
  XboxController controller2 = new XboxController(1);
  int currentController = 1;

  // Speed Declarations
	SpeedControllerGroup mRight = new SpeedControllerGroup(motor_rightRear, motor_rightFront);
	SpeedControllerGroup mLeft = new SpeedControllerGroup(motor_leftRear, motor_leftFront);

  // Differential Drive Declaration
	DifferentialDrive myDrive = new DifferentialDrive(mRight, mLeft);

  // Variable Declarations
  private double speedMultiplier = 2.0;
  private int numberOfButton11Presses = 0;
  private double speedAdder = 0.0;
    // Declarations for two stick driving on the xbox controller
    private double max_speed_Y = 0.75;
    private double max_speed_X = 0.5;


  // Button Declarations
  private JoystickButton button2 = new JoystickButton(controller, 2);
  private JoystickButton button3 = new JoystickButton(controller, 3);
  private JoystickButton button4 = new JoystickButton(controller, 4);

  // Hand Declarations
  public static final GenericHID.Hand kRight;
  public static final GenericHID.Hand kLeft;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    new Thread(() -> {      
      UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
      camera.setResolution(640, 480);
      
      //Get OpenCV access to the primary camera feed.
      CvSink cvSink = CameraServer.getInstance().getVideo();
      //Create a MJPEG stream with OpenCV input.
      CvSource outputStream = CameraServer.getInstance().putVideo("Blur", 640, 480);
      
      //object representing image(maybe)
     Mat source = new Mat();
     Mat output = new Mat();
   
      while(!Thread.interrupted()) {
        //Wait for the next frame and get the image.
        cvSink.grabFrame(source);
        //converts image to gray
        Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
        //Put an OpenCV image and notify sinks.
        outputStream.putFrame(output);
    }
}).start();

  
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
    topMotorControl();
    //breakInMotors();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic(){
  }
  
  public void visionCode(){
    //Rect r = Imgproc.boundingRect(myPipeline.filterContoursOutput.get(0));
  }
  public void robotMovement(){
    /*
    * when trigger is pressed full speed is enabled (this can be mapped to any button)
    * when trigger is released robot moves at half speed
    */
    if(currentController == 1){
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
      System.out.println(controller.getY()/speedMultiplier+speedAdder);
    }
    else if(currentController == 2){
      myDrive.arcadeDrive(controller2.getY(kLeft) * max_speed_Y, controller2.getX(kRight) * max_speed_X);
    }

  }


  public void topMotorControl(){
    if(currentController == 1){
      if(controller.getRawButton(2)){
        motor_panelGrabber.setSpeed(controller.getY());
      }
    }
      
    else if(currentController == 2){
      if(controller2.getTriggerAxis(kRight) != 0){
        motor_panelGrabber.setSpeed(controller2.getTriggerAxis(kRight));
      }
      else if(controller2.getTriggerAxis(kLeft) != 0){
        motor_panelGrabber.setSpeed(-1 * controller2.getTriggerAxis(kLeft));
      }
      else{
        motor_panelGrabber.setSpeed(0);
      }
    }
  }


  public void moveCameraX(int speed){
    cameraServoX.setAngle(cameraServoX.getAngle()+speed);
  }
  public void moveCameraY(int speed){
    cameraServoY.setAngle(cameraServoY.getAngle()+speed);
  }

  public void cameraMovement(){

    if(currentController == 1){
      //right
      if(controller.getPOV()==90){
        moveCameraX(3);
      }
      //left
      else if(controller.getPOV()==270){
        moveCameraX(-3);    
      }
      //up
      if(controller.getPOV()==0){
        moveCameraY(3);
      }
      //down
      else if(controller.getPOV()==180){
        moveCameraY(-3);
      }  
      //up right
      if(controller.getPOV()==45){
        moveCameraX(2);
        moveCameraY(2);
      }
      //down right
      else if(controller.getPOV()==135){
        moveCameraX(2);
        moveCameraY(-2);
      }
      //down left
      else if(controller.getPOV()==225){
        moveCameraX(-2);
        moveCameraY(-2);
      }
      //up left
      else if(controller.getPOV()==315){
        moveCameraX(-2);
        moveCameraY(2);
      }
    }
    else if(currentController == 2){

    }
    
  }


public void breakInMotors(){
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
}

 