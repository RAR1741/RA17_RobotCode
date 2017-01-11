package org.redalert1741.robotBase.input;

public class ButtonEvent
{
	private int buttonNum;
	private Controller controller;
	
	public ButtonEvent(Controller p, int button)
	{
		buttonNum = button;
		controller = p;
	}
	
	public int getButton()
	{
		return buttonNum;
	}
	
	public int getControllerNumber()
	{
		return controller.getControllerNumber();
	}
}
