package org.redalert1741.steamworks.vision;

import java.util.ArrayList;

import org.opencv.core.Rect;
import org.redalert1741.robotBase.config.Config;

public class SteamworksFilter implements VisionFilter {

	@Override
	public Rect getBestRect(ArrayList<Rect> rect) 
	{
		ArrayList<Rect> rekt = rect;
		if(!(rekt.isEmpty()))
		{
			Rect bestRekt = rekt.get(0);
			for(Rect r : rekt)
			{
				if(r.area() > bestRekt.area())
				{
					bestRekt = r;
				}
			}
			return bestRekt;
		}
		else
		{
			return null;
		}
	}

	@Override
	public ArrayList<Rect> getFilteredRects(ArrayList<Rect> rect) 
	{
		ArrayList<Rect> bestRects = new ArrayList<Rect>();
		double aspect = Config.getSetting("TargetAspectRatio", .4);
		double range = Config.getSetting("AspectRatioRange",0.05);
		if(!(rect.isEmpty()))
		{
			for(Rect r : rect)
			{
				if(Math.abs(aspect - (r.width/r.height)) < range)
				{
					bestRects.add(r);
				}
			}
			return bestRects;
		}
		return null;
	}

	@Override
	public ArrayList<Rect> getBestRects(ArrayList<Rect> rect) 
	{
		ArrayList<Rect> rekt = rect;
		if(!(rekt.isEmpty()) && rekt.size()>1)
		{
			ArrayList<Rect> bestRekt = new ArrayList<Rect>();
			bestRekt.add(rekt.get(0));
			bestRekt.add(rekt.get(1));
			for(Rect r : rekt)
			{
				if(r.tl().y < bestRekt.tl().y)
				{
					bestRekt = r;
				}
			}
			return bestRekt;
		}
		else
		{
			return null;
		}
	}

}
