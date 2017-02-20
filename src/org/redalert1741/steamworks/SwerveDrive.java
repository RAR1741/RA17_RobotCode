package org.redalert1741.steamworks;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import edu.wpi.first.wpilibj.AnalogInput;
import com.ctre.CANTalon;

public class SwerveDrive implements Loggable
{
	private static final double PI = 3.14159265358979;
	
	private double TurningSpeedFactor;
	private double length,width,diameter;
	private double temp;
	private double a,b,c,d;
	private double ws1,ws2,ws3,ws4;
	private double wa1,wa2,wa3,wa4;
	private double max;
	private int movecount;
	
	protected SwerveModule FRM;
	protected SwerveModule FLM;
	protected SwerveModule BRM;
	protected SwerveModule BLM;
	
	public SwerveDrive(CANTalon fr, CANTalon fra, AnalogInput fre, CANTalon fl, CANTalon fla, AnalogInput fle, CANTalon br, CANTalon bra, AnalogInput bre, CANTalon bl, CANTalon bla, AnalogInput ble)
	{
		FRM = new SwerveModule(fr, fra, fre, "FR");
		FLM = new SwerveModule(fl, fla, fle, "FL");
		BRM = new SwerveModule(br, bra, bre, "BR");
		BLM = new SwerveModule(bl, bla, ble, "BL");

		length = Config.getSetting("FrameLength",1);
		width = Config.getSetting("FrameWidth",1);
		diameter = Math.sqrt(Math.pow(length,2)+Math.pow(width,2));
		temp = 0.0;
		a = 0.0;b = 0.0;c = 0.0;d = 0.0;
		ws1 = 0.0;ws2 = 0.0;ws3 = 0.0;ws4 = 0.0;
		wa1 = 0.0;wa2 = 0.0;wa3 = 0.0;wa4 = 0.0;
		max = 0.0;
		movecount = 0;
	}
	
	public void swerve(double x, double y, double z, double gyro, boolean fieldOrient)
	{
		gyro *= PI/180.0f;
		z *= TurningSpeedFactor;
		
		if((x!=0 || y!=0) || z!=0)
		{
			movecount = 100;
			if(fieldOrient)
			{
				temp = y * Math.cos(gyro) + x * Math.sin(gyro);
				x = -y * Math.sin(gyro) + x * Math.cos(gyro);
				y = temp;
			}
	
			a = x - z * (length/diameter);
			b = x + z * (length/diameter);
			c = y - z * (width/diameter);
			d = y + z * (width/diameter);
	
			ws1 = Math.sqrt(Math.pow(b,2) + Math.pow(c,2));
			ws2 = Math.sqrt(Math.pow(b,2) + Math.pow(d,2));
			ws3 = Math.sqrt(Math.pow(a,2) + Math.pow(d,2));
			ws4 = Math.sqrt(Math.pow(a,2) + Math.pow(c,2));
			max = 0;
			if(ws1 > max){max = ws1;}
			if(ws2 > max){max = ws2;}
			if(ws3 > max){max = ws3;}
			if(ws4 > max){max = ws4;}
			if(max > 1){ws1 /= max;ws2 /= max;ws3 /= max;ws4 /= max;}
	
			wa1 = Math.atan2(b,c) * 180.0f/PI;
			wa2 = Math.atan2(b,d) * 180.0f/PI;
			wa3 = Math.atan2(a,d) * 180.0f/PI;
			wa4 = Math.atan2(a,c) * 180.0f/PI;
			if(wa1 < 0){wa1 += 360;}//wa1 = FL
			if(wa2 < 0){wa2 += 360;}//wa2 = FR
			if(wa3 < 0){wa3 += 360;}//wa3 = BR
			if(wa4 < 0){wa4 += 360;}//wa4 = BL
			FRM.PIDSet();
			FLM.PIDSet();
			BRM.PIDSet();
			BLM.PIDSet();
			
			double tmp;
			if((tmp = closestAngle(FRM.pidGet()/(FRM.getEncMax()/360.0f), wa2)) != wa2)
			{
				wa2 = tmp;
				ws2 *= -1;
			}
			if((tmp = closestAngle(FLM.pidGet()/(FLM.getEncMax()/360.0f), wa1)) != wa1)
			{
				wa1 = tmp;
				ws1 *= -1;
			}
			if((tmp = closestAngle(BRM.pidGet()/(BRM.getEncMax()/360.0f), wa3)) != wa3)
			{
				wa3 = tmp;
				ws3 *= -1;
			}
			if((tmp = closestAngle(BLM.pidGet()/(BLM.getEncMax()/360.0f), wa4)) != wa4)
			{
				wa4 = tmp;
				ws4 *= -1;
			}
			
			FRM.setDriveSpeed(ws2);
			FLM.setDriveSpeed(-ws1);
			BRM.setDriveSpeed(ws3);
			BLM.setDriveSpeed(-ws4);
			
			FRM.setAngle(wa2);
			FLM.setAngle(wa1);
			BRM.setAngle(wa3);
			BLM.setAngle(wa4);
		}
		else
		{
			movecount--;
			if(movecount < 0)
			{
				swerveAbsolute(0, 0, 0, 0, false);
			}
			else
			{
				FRM.PIDSet();
				FLM.PIDSet();
				BRM.PIDSet();
				BLM.PIDSet();
				FRM.setDriveSpeed(0);
				FLM.setDriveSpeed(0);
				BRM.setDriveSpeed(0);
				BLM.setDriveSpeed(0);
			}
		}
	}
	
