package org.redalert1741.steamworks;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.redalert1741.robotBase.logging.DataLogger;

import edu.wpi.cscore.AxisCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Relay.Value;

public class Robot extends IterativeRobot
{
	private static LoggableNavX navx;
	private static DataLogger logger;
	private static Timer timer;
	private String auto = "";
	AxisCamera ac;
	CvSink cvs;
	GripPipeline grip;
	Relay light;
	List<MatOfPoint> tmpList;
	
	@Override
	public void robotInit()
	{
		timer = new Timer();
		logger = new DataLogger();
		try
		{
			navx = new LoggableNavX(Port.kMXP);
        }
		catch (RuntimeException ex )
		{
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
        }
		ac = new AxisCamera("camera1", "axis-1741.local");
		cvs = new CvSink("bob");
		cvs.setSource(ac);
		grip = new GripPipeline();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while(true)
					{
						Mat matthew = new Mat();
						cvs.grabFrame(matthew);
						grip.process(matthew);
						matthew.release();
						Thread.yield();
					}
				}
				catch(Exception e)
				{
					System.exit(0);
				}
			}
		}).start();
		light = new Relay(0);
	}

	@Override
	public void autonomousInit()
	{
		
	}

	@Override
	public void autonomousPeriodic()
	{
		
	}

	@Override
	public void teleopInit()
	{
		
	}

	@Override
	public void teleopPeriodic()
	{
		
	}

	@Override
	public void testInit()
	{
		timer.reset();
		timer.start();
		logger.open("/home/lvuser/navxTest.log");
		logger.addLoggable(navx);
		logger.setupLoggables();
		logger.writeAttributes();
	}

	@Override
	public void testPeriodic()
	{
		light.set(Value.kForward);
		List<MatOfPoint> tmpList = new ArrayList<MatOfPoint>(grip.findContoursOutput());
		for(MatOfPoint mop : tmpList)
		{
			Rect rec = Imgproc.boundingRect(mop);
			System.out.println(rec);
		}
		System.out.println("break");
		logger.log();
		logger.writeLine();
	}
}

