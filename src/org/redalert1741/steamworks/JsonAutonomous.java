package org.redalert1741.steamworks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.config.Configurable;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.Timer;

public class JsonAutonomous extends Autonomous implements PIDOutput, Configurable
{
	private JsonElement auto;
	private List<AutoInstruction> instructions;
	private int step;
	private Timer timer;
	private double start;
	private double navxStart;
	private PIDController turn;
	private PIDController straighten;
	private double turnSpeed;
	private boolean edge;
	private final double TICKS_PER_ROTATION = 533.4;
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
	
	/**
	 * Creates a JsonAutonomous from the specified file
	 * @param file The location of the file to parse
	 */
	public JsonAutonomous(String file)
	{
		//TODO extract to config
		turn = new PIDController(0.02, 0, 0, Robot.navx, this);
		turn.setInputRange(-180, 180);
		turn.setOutputRange(-0.7, 0.7);
		turn.setAbsoluteTolerance(0.5);
		turn.setContinuous(true);
		
		straighten = new PIDController(0.001, 0, 0, Robot.navx, this);
		straighten.setInputRange(-180, 180);
		straighten.setOutputRange(-0.7, 0.7);
		straighten.setAbsoluteTolerance(0);
		straighten.setContinuous(true);
		
		reloadConfig();
		
		step = -1;
		timer = new Timer();
		instructions = new ArrayList<AutoInstruction>();
		try
		{
			//System.out.println(new File(file).exists());
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
		if(instructions.size() == step)
		{
			Robot.drive.swerveAbsolute(0, 0, 0, 0, false);
			return;
		}
		AutoInstruction ai = instructions.get(step);
		if(ai.type.equals("drive"))
		{
			drive(ai);
		}
		else if(ai.type.equals("drive-t"))
		{
			driveTranslation(ai);
		}
		else if(ai.type.equals("drive-r"))
		{
			driveRotation(ai);
		}
		else if(ai.type.equals("gear"))
		{
			gear(ai);
		}
		else if(ai.type.equals("wait"))
		{
			Robot.drive.swerveAbsolute(0, 0, 0, 0, false);
			if(timer.get() > (ai.unit.equals(Unit.Seconds)?ai.amount:ai.amount/1000.0))
			{
				reset();
			}
			//System.out.println("Wait " + ai.amount + " " + ai.unit);
		}
	}
	
	/**
	 * Helper function for {@link #drive(AutoInstruction)}
	 * @param x X translation speed
	 * @param y Y translation speed
	 * @param z Rotational speed
	 * @param t Time to drive
	 * @return Whether drive has completed
	 */
	private boolean driveTime(double x, double y, double z, double t)
	{
		if(timer.get() < t)
		{
			Robot.drive.swerveAbsolute(x, y, z, Robot.navx.getAngle()-navxStart, false);
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
			Robot.drive.swerveAbsolute(x, y, z, -(Robot.navx.getAngle()-navxStart), false);
		}
		else
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Helper function for {@link #driveRotation(AutoInstruction)}
	 * @param speed Unused
	 * @param amt Unused
	 * @return Whether turn has completed
	 */
	private boolean rotateDegrees(double speed, double amt)
	{
		turnSpeed = turn.get();
		if(Math.abs(turnSpeed) < 0.01 && turnSpeed != 0) { return true; }
		Robot.drive.swerveAbsolute(0, 0, -turnSpeed, Robot.navx.getAngle(), false);
		return false;
	}
	
	/**
	 * Resets all of the variables used for a single auto step and increments the step counter
	 */
	private void reset()
	{
		turn.disable();
		straighten.disable();
		step++;
		timer.reset();
		timer.start();
		start = Robot.drive.FRM.getDriveEnc();
		navxStart = Robot.navx.getAngle();
		edge = true;
	}
	
	/**
	 * Processes gear WIP
	 * @param ai
	 */
	public void gear(AutoInstruction ai)
	{
		//System.out.println("Gear " + (ai.args.get(0)==1?"open":"close"));
	}
	
	/**
	 * Drives in a straight line while maintaining current rotation
	 * @see AutoInstruction
	 * @param ai AutoInstruction to use
	 */
	public void driveTranslation(AutoInstruction ai)
	{
		//System.out.println("Drive Translation: x: " + ai.args.get(0) + ", y: " + ai.args.get(1));
		if(edge)
		{
			straighten.enable();
			straighten.setSetpoint(Robot.navx.getAngle());
			edge=false;
		}
		if(ai.args.size() == 3)
		{
			ai.args.set(2, -straighten.get());
		}
		else
		{
			ai.args.add(-straighten.get());
		}
		drive(ai);
	}
	
	/**
	 * Rotates the robot without moving translationally
	 * @see AutoInstruction
	 * @param ai Instruction to use
	 */
	public void driveRotation(AutoInstruction ai)
	{
		turn.enable();
		if(edge)
		{
			//System.out.println("setpoint " + Robot.navx.getAngle()+ai.amount);
			turn.setSetpoint(Robot.navx.getAngle()+ai.amount);
			edge = false;
		}
		//System.out.println("Drive Rotation: a: " + ai.args.get(0) + ", " + ai.amount + " " + ai.unit);
		if(rotateDegrees(ai.args.get(0), ai.unit.equals(Unit.Degrees) ? ai.amount : ai.amount*360.0))
		{
			reset();
			turn.disable();
		}
	}
	
	/**
	 * Processes a three-argument drive instruction 
	 * @param ai
	 */
	public void drive(AutoInstruction ai)
	{
		//System.out.println("Drive x: " + ai.args.get(0) + ", y: " + ai.args.get(1) + ", z: " + ai.args.get(2) + ", " + ai.amount + " " + ai.unit);
		System.out.println(ai.args.get(2));
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
			if(driveDistance(ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.EncoderTicks) ? ai.amount : ai.amount*TICKS_PER_ROTATION)))
			{
				reset();
			}
		}
		else if(u.equals(Unit.Feet) || u.equals(Unit.Inches))
		{
			
			if(driveDistance(ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.Inches) ? ai.amount*TICKS_PER_INCH : (ai.amount*TICKS_PER_INCH))*12.0))
			{
				reset();
			}
		}
	}

	@Override
	public void pidWrite(double output)
	{
		turnSpeed = output;
	}

	@Override
	public void reloadConfig()
	{
		turn.setPID(Config.getSetting("auto_turn_p", 0.2), 
				Config.getSetting("auto_turn_i", 0),
				Config.getSetting("auto_turn_d", 0));
		straighten.setPID(Config.getSetting("auto_straight_p", 0.2), 
				Config.getSetting("auto_straight_i", 0),
				Config.getSetting("auto_straight_d", 0));
	}

}
