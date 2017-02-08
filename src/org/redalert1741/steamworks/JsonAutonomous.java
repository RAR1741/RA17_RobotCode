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
	private double start;
	private double navxStart;
	private final double TICKS_PER_ROTATION = 133.4;
	private final double TICKS_PER_INCH = TICKS_PER_ROTATION / (4 * Math.PI);
	
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
			reset();
		}
		AutoInstruction ai = instructions.get(step);
		if(ai.type.equals("drive"))
		{
			drive(ai);
		}
		else if(ai.type.equals("gear"))
		{
			gear(ai);
		}
		else if(ai.type.equals("wait"))
		{
			System.out.println("Wait " + ai.amount + " " + ai.unit);
		}
	}
	
	private boolean driveTime(double x, double y, double z, double t)
	{
		if(timer.get() < t)
		{
			Robot.drive.swerveAbsolute(x, y, z, 0, false);
		}
		else
		{
			return true;
		}
		return false;
	}
	
	private boolean driveDistance(double x, double y, double z, double a)
	{
		if(Robot.drive.FRM.getDriveEnc()-start < a)
		{
			Robot.drive.swerveAbsolute(x, y, z, 0, false);
		}
		else
		{
			return true;
		}
		return false;
	}
	
	private boolean rotateDegrees(double speed, double amt)
	{
		if(amt < Robot.navx.getAngle()-navxStart) { return true; }
		Robot.drive.swerveAbsolute(0, 0, speed, Robot.navx.getAngle(), false);
		return false;
	}
	
	/**
	 * Resets all of the variables used for a single step and increments the step counter
	 */
	private void reset()
	{
		step++;
		timer.reset();
		timer.start();
		start = Robot.drive.FRM.getDriveEnc();
		navxStart = Robot.navx.getAngle();
	}
	
	/**
	 * Processes gear WIP
	 * @param ai
	 */
	public void gear(AutoInstruction ai)
	{
		System.out.println("Gear " + (ai.args.get(0)==1?"open":"close"));
	}
	
	public void driveTranslation(AutoInstruction ai)
	{
		System.out.println("Drive Translation: x: " + ai.args.get(0) + ", y: " + ai.args.get(1));
		drive(ai);
	}
	
	public void driveRotation(AutoInstruction ai)
	{
		System.out.println("Drive Rotation: a: " + ai.args.get(0) + ", " + ai.amount + " " + ai.unit);
		if(rotateDegrees(ai.args.get(0), ai.unit.equals(Unit.Degrees) ? ai.amount : ai.amount*360.0))
		{
			reset();
		}
	}
	
	/**
	 * Processes a three-arg drive instruction 
	 * @param ai
	 */
	public void drive(AutoInstruction ai)
	{
		System.out.println("Drive x: " + ai.args.get(0) + ", y: " + ai.args.get(1) + ", z: " + ai.args.get(2) + ", " + ai.amount + " " + ai.unit);
		Unit u = ai.unit;
		if(u.equals(Unit.Seconds) || u.equals(Unit.Milliseconds))
		{
			if(driveTime(ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.Seconds) ? ai.amount : ai.amount/1000.0)))
			{
				reset();
			}
		}
		else if(u.equals(Unit.EncoderTicks) || u.equals(Unit.Rotations))
		{
			if(driveDistance(ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.EncoderTicks) ? ai.amount : ai.amount/TICKS_PER_ROTATION)))
			{
				reset();
			}
		}
		else if(u.equals(Unit.Feet) || u.equals(Unit.Inches))
		{
			if(driveDistance(ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.Inches) ? ai.amount/TICKS_PER_INCH : (ai.amount/TICKS_PER_INCH))/12.0))
			{
				reset();
			}
		}
	}

}
