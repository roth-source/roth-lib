package roth.lib.java.jdbc.sql;

import java.util.Collection;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public abstract class Insert extends Values
{
	protected String table;
	protected List<String> names =  new List<String>();
	
	public Insert()
	{
		
	}

	public Insert setTable(String table)
	{
		this.table = table;
		return this;
	}
	
	public Insert setValues(Collection<Object> values)
	{
		this.values = new List<Object>().allowNull().collection(values);
		return this;
	}
	
	public Insert setNameValues(Map<String, Object> nameValues)
	{
		names = new List<String>();
		values = new List<Object>().allowNull();
		for(Entry<String, Object> nameValueEntry : nameValues.entrySet())
		{
			names.add(nameValueEntry.getKey());
			values.add(nameValueEntry.getValue());
		}
		return this;
	}
	
	public Insert setNameValues(Collection<String> names, Collection<?> values)
	{
		if(names.size() != values.size())
		{
			throw new IllegalArgumentException("names is different size than values");
		}
		this.names = new List<String>().collection(names);
		this.values = new List<Object>().allowNull().collection(values);
		return this;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(INSERT + tick(table) + " (" + tick(names) + ")");
		builder.append(LF + VALUES + "(" + param(values.size()) + ")");
		builder.append(END);
		return builder.toString();
	}
	
	public String getTable() 
	{
		return table;
	}
	
	public List<String> getNames()
	{
		return names;
	}
}
