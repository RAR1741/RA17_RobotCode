package org.redalert1741.steamworks.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.redalert1741.robotBase.config.Config;

import edu.wpi.cscore.AxisCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.VideoSource;

public class VisionThread
{
	private static Thread thread;
	private static VisionRunnable vision;
	private static GripPipeline pipeline;
	private static CvSink cvs;
	private static VideoSource source;
	private static Object lock;
	
	public static class VisionRunnable implements Runnable
	{
		private boolean run = true;
		
		@Override
		public void run()
		{
			run = true;
			while(run)
			{
				Mat matthew = new Mat();
				cvs.grabFrame(matthew);
				synchronized (lock)
				{
					pipeline.process(matthew);
				}
				matthew.release();
				Thread.yield();
			}
		}
		
		public void stop() { run = false; }
	}
	
	public static void enable()
	{
		init();
		disable();
		thread = new Thread(vision);
		thread.start();
	}
	
	public static void disable()
	{
		if(vision != null)
		{
			vision.stop();
			while(thread.isAlive()) {Thread.yield();}
		}
		else
		{
			vision = new VisionRunnable();
		}
	}
	
	private static void init()
	{
		if(pipeline == null)
		{
			pipeline = new GripPipeline();
		}
		if(cvs == null)
		{
			cvs = new CvSink("james");
		}
	}
	
	/**
	 * Sets the video source
	 * @see AxisCamera
	 * @param s
	 */
	public static void setSource(VideoSource s)
	{
		source = s;
		init();
		cvs.setSource(source);
	}
	
	/**
	 * Gets list of MatOfPoints from contoursReport
	 * @return contoursReport
	 */
	public static ArrayList<MatOfPoint> getContours()
	{
		synchronized (lock)
		{
			return pipeline.findContoursOutput();
		}
	}
	
	public static ArrayList<Rect> getRekt()
	{
		ArrayList<Rect> rekt = new ArrayList<>();
		for(MatOfPoint mop : getContours())
		{
			rekt.add(Imgproc.boundingRect(mop));
		}
		return rekt;
	}
	
	public static ArrayList<Rect> filterRekts(double aspectR)
	{
		
	}
	
	public static Rect getBestRekt()
	{
		ArrayList<Rect> rekt = getRekt();
		Rect bestRekt = rekt.get(0);
		if(!(rekt.isEmpty()))
		{
			for(Rect r : rekt)
			{
				if(r.tl().y < bestRekt.tl().y)
				{
					bestRekt = r;
				}
			}
			return bestRekt;
		}
		else
		{
			return null;
		}
	}
	
	public static double getHorizontalAngle()
	{
		
	}
	
	/**
	 * Use Axis Camera "axis1741.local" by default and Setting "visionCamera"
	 */
	public static void useAxisCamera()
	{
		setSource(new AxisCamera("camera1", Config.getSetting("visionCamera", "axis1741.local")));
	}
}
