package org.redalert1741.steamworks;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.VideoSource;

public class VisionThread
{
	private static Thread thread;
	private static VisionRunnable vision;
	private static GripPipeline pipeline;
	private static CvSink cvs;
	private static VideoSource source;
	
	public static class VisionRunnable implements Runnable
	{
		private boolean run = true;
		
		@Override
		public void run()
		{
			run = true;
			while(run)
			{
				//TODO vision
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
}
