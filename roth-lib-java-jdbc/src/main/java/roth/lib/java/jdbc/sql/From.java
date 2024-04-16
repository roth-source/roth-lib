package roth.lib.java.jdbc.sql;

import roth.lib.java.lang.List;

@SuppressWarnings("serial")
public abstract class From extends Sql
{
	protected String name;
	protected String alias;
	protected List<Select> selects = new List<>();
	protected String unionType;
	
	protected From()
	{
		
	}
	
	public From setName(String name)
	{
		this.name = name;
		return this;
	}
	
	public From setAlias(String alias)
	{
		this.alias = alias;
		return this;
	}
	
	public From setSelect(Select select)
	{
		this.selects.add(select);
		return this;
	}
	
	public From setUnionAllSelects(Select...selects)
	{
		this.selects.array(selects);
		this.unionType = UNION_ALL;
		return this;
	}

	public From setUnionSelects(Select...selects)
	{
		this.selects.array(selects);
		this.unionType = UNION;
		return this;
	}

	public String alias()
	{
		return alias != null ? alias : name;
	}
	
	@Override
	public List<Object> getValues()
	{
		if(!selects.isEmpty())
		{
			List<Object> values = new List<>().allowNull();
			for(Select select : selects)
			{
				values.addAll(select.getValues());
			}
			return values;
		}
		else
		{
			return super.getValues();
		}
	}
	
	@Override
	public String toString()
	{
		if(!selects.isEmpty())
		{
			if(selects.size() > 1)
			{
				StringBuilder builder = new StringBuilder();
				builder.append(LF + FROM + "(");
				String seperator = null;
				for(Select select : selects)
				{
					builder.append(seperator != null ? LF + seperator : "");
					builder.append(LF + "(" + select.toString(false) + ")");
					seperator = unionType;
				}
				builder.append(")" + AS + tick(alias));
				return builder.toString();
			}
			else
			{
				return LF + FROM + "(" + LF + selects.getFirst().toString(false) + ")" + AS + tick(alias);
			}
		}
		else
		{
			return LF + FROM + tick(name) + (alias != null ? AS + tick(alias) : "");
		}
	}
	
}
