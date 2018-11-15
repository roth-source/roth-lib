package roth.lib.java.jdbc.mysql;

import java.sql.SQLException;
import java.util.Properties;

import roth.lib.java.jdbc.Jdbc;
import roth.lib.java.jdbc.mysql.sql.MysqlSqlFactory;
import roth.lib.java.mapper.MapperType;

public class MysqlDb extends Jdbc implements MysqlDbWrapper, MysqlSqlFactory
{
	protected static final int DEADLOCK = 1213;
	protected static final int XA_DEADLOCK = 1614;
	
	public MysqlDb()
	{
		super(MapperType.MYSQL);
	}
	
	public MysqlDb(String driver, String url)
	{
		super(MapperType.MYSQL, driver, url);
	}
	
	public MysqlDb(String driver, String url, String username, String password)
	{
		super(MapperType.MYSQL, driver, url, username, password);
	}
	
	public MysqlDb(String driver, String url, Properties properties)
	{
		super(MapperType.MYSQL, driver, url, properties);
	}
	
	public MysqlDb(String driver, String url, String username, String password, Properties properties)
	{
		super(MapperType.MYSQL, driver, url, username, password, properties);
	}
	
	@Override
	protected boolean isDeadLockException(SQLException e)
	{
		boolean deadLockExcepione = false;
		try
		{
			switch(e.getErrorCode())
			{
				case DEADLOCK:
				case XA_DEADLOCK:
				{
					deadLockExcepione = true;
					break;
				}
			}
		}
		catch(Throwable t)
		{
			
		}
		return deadLockExcepione;
	}
	
}
