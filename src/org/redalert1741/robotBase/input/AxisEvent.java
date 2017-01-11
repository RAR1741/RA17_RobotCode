package org.redalert1741.robotBase.input;

public class AxisEvent
{
	private int axisNum;
	private float a;
	private Controller c;
	
	public AxisEvent(Controller controller, int axis, float val)
	{
		axisNum = axis;
		c = controller;
		a = val;
	}
	
	public int getControllerNumber()
	{
		return c.getControllerNumber();
	}
	
	public int getAxisNumber()
	{
		return axisNum;
	}
	
	public float getValue()
	{
		return a;
	}
}
