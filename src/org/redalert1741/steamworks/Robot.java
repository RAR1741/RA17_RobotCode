package org.redalert1741.steamworks;

import org.redalert1741.robotBase.logging.DataLogger;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot
{
	private static LoggableNavX navx;
	private static DataLogger logger;
	private static Timer timer;
	private String auto = "";
	
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
		
	}

	@Override
	public void teleopPeriodic()
	{
		
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

