package org.redalert1741.steamworks;

import org.redalert1741.robotBase.logging.DataLogger;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Relay.Value;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot
{
	private static LoggableNavX navx;
	private static DataLogger logger;
	private static Timer timer;
	private String auto = "";
	
	Vision targeting;
	Relay light;
	
	@Override
	public void robotInit()
	{
		timer = new Timer();
		logger = new DataLogger();
		try
		{
			navx = new LoggableNavX(Port.kMXP);
        }
		catch (RuntimeException ex )
		{
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
        }
		try{
		targeting = new Vision();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		light = new Relay(0);
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
	{
		//targeting.startCameraStream();
		light.set(Value.kForward);
	}
	
	@Override
	public void disabledInit()
	{
		light.set(Value.kOff);
	}

	@Override
	public void teleopPeriodic()
	{
		light.set(Value.kForward);
	}

	@Override
	public void testInit()
	{
		timer.reset();
		timer.start();
		logger.open("/home/lvuser/navxTest.log");
		logger.addLoggable(navx);
		logger.setupLoggables();
		logger.writeAttributes();
	}

	@Override
	public void testPeriodic()
	{
		logger.log();
		logger.writeLine();
	}
}

