package org.redalert1741.steamworks;

import java.io.File;
import java.util.Calendar;

import org.redalert1741.robotBase.logging.*;
import org.redalert1741.robotBase.config.*;
import org.redalert1741.robotBase.input.*;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.SPI.Port;

public class Robot extends IterativeRobot
{
	public static LoggableNavX navx;
	private static DataLogger logger;
	private static Timer timer;
	private static PowerDistributionPanel pdp;
	private static Solenoid redLED;
	private static Solenoid whiteLED;
	@SuppressWarnings("unused")
	private String auto = "";
	
	private double[][] maxEncValue = new double[4][2];
	
	public static SwerveDrive drive;
	public static Climber climber;
	public static GearPlacer gear;
	public static Manipulation manip;
	public static Shooter shooter;
	public static Carousel carousel;
	
	private static XboxController driver;
	private static EdgeDetect driveMode;
	private static EdgeDetect collection;
	
	private static CANTalon FR;
	private static CANTalon FRa;
	private static CANTalon FL;
	private static CANTalon FLa;
	private static CANTalon BR;
	private static CANTalon BRa;
	private static CANTalon BL;
	private static CANTalon BLa;
	private static AnalogInput FRe;
	private static AnalogInput FLe;
	private static AnalogInput BRe;
	private static AnalogInput BLe;
	
	private static PIDController driveAimer;
	private static FakePIDSource cameraSource;
	private static FakePIDOutput driveOutput;
	
	private double x;
	private double y;
	private double twist;
//	private double autoAimOffset;
	private boolean fieldOrient = true;
	private boolean collect = true;
//	private boolean configReload;
	private JsonAutonomous auton;
	private ScopeToggler scopeToggler;
	
	@Override
	public void robotInit()
	{
		timer = new Timer();
		logger = new DataLogger();
		pdp = new PowerDistributionPanel(20);
		redLED = new Solenoid(0);
		whiteLED = new Solenoid(1);
		scopeToggler = new ScopeToggler(0,1);
		Config.loadFromFile("/home/lvuser/config.txt");
		////////////////////////////////////////////////
		try
		{
			navx = new LoggableNavX(Port.kMXP);
		}
		catch (RuntimeException ex )
		{
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
		}
		////////////////////////////////////////////////
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
		collection = new EdgeDetect();
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
		////////////////////////////////////////////////
		climber = new Climber(0, 1);
		////////////////////////////////////////////////
		gear = new GearPlacer(2);
		Config.addConfigurable(gear);
		////////////////////////////////////////////////
		manip = new Manipulation(3,4,22);
		
		shooter = new Shooter(new CANTalon(10));
		Config.addConfigurable(shooter);
		
		carousel = new Carousel(new CANTalon(9));
		Config.addConfigurable(carousel);
		
		ReloadConfig();
	}
//========================================================================================================
	@Override
	public void autonomousInit()
	{
		setupPeriodic("auto");
		drive.angleToZero();
		auton = new JsonAutonomous("/home/lvuser/auto-test.json");
		System.gc();
	}

	@Override
	public void autonomousPeriodic()
	{
//    	log(timer.get());
//		if(timer.get() >= 1 && timer.get() <= 5)
//		{
//			drive.swerveAbsolute(0, -.4, 0, 0, false);
//		}
//		else
//		{
//			drive.swerveAbsolute(0, -.001, 0, 0, false);
//		}
		auton.run();
	}
//========================================================================================================
	@Override
    public void teleopInit()
    { setupPeriodic("teleop")
	; navx.reset();
	; collect = false;
	; System.gc();
    ; }

