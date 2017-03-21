package org.redalert1741.steamworks.vision;

import java.util.List;

import org.opencv.core.Rect;

public interface VisionFilter
{
	public abstract Rect getBestRect(List<Rect> rect);
}
