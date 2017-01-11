package org.redalert1741.robotBase.input;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.DriverStation;

public class Controller 
{
	private class Background implements Runnable
	{
		boolean running = true;
		private boolean[] buttons;
		private float[] axis;
		
		private void sendButtonEvent(ButtonEvent be, boolean p)
		{
			for(ButtonListener b : bl)
			{
				if(p) { b.buttonPressed(be); }
				else { b.buttonReleased(be); }
			}
		}
		
		private void sendAxisEvent(AxisEvent ae)
		{
			for(AxisListener a : al)
			{
				a.axisMoved(ae);
			}
		}
		
		@Override
		public void run()
		{
			buttons = new boolean[monitor[0]];
			axis = new float[monitor[1]];
			boolean b;
			float a;
			while(running)
			{
				for(int i = 0; i < monitor[0]; i++)
				{
					if((b = getNumberedButton(i)) != buttons[i])
					{
						sendButtonEvent(new ButtonEvent(Controller.this, i), b);
						buttons[i] = b;
					}
				}
				Thread.yield();
				for(int i = 0; i < monitor[1]; i++)
				{
					if((a = getRawAxis(i)) != axis[i])
					{
						sendAxisEvent(new AxisEvent(Controller.this, i, a));
						axis[i] = a;
					}
				}
				Thread.yield();
			}
		}
	}
	
	protected DriverStation ap_ds;
    private int port; 
    private int[] monitor;
    private List<ButtonListener> bl;
    private List<AxisListener> al;
    private Background bg;
    
    public Controller(int port)
	{
	    this.port = port;
	    ap_ds = DriverStation.getInstance();
	}

	/**
	 * Get the value of the axis.
	 *
	 * @param axis The axis to read [1-6].
	 * @return The value of the axis.
	 */
	float getRawAxis(int axis)
	{
	    return (float) ap_ds.getStickAxis(port, (int) axis);
	}

	/**
	 * Get the button value for buttons 1 through 12.
	 *
	 * The buttons are returned in a single 16 bit value with one bit representing
	 * the state of each button. The appropriate button is returned as a boolean
	 * value.
	 *
	 * @param button The button number to be read.
	 * @return The state of the button.
	 **/
	boolean getNumberedButton(int button)
	{
	    return ((0x1 << (button-1)) & ap_ds.getStickButtons(port)) != 0;
	}
	
	public int getControllerNumber()
	{
		return port;
	}
	
	protected void setMonitor(int[] monitor)
	{
		this.monitor = monitor;
	}
	
	public void addButtonListener(ButtonListener b)
	{
		if(bl == null) { bl = new ArrayList<>(); }
		bl.add(b);
	}
	
	public void addAxisListener(AxisListener a)
	{
		if(al == null) { al = new ArrayList<>(); }
		al.add(a);
	}
	
	public void startThread()
	{
		stopThread();
		bg = new Background();
		new Thread(bg).start();
	}
	
	public void stopThread()
	{
		bg.running = false;
	}
}