	@Override
	public void teleopPeriodic()
	{
		redLED.set(true);
		whiteLED.set(true);
    	///////////////////////////////////////////////////////////////////////////
    	//Utility
		scopeToggler.startLoop(); // Must be first line in periodic
    	log(timer.get());
    	if(driver.getBackButton())
    	{
    		ReloadConfig();
    	}
    	///////////////////////////////////////////////////////////////////////////
    	//Drive
    	x = driver.getX(Hand.kLeft);
    	y = driver.getY(Hand.kLeft);
    	twist = driver.getX(Hand.kRight);
    	
    	if(x >= -0.05 && x <= 0.05){x=0;}
    	else if(!(driver.getTriggerAxis(Hand.kLeft) > 0.5)) { x=0.5*x; }
    	if(y >= -0.05 && y <= 0.05){y=0;}
    	else if(!(driver.getTriggerAxis(Hand.kLeft) > 0.5)) { y=0.5*y; }
    	if(twist >= -0.05 && twist <= 0.05){twist=0;}
    	else if(!(driver.getTriggerAxis(Hand.kLeft) > 0.5)) { twist=0.5*twist; }
    	else { twist=0.8*twist; }
    	if(driveMode.Check(driver.getStartButton()))
    	{
    		fieldOrient = !fieldOrient;
    	}
    	drive.swerve(-x,-y,-twist,-navx.getAngle(),fieldOrient);
    	///////////////////////////////////////////////////////////////////////////
    	//Climber
    	if(driver.getTriggerAxis(Hand.kRight) > 0.1)
    	{
    		climber.climb(driver.getTriggerAxis(Hand.kRight));
    	}
    	else
    	{
    		climber.climb(0);
    	}
    	///////////////////////////////////////////////////////////////////////////
    	//Gear
    	if(driver.getBumper(Hand.kLeft))
    	{
    		gear.close();
    	}
    	else if(driver.getBumper(Hand.kRight))
    	{
    		gear.open();
    	}
    	else
    	{
 //   		gear.stop();
    	}
    	///////////////////////////////////////////////////////////////////////////
    	//Manipulation
    	if(collection.Check(driver.getXButton()))
    	{
    		collect = !collect;
    	}
    	
    	if(collect)
    	{
    		manip.setInput(0.7, -0.6);
    	}
    	else
    	{
    		manip.setInput(0, 0);
    	}
    	
    	if(driver.getPOV() == 0)
    	{
    		carousel.forward();
    	}
    	else if(driver.getPOV() == 180)
    	{
    		carousel.reverse();
    	}
    	else
    	{
    		carousel.stop();
    	}
    	///////////////////////////////////////////////////////////////////////////
    	//Shooter
    	if(driver.getAButton())
    	{
    		shooter.setSpeed(-2600);
    	}
    	else
    	{
    		shooter.stop();
    	}
    	
    	scopeToggler.endLoop();
	}
//========================================================================================================
	@Override
	public void testInit()
	{
		setupPeriodic("test");
		System.gc();
	}

	@Override
	public void testPeriodic()
	{
		log(timer.get());
    	drive.swerve(0,0,0,0,fieldOrient);
    	if(driver.getBackButton())
    	{
    		ReloadConfig();
    	}
    	
    	if(driver.getStartButton())
    	{
    		maxEncValue = drive.calibrateAngle();
    		for(double[] x: maxEncValue)
    		{
    			System.out.println("Min: " + x[0] + "\tMax: " + x[1]);
    		}
    	}
	}
	
	@Override
	public void disabledInit()
	{
		System.gc();
	}
//========================================================================================================
	public void setupPeriodic(String period)
	{
		timer.reset();
		timer.start();
		ReloadConfig();
		startLogging(period,logger);
		setupLogging();
	}
	
	void startLogging(String mode, DataLogger l)
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

	void setupLogging()
	{
		logger.addAttribute("Time");
		logger.addAttribute("ClimberA1");
		logger.addAttribute("ClimberA2");
		logger.addLoggable(drive);
		logger.addLoggable(navx);
		logger.addLoggable(gear);
		logger.addLoggable(shooter);
		logger.addLoggable(carousel);
		logger.setupLoggables();
		logger.writeAttributes();
	}
	
	void log(double time)
	{
		logger.log("Time", time);
		logger.log("ClimberA1", pdp.getCurrent(15));
		logger.log("ClimberA2", pdp.getCurrent(14));
		logger.log();
		logger.writeLine();
	}
	
	void ReloadConfig()
	{
		Config.loadFromFile("/home/lvuser/config.txt");
		Config.reloadConfig();
		//autoAimOffset = Config.getSetting("autoAimOffest", 0);
		drive.ReloadConfig();
	}
}

