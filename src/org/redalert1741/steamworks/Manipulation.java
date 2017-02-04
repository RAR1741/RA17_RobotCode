package org.redalert1741.steamworks;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

public class Manipulation 
{
	CANTalon input;
	CANTalon output;
	
	public Manipulation(CANTalon in,CANTalon out)
	{
		input = in;
		output = out;
		input.changeControlMode(TalonControlMode.PercentVbus);
		output.changeControlMode(TalonControlMode.PercentVbus);
	}
	
	public void setInput(double power)
	{
		input.set(power);
	}
	
	public void setOutput(double power)
	{
		output.set(power);
	}
}
