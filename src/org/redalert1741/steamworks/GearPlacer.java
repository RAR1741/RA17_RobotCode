package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;
import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Spark;

public class GearPlacer implements Loggable,Configurable
{
	Spark M;
	GearPlacerState state;
	double motorSpeedOpen;
	double motorSpeedClose;

	
	public GearPlacer(int m)
	{
		state = GearPlacerState.Waiting;
		M = new Spark(m);
		motorSpeedOpen = Config.getSetting("gearMotorSpeedOpen", .4);
		motorSpeedClose = Config.getSetting("gearMotorSpeedClose", 0.25);
	}
	
	public enum GearPlacerState
	{
		Opening("Opening"),Closing("Closing"),Waiting("Waiting");
		
		private String val;
		
		GearPlacerState(String v) { val = v; }
		
		@Override
		public String toString() { return val; }
	}
//	
//	public void update()
//	{
//		switch(state)
//		{
//		case Waiting:
//			M.set(0);
//			if(open)
//			{
//				open = false;
//				state = State.Opening;
//			}
//			else if(close)
//			{
//				close = false;
//				state = State.Closing;
//			}
//			break;
//		case Opening:
//			if(M.isRevLimitSwitchClosed())
//			{
//				M.set(0);
//				state = State.Waiting;
//			}
//			else
//			{
//				M.set(-0.5);
//			}
//			break;
//		case Closing:
//			if(M.isFwdLimitSwitchClosed())
//			{
//				M.set(0);
//				state = State.Waiting;
//			}
//			else
//			{
//				M.set(0.5);
//			}
//			break;
//		}
//	}
	public void stop()
	{
		M.set(0);
		state = GearPlacerState.Waiting;
	}
	
	public void open()
	{
		M.set(motorSpeedOpen);
		state = GearPlacerState.Opening;
	}
	
	public void close()
	{
		M.set(-motorSpeedClose);
		state = GearPlacerState.Closing;
	}

	@Override
	public void setupLogging(DataLogger logger) 
	{
		logger.addAttribute("GearPlacerState");
	}

	@Override
	public void log(DataLogger logger) 
	{
		logger.log("GearPlacerState", state);
	}

	@Override
	public void reloadConfig() 
	{
		Config.getSetting("gearMotorSpeed", 0.3);
	}
}
