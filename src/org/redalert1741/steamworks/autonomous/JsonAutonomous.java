package org.redalert1741.steamworks.autonomous;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.steamworks.Autonomous;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PIDOutput;

public class JsonAutonomous extends Autonomous implements PIDOutput, Configurable
{
	private JsonElement auto;
	private List<AutoInstruction> instructions;
	
	private FileReader fr;
	private JsonReader jr;
	private JsonParser jp;
	
	protected DriverStation ap_ds;
	
	private enum Unit { Seconds, Milliseconds, EncoderTicks, Rotations, Inches, Feet, Degrees, Invalid };
	
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
	
	/**
	 * Creates a JsonAutonomous from the specified file
	 * @param file The location of the file to parse
	 */
	public JsonAutonomous(String file)
	{
		parseFile(file);
	}
	
	public void parseFile(String file)
	{
		reloadConfig();
		instructions = new ArrayList<AutoInstruction>();
		try
		{
			//System.out.println(new File(file).exists());
			fr = new FileReader(new File(file));
			jr = new JsonReader(fr);
			jp = new JsonParser();
			auto = jp.parse(jr);
			//auto = new JsonParser().parse(new JsonReader(new FileReader(new File(file))));
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
		
	}

	@Override
	public void reloadConfig()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pidWrite(double output)
	{
		// TODO Auto-generated method stub
		
	}

}
