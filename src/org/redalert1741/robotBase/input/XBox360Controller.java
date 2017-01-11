package org.redalert1741.robotBase.input;

public class XBox360Controller extends Controller
{
	public enum AxisType
    {
        LeftXAxis, LeftYAxis, RightXAxis, RightYAxis, LTrigger, RTrigger
    }

    public enum DPadDirection
    {
        Center, Up, UpLeft, Left, DownLeft, Down, DownRight, Right, UpRight
    }
    
    protected class AxisNumber
    {
		static final int LEFT_X_AXIS_NUM = 0;
		static final int LEFT_Y_AXIS_NUM = 1;
		static final int LEFT_TRIGGER_AXIS = 2;
		static final int RIGHT_TRIGGER_AXIS = 3;
		static final int RIGHT_X_AXIS_NUM = 4;
		static final int RIGHT_Y_AXIS_NUM = 5;
		static final int DPAD_X_AXIS_NUM = 6;
		static final int DPAD_Y_AXIS_NUM = 7;
	}

	protected class StickButton
	{
		static final int LEFT_ANALOG_STICK_BUTTON = 11;
		static final int RIGHT_ANALOG_STICK_BUTTON = 12;
	}
	
	XBox360Controller(int port)
	{
		super(port);
		setMonitor(new int[]{12, 8});
	}
	
	/**
	 * Get the X value of the left analog stick.
	 */
	float getLeftX()
	{
	    return getRawAxis(AxisNumber.LEFT_X_AXIS_NUM);
	}

	/**
	 * Get the Y value of the left analog stick.
	 */
	float getLeftY()
	{
	    return getRawAxis(AxisNumber.LEFT_Y_AXIS_NUM);
	}

	/**
	 * Get the X value of the right analog stick.
	 */
	float getRightX()
	{
	    return getRawAxis(AxisNumber.RIGHT_X_AXIS_NUM);
	}

	/**
	 * Get the Y value of the right analog stick.
	 */
	float getRightY()
	{
	    return getRawAxis(AxisNumber.RIGHT_Y_AXIS_NUM);
	}
	
	/**
	 * Return the axis determined by the argument.
	 *
	 * This is for cases where the gamepad axis is returned programatically,
	 * otherwise one of the previous functions would be preferable (for example
	 * GetLeftX()).
	 *
	 * @param axis The axis to read.
	 * @return The value of the axis.
	 */
	float getAxis(AxisType axis)
	{
	    switch(axis)
	    {
	        case LeftXAxis: return getLeftX();
	        case LeftYAxis: return getLeftY();
	        case RightXAxis: return getRightX();
	        case RightYAxis: return getRightY();
	        case LTrigger: return getLTriggerAxis();
	        default:
	            //wpi_fatal(BadJoystickAxis);
	            return (float) 0.0;
	    }
	}
	
	/**
	 * Gets whether or not the left analog stick is depressed.
	 *
	 * @return The state of the left analog stick button.
	 */
	boolean getLeftPush()
	{
	    return getNumberedButton(StickButton.LEFT_ANALOG_STICK_BUTTON);
	}

	/**
	 * Gets whether or not the right analog stick is depressed.
	 *
	 * @return The state of the right analog stick button.
	 */
	boolean getRightPush()
	{
	    return getNumberedButton(StickButton.RIGHT_ANALOG_STICK_BUTTON);
	}
	
    boolean getA() { return getNumberedButton(1); }
    boolean getB() { return getNumberedButton(2); }
    boolean getY() { return getNumberedButton(4); }
    boolean getX() { return getNumberedButton(3); }
    boolean getLeftBumper() { return getNumberedButton(5); }
    boolean getRightBumper() { return getNumberedButton(6); }
    boolean getBack() { return getNumberedButton(7); }
    boolean getStart() { return getNumberedButton(8); }
    boolean getLeftTrigger() { return getNumberedButton(7); }
    boolean getRightTrigger() { return getNumberedButton(9); }
    float getLTriggerAxis() { return getRawAxis(AxisNumber.LEFT_TRIGGER_AXIS); }
    float getRTriggerAxis() { return getRawAxis(AxisNumber.RIGHT_TRIGGER_AXIS); }
    
	/*
	 * Returns a DPad Direction, not degrees.
	 */
	DPadDirection getDPad()
	{
	    int pos = ap_ds.getStickPOV(getControllerNumber(), 0);

	    if (pos == 315)
	        return DPadDirection.UpLeft;
	    if (pos == 225)
	        return DPadDirection.DownLeft;
	    if (pos == 135)
	        return DPadDirection.DownRight;
	    if (pos == 45)
	        return DPadDirection.UpRight;
	    if (pos == 0)
	        return DPadDirection.Up;
	    if (pos == 270)
	        return DPadDirection.Left;
	    if (pos == 180)
	        return DPadDirection.Down;
	    if (pos == 90)
	        return DPadDirection.Right;

	  return DPadDirection.Center;
	}
	
	/*
	 * AKA getDpadRaw()
	 */
	int getPOV()
	{
		return ap_ds.getStickPOV(getControllerNumber(), 0);
	}
}
