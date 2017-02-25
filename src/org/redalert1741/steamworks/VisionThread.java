package org.redalert1741.steamworks;

public class VisionThread
{
	private static Thread thread;
	private static VisionRunnable vision;
	
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
}
