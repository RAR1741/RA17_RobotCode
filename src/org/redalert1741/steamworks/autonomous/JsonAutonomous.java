package org.redalert1741.steamworks.autonomous;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.redalert1741.robotBase.config.Config;
import org.redalert1741.robotBase.config.Configurable;
import org.redalert1741.steamworks.FakePIDSource;
import org.redalert1741.steamworks.Robot;
import org.redalert1741.steamworks.vision.VisionThread;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

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
	private PIDController track;
	private FakePIDSource cameraSource;
	private double turnSpeed;
	private boolean red;
	private boolean edge;
	private final double TICKS_PER_ROTATION = 533.4;
	private final double TICKS_PER_INCH = TICKS_PER_ROTATION / (4 * Math.PI);
	private final double TICKS_PER_DEGREE = TICKS_PER_INCH * 0.30531;
	
	private FileReader fr;
	private JsonReader jr;
	private JsonParser jp;
	
	protected DriverStation ap_ds;
	
	/**
	 * Creates a JsonAutonomous from the specified file
	 * @param file The location of the file to parse
	 */
	public JsonAutonomous(String file)
	{
		ap_ds = DriverStation.getInstance();
		turn = new PIDController(0.02, 0, 0, Robot.navx, this);
		turn.setInputRange(-180, 180);
		turn.setOutputRange(-0.7, 0.7);
		turn.setAbsoluteTolerance(0.5);
		turn.setContinuous(true);
		
		straighten = new PIDController(0.01, 0, 0, Robot.navx, this);
		straighten.setInputRange(-180, 180);
		straighten.setOutputRange(-0.7, 0.7);
		straighten.setAbsoluteTolerance(0);
		straighten.setContinuous(true);
		
		cameraSource = new FakePIDSource(0,-180,180);
		
		track = new PIDController(Config.getSetting("AutoAimP", 0.01),
				Config.getSetting("AutoAimI", 0),
				Config.getSetting("AutoAimD", 0),cameraSource,this);
		track.setInputRange(-180,180);
		track.setOutputRange(-0.1,0.1);
		track.setAbsoluteTolerance(0.5);
		track.setContinuous(true);
		
		turn.setPID(Config.getSetting("auto_turn_p", 0.02), 
				Config.getSetting("auto_turn_i", 0.00001),
				Config.getSetting("auto_turn_d", 0));
		straighten.setPID(Config.getSetting("auto_straight_p", 0.2), 
				Config.getSetting("auto_straight_i", 0),
				Config.getSetting("auto_straight_d", 0));
		parseFile(file);
	}
	
	public void parseFile(String file)
	{
		reloadConfig();
		
		step = -1;
		timer = new Timer();
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
		Alliance a = ap_ds.getAlliance();
		if(a == Alliance.Red)
		{
			red = true;
		}
		else
		{
			red = false;
		}
		
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
			drive(ai, false, red);
		}
		else if(ai.type.equals("drive-t"))
		{
			driveTranslation(ai, red);
		}
		else if(ai.type.equals("drive-fo"))
		{
			driveFieldOriented(ai,red);
		}
//		else if(ai.type.equals("drive-r"))
//		{
//			driveRotation(ai);
//		}
		else if(ai.type.equals("drive-track"))
		{
			driveVTrack(ai);
		}
		else if(ai.type.equals("gear"))
		{
			gear(ai);
		}
		else if(ai.type.equals("shoot"))
		{
			shoot(ai);
		}
		else if(ai.type.equals("brake"))
		{
			brake(ai);
		}
		else if(ai.type.equals("turnWheels"))
		{
			turnWheels(ai,red);
		}
		else if(ai.type.equals("intake"))
		{
			intake(ai);
		}
