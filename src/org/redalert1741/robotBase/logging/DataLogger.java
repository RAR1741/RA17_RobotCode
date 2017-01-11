package org.redalert1741.robotBase.logging;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataLogger 
{
	private String filename;
	private PrintWriter log = null;
	private Map<String, String> fields;
	private List<Loggable> loggables;

	public DataLogger()
	{
		fields = new LinkedHashMap<String,String>();
		loggables = new ArrayList<>();
	}

	boolean open(String filename)
	{
		this.filename = filename;
		try
		{
			log = new PrintWriter(filename);
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		return true;
	}

	void close()
	{
		if(log!=null)
		{
			log.close();
		}
	}

	boolean reset()
	{
		close();
		open(this.filename);
		writeAttributes();
		return true;
	}

	boolean hasAttribute(String name)
	{
		return fields.containsKey(name);
	}

	// TODO: needs some serious optimization most likely
//	Entry<String,String> FindField(String name)
//	{
//		String real_name = Normalize(name);
//		for (Entry<String,String> e : fields)
//		{
//			if (real_name == e.getKey())
//			{
//				System.out.println(e.getKey());
//				return e;
//			}
//		}
//		return null;
//	}

	boolean addAttribute(String field)
	{
		if (hasAttribute(field)) {
			// TODO: Output warning
			return false; // We already have this attribute
		}

		fields.put(field, "");

		return true;
	}

	boolean log(String field, double d)
	{
		return log(field, String.valueOf(d));
	}

	boolean log(String field, String data)
	{
		if(!hasAttribute(field)) return false;
		
		fields.put(field, data);
		return true;
	}
	
	boolean log(String field, Object data)
	{
		if(!hasAttribute(field)) return false;
		
		fields.put(field, data.toString());
		return true;
	}

	boolean writeAttributes()
	{
		for (Map.Entry<String,String> e : fields.entrySet())
		{
			log.print(e.getKey() + ',');
		}
		log.println();
		return !log.checkError();
	}

	boolean WriteLine()
	{
		for (Map.Entry<String,String> e : fields.entrySet())
		{
			log.print(e.getValue() + ',');
		}
		log.println();
		return !log.checkError();
	}

	String Normalize(String str)
	{
		return str.toLowerCase();
	}
	
	public void addLoggable(Loggable l)
	{
		loggables.add(l);
	}
	
	public void setupLoggables()
	{
		for(Loggable l : loggables)
		{
			l.setupLogging(this);
		}
	}
	
	public void log()
	{
		for(Loggable l : loggables)
		{
			l.log(this);
		}
	}
}
