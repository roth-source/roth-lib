package roth.lib.java.jdbc.mysql.sql;

import roth.lib.java.jdbc.sql.Join;
import roth.lib.java.jdbc.sql.On;
import roth.lib.java.jdbc.sql.Select;

@SuppressWarnings("serial")
public class MysqlJoin extends Join implements MysqlSqlFactory
{
	protected MysqlIndexHint indexHint;
	
	public MysqlJoin()
	{
		
	}
	
	public MysqlJoin setIndexHint(MysqlIndexHint indexHint)
	{
		this.indexHint = indexHint;
		return this;
	}
	
	public static MysqlJoin sql(String sql)
	{
		return (MysqlJoin) new MysqlJoin().setSql(sql);
	}
	
	public static MysqlJoin joinAs(Select select, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(JOIN).setSelect(select).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin join(String table, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin joinAs(String table, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin join(String table, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin joinAs(String table, String alias, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin innerAs(Select select, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(INNER_JOIN).setSelect(select).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin inner(String table, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(INNER_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin innerAs(String table, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(INNER_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin inner(String table, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(INNER_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin innerAs(String table, String alias, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(INNER_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin leftAs(Select select, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(LEFT_JOIN).setSelect(select).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin left(String table, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(LEFT_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin leftAs(String table, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(LEFT_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin left(String table, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(LEFT_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin leftAs(String table, String alias, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(LEFT_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin rightAs(Select select, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(RIGHT_JOIN).setSelect(select).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin right(String table, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(RIGHT_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin rightAs(String table, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(RIGHT_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin right(String table, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(RIGHT_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin rightAs(String table, String alias, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(RIGHT_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin outerAs(Select select, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(OUTER_JOIN).setSelect(select).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin outer(String table, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(OUTER_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin outerAs(String table, String alias, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setJoinType(OUTER_JOIN).setTable(table).setAlias(alias).addOns(ons);
	}
	
	public static MysqlJoin outer(String table, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(OUTER_JOIN).setTable(table).addOns(ons);
	}
	
	public static MysqlJoin outerAs(String table, String alias, MysqlIndexHint indexHint, On...ons)
	{
		return (MysqlJoin) new MysqlJoin().setIndexHint(indexHint).setJoinType(OUTER_JOIN).setTable(table).setAlias(alias).addOns(ons);
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
			builder.append(joinType);
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
			if(indexHint != null)
			{
				builder.append(indexHint);
			}
			builder.append(ON);
			builder.append(list(ons, AND));
			return builder.toString();
		}
	}
	
}
