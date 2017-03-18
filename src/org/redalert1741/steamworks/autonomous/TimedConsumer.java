package org.redalert1741.steamworks.autonomous;

import edu.wpi.first.wpilibj.Timer;

public abstract class TimedConsumer implements AutoInstructionConsumer
{
	protected AutoInstruction ai;
	protected Timer timer;
	
	public TimedConsumer()
	{
		timer = new Timer();
	}

	@Override
	public void init(AutoInstruction ai)
	{
		this.ai = ai;
		timer.stop();
		timer.reset();
		timer.start();
	}

	@Override
	public abstract void update();

	@Override
	public boolean isComplete()
	{
		return timer.hasPeriodPassed(ai.amount / (ai.unit.equals(Unit.Seconds) ? 1.0 : 1000.0));
	}

}
