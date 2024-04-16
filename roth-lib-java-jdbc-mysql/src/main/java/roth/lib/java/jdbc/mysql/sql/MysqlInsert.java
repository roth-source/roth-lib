package roth.lib.java.jdbc.mysql.sql;

import java.util.Collection;

import roth.lib.java.jdbc.sql.Insert;
import roth.lib.java.lang.List;

@SuppressWarnings("serial")
public class MysqlInsert extends Insert implements MysqlSqlFactory
{
	
	public MysqlInsert()
	{
		
	}
	
	public Insert setNames(Collection<String> names)
	{
		this.names = new List<String>().collection(names);
		return this;
	}
	
	public String getParamatersOnlySql()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(INSERT + tick(table) + " (" + tick(names) + ")");
		builder.append(LF + VALUES + "(" + param(names.size()) + ")");
		builder.append(END);
		return builder.toString();	
	}
	
}
