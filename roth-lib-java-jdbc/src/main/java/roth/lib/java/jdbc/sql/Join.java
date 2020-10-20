package roth.lib.java.jdbc.sql;

import roth.lib.java.lang.List;

@SuppressWarnings("serial")
public abstract class Join extends Sql
{
	protected String sql;
	protected String joinType = INNER_JOIN;
	protected Select select;
	protected String table;
	protected String alias;
	protected String index;
	protected List<On> ons = new List<On>();
	
	protected Join()
	{
		
	}
	
	public Join setSql(String sql)
	{
		this.sql = sql;
		return this;
	}
	
	public Join setJoinType(String joinType)
	{
		this.joinType = joinType;
		return this;
	}
	
	public Join setSelect(Select select)
	{
		this.select = select;
		return this;
	}
	
	public Join setTable(String table)
	{
		this.table = table;
		return this;
	}
	
	public Join setAlias(String alias)
	{
		this.alias = alias;
		return this;
	}

	public Join setIndex(String index)
	{
		this.index = index;
		return this;
	}

	public Join setOns(List<On> ons)
	{
		this.ons = ons;
		return this;
	}
	
	public Join addOns(On...ons)
	{
		this.ons.array(ons);
		return this;
	}
	
	@Override
	public List<Object> getValues()
	{
		if(select != null)
		{
			return select.getValues();
		}
		else
		{
			List<Object> values = new List<Object>().allowNull();
			for(On on : ons)
			{
				values.addAll(on.getValues());
			}
			return values;
		}
	}
	
	@Override
	public String toString()
	{
		if(sql != null)
		{
			return sql;
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			builder.append(joinType.toString());
			if(select != null)
			{
				builder.append("(");
				builder.append(LF);
				builder.append(select.toString(false));
				builder.append(LF);
				builder.append("        ) ");
			}
			else
			{
				builder.append(tick(table));
			}
			if(alias != null)
			{
				builder.append(AS);
				builder.append(tick(alias));
			}
			if(index != null)
			{
				builder.append(index);
			}
			builder.append(ON);
			builder.append(list(ons, AND));
			return builder.toString();
		}
	}
	
}
