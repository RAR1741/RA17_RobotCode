package org.redalert1741.steamworks.autonomous;

public interface AutoInstructionConsumer
{
	public abstract void init(AutoInstruction ai);
	public abstract void update();
	public abstract void finish();
	public abstract boolean isComplete();
}
