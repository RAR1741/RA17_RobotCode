package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

public class Carousel implements Configurable, Loggable
{
	private CANTalon motor;
	private double forwardSpeed;
	
	public Carousel(CANTalon s)
	{
		motor = s;
		//TODO should encoder
		motor.changeControlMode(TalonControlMode.PercentVbus);
	}
	
	public void forward()
	{
		motor.set(forwardSpeed);
	}
	
	public void stop()
	{
		motor.set(0);
	}
	
	public void reverse()
	{
		motor.set(-forwardSpeed);
	}

	@Override
	public void setupLogging(DataLogger logger)
	{
		logger.addAttribute("CarouselSetpoint");
		logger.addAttribute("CarouselCurrent");
	}

	@Override
	public void log(DataLogger logger)
	{
		logger.log("CarouselSetpoint", motor.getSetpoint());
		logger.log("CarouselCurrent", motor.getOutputCurrent());
	}

	@Override
	public void reloadConfig()
	{
		forwardSpeed = Config.getSetting("carouselSpeed", 0.7);
	}

}
