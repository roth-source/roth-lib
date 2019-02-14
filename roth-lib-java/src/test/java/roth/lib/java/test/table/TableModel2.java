package roth.lib.java.test.table;

import java.io.Serializable;

import roth.lib.java.annotation.Entity;
import roth.lib.java.annotation.Property;
import roth.lib.java.json.JsonMapper;

@Entity
@SuppressWarnings("serial")
public class TableModel2 implements Serializable
{
	@Property(name = "value1", deserializer = TestDeserializer.class)
	protected Integer value1;
	
	@Property(name = "value2")
	protected String value2;

	@Property(name = "valueTrim", trimLength=10)
	protected String valueTrim;

	public TableModel2()
	{
		
	}
	
	public Integer getValue1()
	{
		return value1;
	}
	
	public String getValue2()
	{
		return value2;
	}
	
	public TableModel2 setValue1(Integer value1)
	{
		this.value1 = value1;
		return this;
	}
	
	public TableModel2 setValue2(String value2)
	{
		this.value2 = value2;
		return this;
	}

	@Override
	public String toString()
	{
		return new JsonMapper().setPrettyPrint(true).serialize(this);
	}
	
}
