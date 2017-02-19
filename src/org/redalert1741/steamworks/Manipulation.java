package org.redalert1741.steamworks;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Spark;

public class Manipulation 
{
	Spark input1;
	Spark input2;
	CANTalon output;
	
	public Manipulation(int in1,int in2,int out)
	{
		input1 = new Spark(in1);
		input2 = new Spark(in2);
		output = new CANTalon(out);
		output.changeControlMode(TalonControlMode.PercentVbus);
	}
	
	public void setInput(double power1,double power2)
	{
		input1.set(power1);
		input2.set(power2);
	}
	
	public void setOutput(double power)
	{
		output.set(power);
	}
}
