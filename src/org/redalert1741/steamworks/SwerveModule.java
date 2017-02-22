package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import com.ctre.CANTalon;
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Timer;

public class SwerveModule implements Loggable
{
	private double SteerP,SteerI,SteerD;
	private double SpeedP,SpeedI,SpeedD;
	private double SteerSpeed,SteerTolerance,SteerEncMax,SteerEncMin;
	private double SteerOffset;
	private double MaxRPM;
	
	private CANTalon drive;
	private CANTalon angle;
	private AnalogInput encoder;
	private PIDController PIDc;
	private FakePIDSource encFake;
	private String s;
	
	public SwerveModule(CANTalon d, CANTalon a, AnalogInput e, String name)
	{
		SpeedP = Config.getSetting("speedP",1);
		SpeedI = Config.getSetting("speedI",0);
		SpeedD = Config.getSetting("speedD",0);
		SteerP = Config.getSetting("steerP",2);
		SteerI = Config.getSetting("steerI",0);
		SteerD = Config.getSetting("steerD",0);
		SteerTolerance = Config.getSetting("SteeringTolerance", .25);
		SteerSpeed = Config.getSetting("SteerSpeed", 1);
		SteerEncMax = Config.getSetting("SteerEncMax",4.792);
		SteerEncMax = Config.getSetting("SteerEncMin",0.01);
		SteerOffset = Config.getSetting("SteerEncOffset",0);
		MaxRPM = Config.getSetting("driveCIMmaxRPM", 4200);
		
		drive = d;
		drive.setPID(SpeedP, SpeedI, SpeedD);
		drive.setFeedbackDevice(FeedbackDevice.QuadEncoder);
    drive.configEncoderCodesPerRev(20);
    drive.enable();
		
		angle = a;
		
		encoder = e;
		
		encFake = new FakePIDSource(SteerOffset,SteerEncMin,SteerEncMax);
		
		PIDc = new PIDController(SteerP,SteerI,SteerD,encFake,angle);
		PIDc.disable();
		PIDc.setContinuous(true);
		PIDc.setInputRange(SteerEncMin,SteerEncMax);
		PIDc.setOutputRange(-SteerSpeed,SteerSpeed);
		PIDc.setPercentTolerance(SteerTolerance);
		PIDc.setSetpoint(2.4);
		PIDc.enable();
		s = name;
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
		drive.changeControlMode(TalonControlMode.PercentVbus);
		drive.set(speed);
	}
	
	public void setDriveSpeed(double speed)
	{
		drive.changeControlMode(TalonControlMode.Speed);
		drive.set(speed*MaxRPM);
		//System.out.println(drive.getSetpoint());
	}
	
	public double pidGet()
	{
		return encFake.pidGet();
	}
	
	public void setEncMax(double max)
	{
		encFake.setMinMax(0, max);
		PIDc.setInputRange(0, max);
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
		PIDc.setSetpoint(angle*((SteerEncMax-SteerEncMin)/360.0f));
	}
	
	public double[] calibrateAngle() 
	{
		double max = 0;
		double min = 10;
		Timer t = new Timer();
		t.reset();
		t.start();
		angle.changeControlMode(TalonControlMode.PercentVbus);
		PIDSet();
		PIDc.setSetpoint(0);
		while(t.get() <= 5)
		{
			if(encoder.getVoltage() > 0.25 && encoder.getVoltage() < 0.75)
			{
				encFake.pidSetAbsolute(encoder.pidGet());
				PIDc.setSetpoint(4.6);
			}
			else if(encoder.getVoltage() < 4.8 && encoder.getVoltage() > 4.5)
			{
				encFake.pidSetAbsolute(encoder.pidGet());
				PIDc.setSetpoint(0.35);
			}
			else if(encoder.getVoltage() < 4.5 && encoder.getVoltage() > .75)
			{
				encFake.pidSetAbsolute(encoder.pidGet());
				PIDc.setSetpoint(0);
			}
			
			if(encoder.getVoltage() > max)
			{
				max = encoder.getVoltage();
			}
			if(encoder.getVoltage() < min)
			{
				min = encoder.getVoltage();
			}
		}
		angle.set(0);
		angle.setControlMode(0);
		return new double[] {min,max};
	}
	
	@Override
	public void setupLogging(DataLogger logger)
	{
		logger.addAttribute(s + "pos");
		logger.addAttribute(s + "Current");
		logger.addAttribute(s + "speed");
		logger.addAttribute(s + "setpoint");
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
		logger.log(s + "setpoint", drive.getSetpoint());
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
		SpeedP = Config.getSetting("speedP",1);
		SpeedI = Config.getSetting("speedI",0);
		SpeedD = Config.getSetting("speedD",0);
		drive.setPID(SpeedP, SpeedI, SpeedD);
		SteerP = Config.getSetting("steerP"+s,2);
		SteerI = Config.getSetting("steerI"+s,0);
		SteerD = Config.getSetting("steerD"+s,0);
		PIDc.setPID(SteerP,SteerI,SteerD);
		MaxRPM = Config.getSetting("driveCIMmaxRPM", 4200);
	///////////////////////////////////////////////////////////////////
		SteerTolerance = Config.getSetting("SteeringTolerance", 0.25);
		SteerSpeed = Config.getSetting("SteerSpeed", 1);
		SteerEncMax = Config.getSetting("SteerEncMax" + s,4.792);
		SteerEncMin = Config.getSetting("SteerEncMin" + s,0.01);
		PIDc.setInputRange(SteerEncMin,SteerEncMax);
		encFake.setMinMax(SteerEncMin, SteerEncMax);
		PIDc.setOutputRange(-SteerSpeed,SteerSpeed);
		PIDc.setPercentTolerance(SteerTolerance);
	/////////////////////////////////////////////////////
		SteerOffset = Config.getSetting("SteerEncOffset" + s,0);
		encFake.setOffset(SteerOffset);
	}
	
	public double getDriveEnc()
	{
		return drive.getEncPosition();
	}

	public void setBrake() 
	{
		drive.enableBrakeMode(true);
	}
	
	public void setCoast()
	{
		drive.enableBrakeMode(false);
	}
}
