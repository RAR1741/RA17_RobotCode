package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;
import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

public class Shooter implements Loggable, Configurable
{
	private CANTalon flyWheel;
	double p,i,d,f;
	double wallRPM;
	
	public Shooter(CANTalon m)
	{
		flyWheel = m;
		
    	flyWheel.setFeedbackDevice(FeedbackDevice.QuadEncoder);
    	flyWheel.reverseSensor(false);
    	flyWheel.changeControlMode(TalonControlMode.Speed);
    	flyWheel.configNominalOutputVoltage(+0.0f, -0.0f);
    	flyWheel.configPeakOutputVoltage(+12.0f, -12.0f);
    	flyWheel.setProfile(0);
    	flyWheel.setF(Config.getSetting("FlyF", 0));
    	flyWheel.setPID(Config.getSetting("FlyP", 1), 
    					Config.getSetting("FlyI", 0), 
    					Config.getSetting("FlyD", 0));
    	flyWheel.configEncoderCodesPerRev(20);//40 for CIMcoder
    	flyWheel.enable();
	}
	
	public void setSpeed(double rpm)
	{
		flyWheel.set(rpm);
	}
	
	@Override
	public void setupLogging(DataLogger logger) 
	{
		logger.addAttribute("ShootOutputV");
		logger.addAttribute("ShootOutputCurrent");
		logger.addAttribute("ShootEncPos");
		logger.addAttribute("ShootEncVelocity");
		logger.addAttribute("ShootSpeed");
		logger.addAttribute("ShootSetpoint");
	}

	@Override
	public void log(DataLogger logger) 
	{
		logger.log("ShootOutputV", flyWheel.getOutputVoltage());
		logger.log("ShootOutputCurrent", flyWheel.getOutputCurrent());
		logger.log("ShootEncPos", flyWheel.getEncPosition());
		logger.log("ShootEncVelocity", flyWheel.getEncVelocity());
		logger.log("ShootSpeed", flyWheel.getSpeed());
		logger.log("ShootSetpoint", flyWheel.getSetpoint());
	}

	@Override
	public void reloadConfig() 
	{
		p = Config.getSetting("FlyP", 1);
		i = Config.getSetting("FlyI", 0);
		d = Config.getSetting("FlyD", 0);
		f = Config.getSetting("FlyF", 0);
		flyWheel.setF(f);
		flyWheel.setPID(p, i, d);
	}

}
