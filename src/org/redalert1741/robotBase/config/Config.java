package org.redalert1741.robotBase.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Config 
{
	private static Map<String, Double> settings;
	private static List<Configurable> configurables; 

	public static void dumpConfig()
	{
		System.out.println("DUMP");
		for(Map.Entry<String, Double> e : settings.entrySet())
		{
			System.out.println(e.getKey() + ": " + e.getValue());
		}
		System.out.println("END DUMP");
	}
	
	public static boolean loadFromFile(String filename)
	{
		return parse(filename);
	}

	public static double getSetting(String name, double reasonable_default)
	{
		double retval = reasonable_default;
		name = name.toLowerCase();
		
		if (settings.containsKey(name)) 
		{
			retval = settings.get(name);
		}

		return retval;
	}

	public static void setSetting(String name, double value)
	{
		name = name.toLowerCase();
		settings.put(name, value);
	}

	static boolean parse(String filename)
	{
		settings = new HashMap<String,Double>();
		Scanner infile;
		try
		{
			infile = new Scanner(new File(filename));
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Couldn't find config file \"" + filename + "\"");
			return false;
		} 
		
		/*
		 * Match exactly 0 '#'
		 * Match as many alphanumeric characters + '_'
		 * Optional ' ' around an '='
		 * Match a number, with an optional '.' and more numbers
		 */
		Pattern p = Pattern.compile("(#{0})[\\w\\d_]+ ?= ?-?\\d+(\\.\\d+)?");
		
		while(infile.hasNextLine())
		{
			String in = infile.nextLine();
			if(p.matcher(in).matches())
			{
				String[] key = in.split(" ?=");
				Double value = Double.parseDouble(key[1]);
				settings.put(key[0].toLowerCase(), value);
				System.out.println(key[0] + ": " + value);
			}
			else if(!in.startsWith("#") && !in.startsWith("\n"))
			{
				System.out.println("Could not parse line \"" + in + "\"");
			}
		}
		infile.close();
		return true;
	}
	
	public static void addConfigurable(Configurable c)
	{
		if(configurables == null) { configurables = new ArrayList<>(); }
		configurables.add(c);
	}
	
	public static void reloadConfig()
	{
		for(Configurable c : configurables)
		{
			c.reloadConfig();
		}
	}

}
