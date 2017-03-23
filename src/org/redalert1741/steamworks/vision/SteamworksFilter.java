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
		double range = Config.getSetting("AspectRatioRange",0.1);
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
				if(r.area() > bestRekt.get(1).area())
				{
					if(r.area() > bestRekt.get(0).area())
					{
						bestRekt.set(0, r);
					}
					else
					{
						bestRekt.set(1, r);
					}
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
	public Rect getCombinedBestRect(ArrayList<Rect> rect) 
	{
		ArrayList<Rect> rekt = rect;
		if(!(rekt.isEmpty()) && rekt.size()>1)
		{
			ArrayList<Rect> bestRekts = new ArrayList<Rect>();
			bestRekts.add(rekt.get(0));
			bestRekts.add(rekt.get(1));
			for(Rect r : rekt)
			{
				if(r.area() > bestRekts.get(1).area())
				{
					if(r.area() > bestRekts.get(0).area())
					{
						bestRekts.set(0, r);
					}
					else
					{
						bestRekts.set(1, r);
					}
				}
			}
			Rect bestRekt = new Rect(bestRekts.get(0).tl(),bestRekts.get(1).br());
			return bestRekt;
		}
		else
		{
			return null;
		}
	}

}
