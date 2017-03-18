package org.redalert1741.steamworks.autonomous;

import org.redalert1741.steamworks.Robot;

public class DriveTimeConsumer extends TimedConsumer
{
	@Override
	public void update()
	{
		Robot.drive.swerveAbsolute(ai.args.get(0), ai.args.get(1), ai.args.get(2), 0, false);
	}

	@Override
	public void finish()
	{
		Robot.drive.swerveAbsolute(0,0,0,0,false);
	}
}
