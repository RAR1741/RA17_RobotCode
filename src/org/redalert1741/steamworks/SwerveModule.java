package org.redalert1741.steamworks;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.PIDController;

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
		SteerP = Config.GetSetting("steerP",2);
		SteerI = Config.GetSetting("steerI",0);
		SteerD = Config.GetSetting("steerD",0);
		SteerTolerance = Config.GetSetting("Steering%Tolerance", .25);
		SteerSpeed = Config.GetSetting("SteerSpeed", 1);
		SteerEncMax = Config.GetSetting("SteerEncMax",4.792);
		SteerOffset = Config.GetSetting("SteerEncOffset",0);
		
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
	
	@Override
	public void setupLogging(Logger logger)
	{
		logger.AddAttribute(s + "pos");
		logger.AddAttribute(s + "Current");
		logger.AddAttribute(s + "speed");
		logger.AddAttribute(s + "Apos");
		logger.AddAttribute(s + "ACurrent");
		logger.AddAttribute(s + "Encpos");
		logger.AddAttribute(s + "EncSetpoint");
		logger.AddAttribute(s + "EncSpeed");
	}
	
	@Override
	public void log(Logger logger)
	{
		logger.Log(s + "pos", drive.getEncPosition());
		logger.Log(s + "Current", drive.getOutputCurrent());
		logger.Log(s + "speed", drive.getSpeed());
		logger.Log(s + "Apos", angle.getEncPosition());
		logger.Log(s + "ACurrent", angle.getOutputCurrent());
		//logger.Log(s + "Encpos", encoder.getVoltage() + encFake.m_offset);
		logger.Log(s + "Encpos", encoder.getVoltage());
		logger.Log(s + "EncSetpoint", PIDc.getSetpoint());
		logger.Log(s + "EncSpeed", encFake.getSpeed());
	}
	
	public void ReloadConfig(String s)
	{
	/////////////////////////////////////////////////////
		SteerP = Config.GetSetting("steerP",2);
		SteerI = Config.GetSetting("steerI",0);
		SteerD = Config.GetSetting("steerD",0);
		PIDc.setPID(SteerP,SteerI,SteerD);
	///////////////////////////////////////////////////////////////////
		SteerTolerance = Config.GetSetting("Steering%Tolerance", 0.25);
		SteerSpeed = Config.GetSetting("SteerSpeed", 1);
		SteerEncMax = Config.GetSetting("SteerEncMax" + s,4.792);
		PIDc.setInputRange(0,SteerEncMax);
		PIDc.setOutputRange(-SteerSpeed,SteerSpeed);
		PIDc.setPercentTolerance(SteerTolerance);
	/////////////////////////////////////////////////////
		SteerOffset = Config.GetSetting("SteerEncOffset" + s,0);
		encFake.setOffset(SteerOffset);
	}
}
