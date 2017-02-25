package org.redalert1741.steamworks.vision;

import java.util.ArrayList;

import org.opencv.core.Rect;

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

	@Override
	public ArrayList<Rect> getFilteredRects(ArrayList<Rect> rect) 
	{
		return null;
	}

}
