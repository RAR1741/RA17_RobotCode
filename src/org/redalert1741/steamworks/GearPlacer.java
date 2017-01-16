package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;
import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

public class GearPlacer implements Loggable,Configurable
{
	CANTalon M;
	GearPlacerState state;
	double motorSpeed;

	
	public GearPlacer(CANTalon m)
	{
		M = m;
		M.changeControlMode(TalonControlMode.PercentVbus);
		M.ConfigFwdLimitSwitchNormallyOpen(true);
		M.ConfigRevLimitSwitchNormallyOpen(true);
		M.enable();
		
		motorSpeed = Config.getSetting("gearMotorSpeed", 0.5);
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
		M.set(motorSpeed);
		state = GearPlacerState.Opening;
	}
	
	public void close()
	{
		M.set(-motorSpeed);
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
		Config.getSetting("gearMotorSpeed", 0.5);
	}
}