//		else if(ai.type.equals("tankDrive"))
//		{
//			tankDrive(ai);
//		}
		else if(ai.type.equals("turnDeg"))
		{
			turnDegrees(ai, red);
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
		else
		{
			System.out.println("Invalid Command");
			reset();
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
	private boolean driveTime(double x, double y, double z, double t, boolean fieldOrient)
	{
		if(timer.get() < t)
		{
			Robot.drive.swerveAbsolute(x, y, 0,-Robot.navx.getAngle()+navxStart, fieldOrient);
		}
		else
		{
			return true;
		}
		return false;
	}
	
	private boolean driveDistance(double x, double y, double z, double a, boolean fieldOrient)
	{
		if(Math.abs(Robot.drive.FRM.getDriveEnc()-start) < a)
		{
			Robot.drive.swerveAbsolute(x, y, z, -Robot.navx.getAngle()+navxStart, fieldOrient);
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
		if(Math.abs(Robot.navx.getAngle()-navxStart-amt) < 0.5) { return true; }
		Robot.drive.swerveAbsolute(0, 0, -turnSpeed, Robot.navx.getAngle(), false);
		return false;
	}
	
	public void shoot(AutoInstruction ai)
	{
		Robot.shooter.setSpeed(Config.getSetting("something", -1640));
		if(timer.get() > ai.args.get(0))
		{
			Robot.carousel.forward();
			if(timer.get() > ai.args.get(1))
			{
				Robot.shooter.stop();
				Robot.carousel.stop();
				reset();
			}
		}
	}
	
	public void turnDegrees(AutoInstruction ai, boolean r)
	{
		if(Math.abs(Robot.drive.FRM.getDriveEnc()-start) < ai.amount * TICKS_PER_DEGREE)
		{
			Robot.drive.swerveAbsolute(0, 0, r ? -ai.args.get(0) : ai.args.get(0), 0, false);
		}
		else
		{
			Robot.drive.swerveAbsolute(0, 0, 0, 0, false);
			reset();
		}
	}
	
	/**
	 * Resets all of the variables used for a single auto step and increments the step counter
	 */
	private void reset()
	{
		turn.disable();
		straighten.disable();
		step++;
		Robot.drive.swerveAbsolute(0, 0, 0, 0, false);
		timer.reset();
		timer.start();
		start = Robot.drive.FRM.getDriveEnc();
		navxStart = Robot.navx.getAngle();
		edge = true;
	}
	
	/**
	 * Stop the Robit and wait for a time
	 * @param ai
	 */
	public void brake(AutoInstruction ai)
	{
		Robot.drive.swerveAbsolute(0, 0, 0, 0, false);
		Robot.drive.setBrake();
		if(timer.get() > ai.args.get(0))
		{
			Robot.drive.setCoast();
			reset();
		}
	}
	
	/**
	 * turns wheels to position
	 * @param ai
	 */
	public void turnWheels(AutoInstruction ai, boolean r)
	{
		Robot.drive.swerveAbsolute(r ? -ai.args.get(0) : ai.args.get(0), ai.args.get(1), 0, -Robot.navx.getAngle()+navxStart, true);
		if(timer.get() > ai.amount)
		{
			reset();
		}
	}
	
	/**
	 * Processes gear
	 * @param ai
	 */
	public void gear(AutoInstruction ai)
	{
		if(ai.args.get(0) == 1)
		{
			Robot.gear.open();
		}
		else
		{
			Robot.gear.close();
		}
		reset();
	}
	
	public void intake(AutoInstruction ai)
	{
		Robot.manip.setInput(Robot.isCompetition() ? -1 : 0.6, Robot.isCompetition() ? 0.7 : -0.7);
		if(timer.get() > ai.args.get(0))
		{
			Robot.manip.setInput(0,0);
			reset();
		}
	}
	
	public void tankDrive(AutoInstruction ai)
	{
		System.out.println("tank");
		if(Robot.drive.FRM.getDriveEnc()-start < (ai.amount*TICKS_PER_INCH)*12.0)
		{
			Robot.drive.tankDrive(ai.args.get(0), ai.args.get(1));
			System.out.println(ai.args.get(0) + ai.args.get(1));
		}
		else
		{
			reset();
		}
	}
	
	/**
	 * Drives in a straight line while maintaining current rotation and also field oriented
	 * @see AutoInstruction
	 * @param ai AutoInstruction to use
	 */
	public void driveFieldOriented(AutoInstruction ai, boolean r)
	{
		//System.out.println("Drive Translation: x: " + ai.args.get(0) + ", y: " + ai.args.get(1));
		if(ai.args.size() == 3)
		{
			ai.args.set(2, 0.0);
		}
		else
		{
			ai.args.add(0.0);
		}
		drive(ai, true, r);
	}
	
	/**
	 * Drives in a straight line while maintaining current rotation
	 * @see AutoInstruction
	 * @param ai AutoInstruction to use
	 */
	public void driveTranslation(AutoInstruction ai, boolean r)
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
		drive(ai, false, r);
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
	
	public void driveVTrack(AutoInstruction ai)
	{
		if(edge)
		{
			track.enable();
			edge=false;
		}
		if(ai.args.size() == 3)
		{
			cameraSource.pidSet(-VisionThread.getHorizontalAngle());
			track.setSetpoint(0);
			ai.args.set(2, track.get());
		}
		else
		{
			cameraSource.pidSet(-VisionThread.getHorizontalAngle());
			track.setSetpoint(0);
			ai.args.add(track.get());
		}
		
		System.out.println("Thing: " + ai.args.toString());
		drive(ai,false,false);
	}
	
	
	/**
	 * Processes a three-argument drive instruction 
	 * @param ai
	 */
	public void drive(AutoInstruction ai, boolean fieldOrient, boolean r)
	{
		//System.out.println("Drive x: " + ai.args.get(0) + ", y: " + ai.args.get(1) + ", z: " + ai.args.get(2) + ", " + ai.amount + " " + ai.unit);
		System.out.println(ai.args.get(2));
		Unit u = ai.unit;
		if(u.equals(Unit.Seconds) || u.equals(Unit.Milliseconds))
		{
			if(driveTime(r ? -ai.args.get(0) : ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.Seconds) ? ai.amount : ai.amount/1000.0), fieldOrient))
			{
				reset();
			}
		}
		else if(u.equals(Unit.EncoderTicks) || u.equals(Unit.Rotations))
		{
			if(driveDistance(r ? -ai.args.get(0) : ai.args.get(0), ai.args.get(1), ai.args.get(2), (u.equals(Unit.EncoderTicks) ? ai.amount : ai.amount*TICKS_PER_ROTATION), fieldOrient))
			{
				reset();
			}
		}
		else if(u.equals(Unit.Feet) || u.equals(Unit.Inches))
		{	
			if(driveDistance(r ? -ai.args.get(0) : ai.args.get(0),ai.args.get(1), ai.args.get(2), (u.equals(Unit.Inches) ? ai.amount*TICKS_PER_INCH : (ai.amount*TICKS_PER_INCH))*12.0, fieldOrient))
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
		turn.setPID(Config.getSetting("auto_turn_p", 0.02), 
				Config.getSetting("auto_turn_i", 0.00001),
				Config.getSetting("auto_turn_d", 0));
		straighten.setPID(Config.getSetting("auto_straight_p", 0.2), 
				Config.getSetting("auto_straight_i", 0),
				Config.getSetting("auto_straight_d", 0));
		track.setPID(Config.getSetting("AutoAimP", 0.01),
				Config.getSetting("AutoAimI", 0),
				Config.getSetting("AutoAimD", 0));
	}

}
