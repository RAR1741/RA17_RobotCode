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
	private static final int HORIZONTAL_CAMERA_ANGLE = (int) Config.getSetting("HorizontalCameraAngle", 0);
	private static final double HORIZONTAL_FOV = (int) Config.getSetting("HorizontalFOV", 49);
	private static Thread thread;
	private static VisionRunnable vision;
	private static GripPipeline pipeline;
	private static CvSink cvs;
	private static VideoSource source;
	private static Object lock;
	private static VisionFilter filter;
	
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
	
	/**
	 * Enables/restarts the vision tracking thread
	 */
	public static void enable()
	{
		init();
		disable();
		thread = new Thread(vision);
		thread.start();
	}
	
	/**
	 * Disables the vision tracking thread
	 */
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
	
	/**
	 * Init things that can be null
	 */
	private static void init()
	{
		if(lock == null)
		{
			lock = new Object();
		}
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
	 * Sets the filter to use
	 * @see VisionFilter
	 * @param vf Filter to use
	 */
	public static void setFilter(VisionFilter vf)
	{
		filter = vf;
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
	
	/**
	 * Converts the output of {@link #getContours()} to Rects
	 * @return Current contours as Rects
	 */
	public static ArrayList<Rect> getRekt()
	{
		ArrayList<Rect> rekt = new ArrayList<>();
		for(MatOfPoint mop : getContours())
		{
			rekt.add(Imgproc.boundingRect(mop));
		}
		return rekt;
	}
	
	/**
	 * Filters all Rects using current {@link VisionFilter}
	 * @see #getRekt()
	 * @return Rects filtered by the current filter or all of the Rects if there is none
	 */
	public static ArrayList<Rect> filterRekts()
	{
		return filter == null ? getRekt() : filter.getFilteredRects(getRekt());
	}
	
	/**
	 * Finds the best Rect from {@link #getRekt()} using the current {@link VisionFilter}
	 * @return The best Rect or null if no filter is set
	 */
	public static Rect getBestRekt()
	{
		return filter == null ? null : filter.getCombinedBestRect(getRekt());
	}

	/**
	 * Calculates current horizontal angle to the best Rect
	 * @see #getRekt()
	 * @see #getBestRekt()
	 * @return angle to best Rect
	 */
	public static double getHorizontalAngle()
	{
		Rect bestRect = getBestRekt();
		Mat matthew = new Mat();
		if(bestRect != null)
		{
			cvs.grabFrame(matthew);
			double x = bestRect.br().x - (bestRect.width / 2);
//			Set x to +/- 1 using the position on the screen
			x = ((2 * (x / matthew.width())) - 1);
			matthew.release();
			return HORIZONTAL_CAMERA_ANGLE-(x*HORIZONTAL_FOV /2.0);
		}
		else
		{
			return Double.POSITIVE_INFINITY;
		}

	}
	
	public static double getWidthBestRekt()
	{
		return getBestRekt().width;
	}
	
	public static int getHeightBestRekt()
	{
		return getBestRekt().height;
	}
	
	/**
	 * Use Axis Camera "axis1741.local" by default and Setting "visionCamera"
	 */
	public static void useAxisCamera()
	{
		setSource(new AxisCamera("camera1", Config.getSetting("visionCamera", "axis1741.local")));
	}
}
