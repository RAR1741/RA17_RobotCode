package org.redalert1741.steamworks;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

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
}
