package org.redalert1741.steamworks.vision;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.redalert1741.robotBase.config.Config;

import edu.wpi.cscore.AxisCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.CameraServer;

public class VisionThread
{
	private static final int HORIZONTAL_CAMERA_ANGLE = (int) Config.getSetting("HorizontalCameraAngle", 0);
	private static final double HORIZONTAL_FOV = (int) Config.getSetting("HorizontalFOV", 60);
	private static Thread thread;
	private static VisionRunnable vision;
	private static GripPipeline pipeline;
	private static CvSink cvs;
	public static VideoSource source;
	private static Object lock;
	private static VisionFilter filter;
	private static CvSource outputStream;
	
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
				if(outputStream != null)
				{
					//System.out.println("put");
					outputStream.putFrame(pipeline.rgbThresholdOutput());
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
		if(vision != null && thread != null)
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
		try
		{
			cvs.setSource(source);
		}
		catch(Exception e)
		{
//			Catch any errors and print them to the console
			System.out.println("Camera Connection Failed...");
			e.printStackTrace();
		}

	}
	
	/**
	 * Gets list of MatOfPoints from contoursReport
	 * @return contoursReport
	 */
	public static ArrayList<MatOfPoint> getContours()
	{
		synchronized (lock)
		{
			return pipeline.filterContoursOutput();
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
	 * Finds the best Rect from {@link #getRekt()} using the current {@link VisionFilter}
	 * @return The best Rect or null if no filter is set
	 */
	public static Rect getBestRekt()
	{
		return filter == null ? null : filter.getBestRect(getRekt());
	}

	/**
	 * Calculates current horizontal angle to the best Rect
	 * @see #getRekt()
	 * @see #getBestRekt()
	 * @return angle to best Rect
	 */
	public static Double getHorizontalAngle()
	{
		Rect bestRect = getBestRekt();
		Mat matthew = new Mat();
		if(bestRect != null)
		{
			int w = (int) Config.getSetting("camerawidth", 160);
			double x = bestRect.br().x - (bestRect.width / 2);//x = middle of target in x dir
//			Set x to +/- 1 using the position on the screen
			x = ((2 * (x / w)) - 1);
			System.out.println("Matthew width: " + w);
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
		try
		{
			setSource(new AxisCamera("camera1", Config.getSetting("visionCamera", "axis-1741-bw.local")));
			System.out.println("Opened successfully...");
		}
		catch (Exception e) 
		{
//			Catch any errors and print them to the console
			System.out.println("Camera Connection Failed...");
			e.printStackTrace();
		}
	}
	
	public static void useUSBCamera()
	{
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
        camera.setResolution(640, 480);
		setSource(camera);
//		outputStream = CameraServer.getInstance().putVideo("Blur", 640, 480);
	}
}
