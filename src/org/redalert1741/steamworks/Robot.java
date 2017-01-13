package org.redalert1741.steamworks;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.SPI.Port;

public class Robot extends IterativeRobot
{
	private static AHRS navx;
	
	@Override
	public void robotInit()
	{
		try
		{
			navx = new AHRS(Port.kMXP);
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
		
	}

	@Override
	public void testPeriodic()
	{
		
	}
}

