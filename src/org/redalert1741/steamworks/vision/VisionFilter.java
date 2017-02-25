package org.redalert1741.steamworks.vision;

import java.util.ArrayList;

import org.opencv.core.Rect;

public interface VisionFilter
{
	public abstract Rect getBestRect(ArrayList<Rect> rect);
	public abstract ArrayList<Rect> getFilteredRects(ArrayList<Rect> rect);
}
