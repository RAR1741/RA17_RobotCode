package org.redalert1741.steamworks;

public class JsonMain
{
	public static void definitelyNotTheMainFunction(String[] args)
	{
		JsonAutonomous test = new JsonAutonomous(JsonAutonomous.class.getResource("/json-test.json").getPath());
		
		test.run();
	}
}
