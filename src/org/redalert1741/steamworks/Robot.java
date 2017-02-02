package org.redalert1741.steamworks;

import java.io.File;
import java.util.Calendar;

import org.redalert1741.robotBase.logging.*;
import org.redalert1741.robotBase.config.*;
import org.redalert1741.robotBase.input.*;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.GenericHID.Hand;

public class Robot extends IterativeRobot
{
	//private static LoggableNavX navx;
	private static DataLogger logger;
	private static Timer timer;
	@SuppressWarnings("unused")
	private String auto = "";
	
	private double[] maxEncValue = new double[4];
	
	SwerveDrive drive;
	XboxController driver;
	EdgeDetect driveMode;
	
	CANTalon FR;
	CANTalon FRa;
	CANTalon FL;
	CANTalon FLa;
	CANTalon BR;
	CANTalon BRa;
	CANTalon BL;
	CANTalon BLa;
	AnalogInput FRe;
	AnalogInput FLe;
	AnalogInput BRe;
	AnalogInput BLe;
	
	PIDController driveAimer;
	FakePIDSource cameraSource;
	FakePIDOutput driveOutput;
	
	double x;
	double y;
	double twist;
	double autoAimOffset;
	boolean fieldOrient = true;
	boolean configReload;
	
	@Override
	public void robotInit()
	{
		timer = new Timer();
		logger = new DataLogger();
		Config.loadFromFile("/home/lvuser/config.txt");
//		try
//		{
//			navx = new LoggableNavX(Port.kMXP);
//      }
//		catch (RuntimeException ex )
//		{
//            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
//      }
		FRe = new AnalogInput(0);
		FLe = new AnalogInput(2);
		BRe = new AnalogInput(3);
		BLe = new AnalogInput(1);
	   	FR = new CANTalon(1);
    	FRa = new CANTalon(5);
    	FL = new CANTalon(3);
    	FLa = new CANTalon(7);
    	BR = new CANTalon(4);
    	BRa = new CANTalon(8);
    	BL = new CANTalon(2);
    	BLa = new CANTalon(6);
		drive = new SwerveDrive(FR, FRa, FRe, FL, FLa, FLe, BR, BRa, BRe, BL, BLa, BLe);
		////////////////////////////////////////////////
		driver = new XboxController(4);
		////////////////////////////////////////////////
		driveMode = new EdgeDetect();
		////////////////////////////////////////////////
		cameraSource = new FakePIDSource();
		driveOutput = new FakePIDOutput();
		driveAimer = new PIDController(Config.getSetting("AutoAimP", 0.12),
									   Config.getSetting("AutoAimI", 0.00),
									   Config.getSetting("AutoAimD", 0.00),
									   cameraSource,
									   driveOutput);
		driveAimer.setInputRange(-24,24);
		driveAimer.setOutputRange(-.3,.3);
		driveAimer.setAbsoluteTolerance(.5);
	}

	@Override
	public void autonomousInit()
	{
		
	}

	@Override
	public void autonomousPeriodic()
	{
		
	}

	@Override
    public void teleopInit()
    { StartLogging("teleop",logger)
    ; SetupLogging()
    ; ReloadConfig()
    ; timer.reset()
    ; timer.start()
    ; }

	@Override
	public void teleopPeriodic()
	{
    	x = driver.getX(Hand.kLeft);
    	y = driver.getY(Hand.kLeft);
    	twist = driver.getX(Hand.kRight);
    	
    	if(x >= -0.1 && x <= 0.1){x=0;}
    	else if(!driver.getBumper(Hand.kRight)) { x=0.6*x; }
    	if(y >= -0.1 && y <= 0.1){y=0;}
    	else if(!driver.getBumper(Hand.kRight)) { y=0.6*y; }
    	if(twist >= -0.1 && twist <= 0.1){twist=0;}
    	else if(!driver.getBumper(Hand.kRight)) { twist=0.6*twist; }
    	else { twist=0.8*twist; }
    	if(driveMode.Check(driver.getStartButton()))
    	{
    		fieldOrient = !fieldOrient;
    	}
    	
    	drive.Swerve(x,y,twist,0,fieldOrient);
    	ReloadConfig();
	}

	@Override
	public void testInit()
	{
		timer.reset();
		timer.start();
		logger.open("/home/lvuser/navxTest.log");
		//logger.addLoggable(navx);
		logger.setupLoggables();
		logger.writeAttributes();
	}

	@Override
	public void testPeriodic()
	{
		logger.log();
		logger.writeLine();
    	drive.Swerve(0,0.3,0,0,fieldOrient);
    	if(driver.getBackButton())
    	{
    		ReloadConfig();
    	}
    	
    	if(driver.getStartButton())
    	{
    		maxEncValue = drive.calibrateAngle();
    		for(double x: maxEncValue)
    		{
    			System.out.println(x);
    		}
    	}
	}
	
	void StartLogging(String mode, DataLogger l)
	{
		String robot = !(Config.getSetting("isPrototype", 0) == 0) ? "_proto" : "_comp";
		l.close();
		Calendar calendar = Calendar.getInstance();
		String dir = "/home/lvuser";
		if(new File("/media/sda").exists())
		{
			dir = "/media/sda";
		}
		String name = dir + "/log-" + calendar.get(Calendar.YEAR) + "-" +
				calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "_" +
				calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE) + "-" +
				calendar.get(Calendar.SECOND) + "_" + mode + robot + ".csv";
		System.out.println(name);
		l.open(name);
	}

	void SetupLogging()
	{
		logger.addLoggable(drive);
		//logger.addLoggable(navx);
		logger.addAttribute("Time");
		logger.addAttribute("AccX");
		logger.addAttribute("AccY");
		logger.addAttribute("AccZ");
		//drive.setupLogging(logger);
		logger.writeAttributes();
	}
	
	void ReloadConfig()
	{
		Config.loadFromFile("/home/lvuser/config.txt");
		autoAimOffset = Config.getSetting("autoAimOffest", 0);
		drive.ReloadConfig();
	}
}

