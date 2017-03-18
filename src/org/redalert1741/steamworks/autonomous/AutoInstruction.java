package org.redalert1741.steamworks.autonomous;

import java.util.List;

public class AutoInstruction
{
	public String type;
	public Unit unit;
	public double amount;
	public List<Double> args;
	public AutoInstruction(String type, Unit unit, double amount, List<Double> args)
	{
		this.type = type;
		this.unit = unit;
		this.amount = amount;
		this.args = args;
	}
}
