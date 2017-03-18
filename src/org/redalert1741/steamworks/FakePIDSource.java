package org.redalert1741.steamworks;

import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

public class FakePIDSource implements PIDSource
{
	private double m_value;
	protected double m_offset;
	private double m_min;
	private double m_max;
	private double m_diff;
	private double m_speed;
	private Double m_lastPos;
	
	public FakePIDSource(double offset,double min,double max)
	{
		m_offset = offset;
		m_min = min;
		m_max = max;
	}
	
	public FakePIDSource()
	{
		m_offset = 0.00f;
		m_min = Double.MIN_VALUE;
		m_max = Double.MAX_VALUE;
	}

	@Override
	public void setPIDSourceType(PIDSourceType pidSource) 
	{
		
	}

	@Override
	public PIDSourceType getPIDSourceType() 
	{
		return PIDSourceType.kDisplacement;
	}

	@Override
	public double pidGet() 
	{
		if(m_lastPos == null) { m_lastPos = m_value; }
		m_speed = m_value - m_lastPos;
		m_lastPos = m_value;
		return m_value;
	}
	
	public void pidSet(double value) 
	{
		m_value = value + m_offset;
		if(m_value <= m_max && m_value >= m_min)//checks to see if within normal range
		{
			//nothing happens, pidGet will return a proper value 
		}
		else if(m_value > m_max)//checks if too high
		{
			m_diff = m_value - m_max;//get difference
			m_value = m_min + m_diff;//sets value to wrap from max to min and add the diff
		}
		else if(m_value < m_min)//checks if too high
		{
			m_diff = m_min - m_value;//get difference
			m_value = m_max - m_diff;//sets value to wrap from min to max and subtract the diff
		}
	}
	
	public void pidSetAbsolute(double value) 
	{
		m_value = value;
	}
	
	public double getSpeed()
	{
		return m_speed;
	}
	
	public void setOffset(double offset)
	{
		m_offset = offset;
	}
	
	public double getOffset()
	{
		return m_offset;
	}
	
	public void setMinMax(double min, double max)
	{
		m_min = min;
		m_max = max;
	}
	
}

