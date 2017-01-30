package org.redalert1741.steamworks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import edu.wpi.first.wpilibj.Timer;

public class JsonAutonomous extends Autonomous
{
	private JsonElement auto;
	private List<AutoInstruction> instructions;
	private int step;
	private Timer timer;
	
	private enum Unit { Seconds, Milliseconds, EncoderTicks, Rotations, Inches, Feet, Invalid };
	
	private static class AutoInstruction
	{
		public String type;
		public Unit unit;
		public double amount;
		public List<Double> args;
		public AutoInstruction(String type, Unit unit, double amount, List<Double> args)
		{
			this.type = type;
			this.unit = unit;
			this.amount = amount;
			this.args = args;
		}
	}
	
	public JsonAutonomous(String file)
	{
		step = -1;
		timer = new Timer();
		instructions = new ArrayList<AutoInstruction>();
		try
		{
			auto = new JsonParser().parse(new JsonReader(new FileReader(new File(file))));
			JsonElement inner = auto.getAsJsonObject().get("auto");
			if(inner.isJsonArray())
			{
				for(JsonElement e : inner.getAsJsonArray())
				{
					JsonObject o = e.getAsJsonObject();
					List<Double> tmp = new ArrayList<Double>();
					for(JsonElement e2 : o.get("args").getAsJsonArray())
					{
						tmp.add(e2.getAsDouble());
					}
					instructions.add(new AutoInstruction(o.get("type").getAsString(), parseUnit(o.get("unit").getAsString()), o.get("amount").getAsDouble(), tmp));
				}
			}
		}
		catch (JsonIOException | JsonSyntaxException | FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Unit parseUnit(String in)
	{
		return Unit.valueOf(in);
	}

	@Override
	public void run()
	{
		if(step == -1)
		{
			timer.reset();
			timer.start();
		}
		AutoInstruction ai = instructions.get(step);
		if(ai.type.equals("drive"))
		{
			System.out.println("Drive x: " + ai.args.get(0) + ", y: " + ai.args.get(1) + ", z: " + ai.args.get(2) + ", " + ai.amount + " " + ai.unit);
			if(ai.unit.equals(Unit.Seconds))
			{
				if(timer.get() < ai.amount)
				{
					Robot.drive.swerveAbsolute(ai.args.get(0), ai.args.get(1), ai.args.get(2), 0, false);
				}
				else
				{
					step++;
					timer.reset();
					timer.start();
				}
			}
		}
		else if(ai.type.equals("gear"))
		{
			System.out.println("Gear " + (ai.args.get(0)==1?"open":"close"));
		}
		else if(ai.type.equals("wait"))
		{
			System.out.println("Wait " + ai.amount + " " + ai.unit);
		}
	}

}
