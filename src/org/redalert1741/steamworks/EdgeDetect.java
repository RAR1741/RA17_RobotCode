package org.redalert1741.steamworks;

public class EdgeDetect 
{
	private boolean state;
	
	EdgeDetect()
	{
		state = false;
	}
	
	boolean Check(boolean in)
	{
		boolean out;
		if(in && state)
		{
			out = true;
		}
		else
		{
			out = false;
		}

		if(in)
		{
			state = false;
		}
		else
		{
			state = true;
		}
		return out;
	}
}