	public void swerveAbsolute(double x, double y, double z, double gyro, boolean fieldOrient)
	{
		gyro *= PI/180.0f;
		z *= TurningSpeedFactor;
		if(fieldOrient)
		{
			temp = y * Math.cos(gyro) + x * Math.sin(gyro);
			x = -y * Math.sin(gyro) + x * Math.cos(gyro);
			y = temp;
		}

		a = x - z * (length/diameter);
		b = x + z * (length/diameter);
		c = y - z * (width/diameter);
		d = y + z * (width/diameter);

		ws1 = Math.sqrt(Math.pow(b,2) + Math.pow(c,2));
		ws2 = Math.sqrt(Math.pow(b,2) + Math.pow(d,2));
		ws3 = Math.sqrt(Math.pow(a,2) + Math.pow(d,2));
		ws4 = Math.sqrt(Math.pow(a,2) + Math.pow(c,2));
		max = 0;
		if(ws1 > max){max = ws1;}
		if(ws2 > max){max = ws2;}
		if(ws3 > max){max = ws3;}
		if(ws4 > max){max = ws4;}
		if(max > 1){ws1 /= max;ws2 /= max;ws3 /= max;ws4 /= max;}

		wa1 = Math.atan2(b,c) * 180.0f/PI;
		wa2 = Math.atan2(b,d) * 180.0f/PI;
		wa3 = Math.atan2(a,d) * 180.0f/PI;
		wa4 = Math.atan2(a,c) * 180.0f/PI;
		if(wa1 < 0){wa1 += 360;}//wa1 = FL
		if(wa2 < 0){wa2 += 360;}//wa2 = FR
		if(wa3 < 0){wa3 += 360;}//wa3 = BR
		if(wa4 < 0){wa4 += 360;}//wa4 = BL
		FRM.PIDSet();
		FLM.PIDSet();
		BRM.PIDSet();
		BLM.PIDSet();
		
		FRM.setDriveSpeed(ws2);
		FLM.setDriveSpeed(-ws1);
		BRM.setDriveSpeed(ws3);
		BLM.setDriveSpeed(-ws4);

		FRM.setAngle(wa2);
		FLM.setAngle(wa1);
		BRM.setAngle(wa3);
		BLM.setAngle(wa4);
	}
	
	public void angleToZero()
	{
		FRM.PIDSet();
		FLM.PIDSet();
		BRM.PIDSet();
		BLM.PIDSet();
		FRM.setDrive(0);
		FLM.setDrive(0);
		BRM.setDrive(0);
		BLM.setDrive(0);
		FRM.setAngle(0);
		FLM.setAngle(0);
		BRM.setAngle(0);
		BLM.setAngle(0);
	}
	
	public void setEncMax(SwerveModule module,double max)
	{
		module.setEncMax(max);
	}
	
	public double[][] calibrateAngle()
	{
		double[][] max = new double[4][2];
		max[0] = FRM.calibrateAngle();
		max[1] = FLM.calibrateAngle();
		max[2] = BRM.calibrateAngle();
		max[3] = BLM.calibrateAngle();
		return max;
	}
	
	@Override
	public void setupLogging(DataLogger logger)
	{
		FRM.setupLogging(logger);
		FLM.setupLogging(logger);
		BRM.setupLogging(logger);
		BLM.setupLogging(logger);
	}
	
	@Override
	public void log(DataLogger logger)
	{
		FRM.log(logger);
		FLM.log(logger);
		BRM.log(logger);
		BLM.log(logger);
	}

	public void ReloadConfig()
	{
		length = Config.getSetting("FrameLength",1);
		width = Config.getSetting("FrameWidth",1);
		TurningSpeedFactor = Config.getSetting("turningSpeedFactor", 1);
		/////////////////////////////////////////////////////
		FRM.ReloadConfig("FR");
		FLM.ReloadConfig("FL");
		BRM.ReloadConfig("BR");
		BLM.ReloadConfig("BL");
	}
	
	/**
	 * Finds the closest angle, including 180 degree weirdness
	 * @param p Current position
	 * @param t Target angle
	 * @return
	 */
	public static double closestAngle(double p, double t)
	{
		p %= 360;
		double t1 = t % 360;
		double t2 = (t1+180)%360;
		double d1 = Math.abs(p - t1);
		if(d1 > 180) d1 = 360 - d1;
		double d2 = Math.abs(p -t2);
		if(d2 > 180) d2 = 360 - d2;
		return (d1 < d2 ? t1 : t2);
	}
	
	public String toString()
	{
		return "FR Speed: " + FRM.getTurnSpeed() + "\n" +"FL Speed: " + FLM.getTurnSpeed() + "\n" +"BR Speed: " + BRM.getTurnSpeed() + "\n" +"BL Speed: " + BLM.getTurnSpeed() + "\n";
	}
}
