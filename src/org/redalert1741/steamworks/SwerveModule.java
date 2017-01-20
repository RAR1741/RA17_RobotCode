package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Timer;

public class SwerveModule implements Loggable
{
	private double SteerP,SteerI,SteerD;
	private double SteerSpeed,SteerTolerance,SteerEncMax;
	private double SteerOffset;
	
	private CANTalon drive;
	private CANTalon angle;
	private AnalogInput encoder;
	private PIDController PIDc;
	private FakePIDSource encFake;
	private String s;
	
	public SwerveModule(CANTalon d, CANTalon a, AnalogInput e, String name)
	{
		SteerP = Config.getSetting("steerP",2);
		SteerI = Config.getSetting("steerI",0);
		SteerD = Config.getSetting("steerD",0);
		SteerTolerance = Config.getSetting("Steering%Tolerance", .25);
		SteerSpeed = Config.getSetting("SteerSpeed", 1);
		SteerEncMax = Config.getSetting("SteerEncMax",4.792);
		SteerOffset = Config.getSetting("SteerEncOffset",0);
		
		drive = d;
		drive.setControlMode(0);
		
		angle = a;
		angle.setControlMode(0);
		
		encoder = e;
		
		encFake = new FakePIDSource(SteerOffset,0,SteerEncMax);
		
		PIDc = new PIDController(SteerP,SteerI,SteerD,encFake,angle);
		PIDc.disable();
		PIDc.setContinuous(true);
		PIDc.setInputRange(0,SteerEncMax);
		PIDc.setOutputRange(-SteerSpeed,SteerSpeed);
		PIDc.setPercentTolerance(SteerTolerance);
		PIDc.setSetpoint(2.4);
		PIDc.enable();
		s=name;
	}
	
	public void setAngleDrive(double speed, double angle)
	{
		if(Math.abs(encFake.pidGet()/(SteerEncMax/360.0f) - angle) > 90)
		{
			angle = (angle + 180)%360;
			speed = -speed;
		}
		
		setDrive(speed);
		setAngle(angle);
	}
	
	public double getTurnSpeed()
	{
		return encFake.getSpeed();
	}
	
	public void setDrive(double speed)
	{
		drive.set(speed);
	}
	
	public double pidGet()
	{
		return encFake.pidGet();
	}
	
	public double getEncMax()
	{
		return SteerEncMax;
	}
	
	public void PIDSet()
	{
		encFake.pidSet(encoder.pidGet());
	}
	
	public void setAngle(double angle)
	{
		PIDc.setSetpoint(angle*(SteerEncMax/360.0f));
	}
	
	public double calibrateAngle() 
	{
		double max = 0;
		Timer t = new Timer();
		t.reset();
		t.start();
		angle.set(0.1);
		while(t.get() <= 5)
		{
			if(encoder.getVoltage() > max)
			{
				max = encoder.getVoltage();
			}
		}
		angle.set(0);
		return max;
	}
	
	@Override
	public void setupLogging(DataLogger logger)
	{
		logger.addAttribute(s + "pos");
		logger.addAttribute(s + "Current");
		logger.addAttribute(s + "speed");
		logger.addAttribute(s + "Apos");
		logger.addAttribute(s + "ACurrent");
		logger.addAttribute(s + "Encpos");
		logger.addAttribute(s + "EncSetpoint");
		logger.addAttribute(s + "EncSpeed");
	}
	
	@Override
	public void log(DataLogger logger)
	{
		logger.log(s + "pos", drive.getEncPosition());
		logger.log(s + "Current", drive.getOutputCurrent());
		logger.log(s + "speed", drive.getSpeed());
		logger.log(s + "Apos", angle.getEncPosition());
		logger.log(s + "ACurrent", angle.getOutputCurrent());
		//logger.Log(s + "Encpos", encoder.getVoltage() + encFake.m_offset);
		logger.log(s + "Encpos", encoder.getVoltage());
		logger.log(s + "EncSetpoint", PIDc.getSetpoint());
		logger.log(s + "EncSpeed", encFake.getSpeed());
	}
	
	public void ReloadConfig(String s)
	{
	/////////////////////////////////////////////////////
		SteerP = Config.getSetting("steerP",2);
		SteerI = Config.getSetting("steerI",0);
		SteerD = Config.getSetting("steerD",0);
		PIDc.setPID(SteerP,SteerI,SteerD);
	///////////////////////////////////////////////////////////////////
		SteerTolerance = Config.getSetting("Steering%Tolerance", 0.25);
		SteerSpeed = Config.getSetting("SteerSpeed", 1);
		SteerEncMax = Config.getSetting("SteerEncMax" + s,4.792);
		PIDc.setInputRange(0,SteerEncMax);
		PIDc.setOutputRange(-SteerSpeed,SteerSpeed);
		PIDc.setPercentTolerance(SteerTolerance);
	/////////////////////////////////////////////////////
		SteerOffset = Config.getSetting("SteerEncOffset" + s,0);
		encFake.setOffset(SteerOffset);
	}
}
