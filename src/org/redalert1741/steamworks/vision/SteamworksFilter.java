package org.redalert1741.steamworks.vision;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Rect;
import org.redalert1741.robotBase.config.Config;

public class SteamworksFilter implements VisionFilter
{
	@Override
	public Rect getBestRect(List<Rect> rect) 
	{
		ArrayList<Rect> rekt = getFilteredRects(rect);
		if(rekt.size()>1)
		{
			rekt.sort((a,b) -> a.area() > b.area() ? -1 : 1);
		}
		Rect bestRekt = new Rect(rekt.get(0).tl(),rekt.get(1).br());
//		System.out.println(bestRekt.tl() + " " + bestRekt.br());
		return bestRekt;
	}

	public ArrayList<Rect> getFilteredRects(List<Rect> rect) 
	{
		ArrayList<Rect> bestRects = new ArrayList<Rect>();
		double aspect = Config.getSetting("TargetAspectRatio", .4);
		double range = Config.getSetting("AspectRatioRange",0.07);
		if(!(rect.isEmpty()))
		{
			for(Rect r : rect)
			{
				if(Math.abs(aspect - ((double)r.width/r.height)) < range)
				{
					System.out.println((double)r.width/r.height);
					bestRects.add(r);
				}
			}
			return bestRects;
		}
		return new ArrayList<Rect>();
	}
}
