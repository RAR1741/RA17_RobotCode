package org.redalert1741.steamworks;

import java.util.ArrayList;
import java.util.Iterator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.robotBase.logging.DataLogger;
import org.redalert1741.robotBase.logging.Loggable;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class Vision implements Configurable, Loggable
{
	private final String cameraURL = "http://axis-1741.local/mjpg/video.mjpg";
	private final int minTargetHeight = 20;
	private final int minTargetWidth = 30;
	private final double maxAspectR = 3;
	private final double minAspectR = 0.75;
	
	static
	{ 
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1741-frc.local");
	}
//	Constants for RGB values
	public static final Scalar 
		RED = new Scalar(0, 0, 255),
		BLUE = new Scalar(255, 0, 0),
		GREEN = new Scalar(0, 255, 0),
		BLACK = new Scalar(0,0,0),
		YELLOW = new Scalar(0, 255, 255),
		CYAN =  new Scalar(255, 255, 0),
//		Lower and upper bounds of the HSV filtering
		LOWER_BOUNDS = new Scalar(71,36,100),
		UPPER_BOUNDS = new Scalar(154,255,255);//lower:(73,53,220) Upper:(94,255,255)
	
//   	Random variables
		public static VideoCapture videoCapture;
		public static Mat matInput, matOriginal, matHSV, matThresh, clusters, matHeirarchy;
		public static NetworkTable table, station;
		//////////////////  DO NOT EDIT  //////////////////////
	//	Constants for known variables
		public static final int TOP_TARGET_HEIGHT = 99;
		public static final int TOP_CAMERA_HEIGHT = 12;
	//	Camera detail constants
		public static final double VERTICAL_FOV  = 34;
		public static final double HORIZONTAL_FOV  = 49;
		public static final double VERTICAL_CAMERA_ANGLE = 55;
		public static final double HORIZONTAL_CAMERA_ANGLE = 0;
		///////////////////////////////////////////////////////
	//	Main loop variable
		public static boolean shouldRun = true;
		
		
	public Vision()
	{
//		Initialize the matrixes
		matOriginal = new Mat();
		matHSV = new Mat();
		matThresh = new Mat();
		clusters = new Mat();
		matHeirarchy = new Mat();
		
//		Set the network table to use
		table = NetworkTable.getTable("logging");
		station = NetworkTable.getTable("Station");
	}
	
	public void startCameraStream()
	{
		while(true)
		{
			try {
//				Opens up the camera stream and tries to load it
				System.out.println("Initializing camera...");
				videoCapture = new VideoCapture();
				
				System.out.println("Opening stream...");
				videoCapture.open(cameraURL);
				
				System.out.println("Checking connection...");
//				Wait until it is opened
				while(!videoCapture.isOpened()){}
				
				System.out.println("Opened successfully...");
				
//				Actually process the image
				processImage();
				System.out.println("Finished processing...");
				
			} catch (Exception e) {
//				Catch any errors and print them to the console
				System.out.println("Uh oh...");
				e.printStackTrace();
				break;
			}
		}
		
	}

	public void processImage() 
	{
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		double x,y,targetAngle,distance,width,pan,tilt;
		Rect bestRec = new Rect(0,0,0,0);
		String output = new String();
	    
	    long lastTime = 0;
	    long temp = 0;
	    double fps = 0;

		while(true)
		{
//			Clear variables from previous loop
			contours.clear();
			output = "";
			
//			Capture image from the axis camera
			videoCapture.read(matOriginal);
			matInput = matOriginal.clone();
			
//			Convert the image type from BGR to HSV
			Imgproc.cvtColor(matOriginal, matHSV, Imgproc.COLOR_BGR2HSV);
			
//			Filter out any colors not inside the threshold
			Core.inRange(matHSV, LOWER_BOUNDS, UPPER_BOUNDS, matThresh);
			
//			Find the contours
			Imgproc.findContours(matThresh, contours, matHeirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

//			Make sure the contours that are detected are at least 30x30 pixels and a valid aspect ratio
			for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
				Rect rec = Imgproc.boundingRect(matOfPoint);
				if(rec.height < minTargetHeight || rec.width < minTargetWidth){
					iterator.remove();
//					drawContour(matOriginal, rec, BLUE);
					continue;
				}
				
				float aspect = (float)rec.width/(float)rec.height;
				if(aspect > maxAspectR || aspect < minAspectR){
//					drawContour(matOriginal, rec, YELLOW);
					iterator.remove();
				}
			}
			
			bestRec = new Rect(0,0,0,0);
//			Calculate targeting output for each of the remaining contours
			for (int p = 0; p < contours.size(); p++) 
			{
				Rect rec = Imgproc.boundingRect(contours.get(p));
				if(rec.width > bestRec.width){
					bestRec = rec;
				}
			}
			
//			If there was actually a target
			if(bestRec.width != 0)
			{			
//				Horizontal angle to target
				pan = getHAngle(bestRec);
				table.putNumber("TargetHAngle", pan);
//				Vertical angle to target
				tilt = getBAngle(bestRec);
				table.putNumber("TargetVAngle", tilt);
//				Calculate the horizontal distance to the goal
				distance = getDistance(bestRec);
				table.putNumber("TargetDistance", distance);
//				Get target width
				width = getWidth(bestRec);
				table.putNumber("TargetWidth", width);
//				output = String.format("%.2f,%.2f,%.2f,%.2f", width, pan, tilt, distance);
//				System.out.println(output);
			}
			else
			{
				table.putNumber("TargetHAngle", -1);
				table.putNumber("TargetVAngle", -1);
				table.putNumber("TargetDistance", -1);
				table.putNumber("TargetWidth", -1);
			}
		}
	}
	
	//using best rectangle, get horizontal angle
	private double getHAngle(Rect r)
	{
		double x = 0.0;
		x = r.br().x - (r.width / 2);
//		Set x to +/- 1 using the position on the screen
		x = ((2 * (x / matOriginal.width())) - 1);
		double pan = HORIZONTAL_CAMERA_ANGLE-(x*HORIZONTAL_FOV /2.0);
		return pan;
	}
	
	//using best rectangle, get vertical angle
	private double getBAngle(Rect r)
	{
		double y = r.br().y + (r.height / 2);
//		Set y to +/- 1 using the position on the screen
		y = ((2 * (y / matOriginal.height())) - 1);
		double tilt = VERTICAL_CAMERA_ANGLE-(y*VERTICAL_FOV /2.0);
		return tilt;
	}
	
	private double getDistance(Rect r)
	{
		double y = r.br().y + (r.height / 2);
		double targetAngle = (y * VERTICAL_FOV / 2) + VERTICAL_CAMERA_ANGLE;
		 return (TOP_TARGET_HEIGHT - TOP_CAMERA_HEIGHT)/
				Math.tan(Math.toRadians(targetAngle));
	}
	
	private double getWidth(Rect r)
	{
		return r.width;
	}
	
	@Override
	public void setupLogging(DataLogger logger) {
		
	}

	@Override
	public void log(DataLogger logger) {
		
	}

	@Override
	public void reloadConfig() {
		
	}
}
