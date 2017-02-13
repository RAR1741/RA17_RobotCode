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
	CANTalon flyWheel;
	CANTalon angle;
	double p,i,d,f;
	double wallRPM;
	
	public Shooter(CANTalon m, CANTalon a)
	{
		flyWheel = m;
		angle = a;
		
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
	
	@Override
	public void setupLogging(DataLogger logger) 
	{
		logger.addAttribute("OutputV");
		logger.addAttribute("OutputCurrent");
		logger.addAttribute("EncPos");
		logger.addAttribute("EncVelocity");
		logger.addAttribute("Speed");
		logger.addAttribute("Position");
		logger.addAttribute("Setpoint");
		logger.addAttribute("IA");
		logger.addAttribute("ControlMode");
	}

	@Override
	public void log(DataLogger logger) 
	{
		logger.log("OutputV", flyWheel.getOutputVoltage());
		logger.log("OutputCurrent", flyWheel.getOutputCurrent());
		logger.log("EncPos", flyWheel.getEncPosition());
		logger.log("EncVelocity", flyWheel.getEncVelocity());
		logger.log("Speed", flyWheel.getSpeed());
		logger.log("Position", flyWheel.getPosition());
		logger.log("Setpoint", flyWheel.getSetpoint());
		logger.log("IA", flyWheel.getSetpoint());
		logger.log("ControlMode", flyWheel.getControlMode().toString());
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
