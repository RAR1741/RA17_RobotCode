package org.redalert1741.steamworks;

import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import edu.wpi.first.wpilibj.Spark;

public class Climber implements Loggable
{
	private Spark climber1;
	private Spark climber2;

	public Climber(int c1, int c2)
	{
		climber1 = new Spark(c1);
		climber2 = new Spark(c2);
	}
	
	@Override
	public void setupLogging(DataLogger logger)
	{
		logger.addAttribute("climber1_Setpoint");
		logger.addAttribute("climber2_Setpoint");
	}

	@Override
	public void log(DataLogger logger)
	{
		logger.log("climber1_Setpoint", climber1.get());
		logger.log("climber2_Setpoint", climber2.get());
	}

}
