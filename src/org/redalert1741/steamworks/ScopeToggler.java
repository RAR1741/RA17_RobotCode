package org.redalert1741.steamworks;

import edu.wpi.first.wpilibj.DigitalOutput;

/**
 * Handles digital output ports for oscilloscope measurement of loop timing
 */
public class ScopeToggler {
	private DigitalOutput toggleOutput;
	private DigitalOutput highLowOutput;
	boolean oldState;
	/**
	 * Creates new ScopeToggler
	 * @param togglePort int DigitalOutput port for toggles
	 * @param highLow int DigitalOutput port for high/low cycle
	 */
	public ScopeToggler(int togglePort, int highLow) {
		toggleOutput = new DigitalOutput(togglePort);
		highLowOutput = new DigitalOutput(highLow);
		oldState = false;
	}
	
	/**
	 * Takes action at start of target loop
	 */
	public void startLoop() {
		// toggle
		oldState = ! oldState;
		toggleOutput.set(oldState);
		
		// set high
		highLowOutput.set(true);
	}
	
	/**
	 * Takes action at end of target loop
	 */
	public void endLoop() {
		highLowOutput.set(false);
	}
}
