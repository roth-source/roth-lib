package roth.lib.java.jdbc.mysql;

import java.sql.SQLException;
import java.util.Properties;

import roth.lib.java.jdbc.Jdbc;
import roth.lib.java.jdbc.JdbcConnection;
import roth.lib.java.jdbc.JdbcException;
import roth.lib.java.jdbc.mysql.sql.MysqlInsert;
import roth.lib.java.jdbc.mysql.sql.MysqlSqlFactory;
import roth.lib.java.lang.List;
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
	
	public boolean createTemporaryTable(JdbcConnection connection, String tableName, String fieldName) throws SQLException 
	{
		StringBuilder createTableSql = new StringBuilder();
		createTableSql.append(String.format("CREATE TEMPORARY TABLE IF NOT EXISTS `%s` (`%s` VARCHAR(128) PRIMARY KEY NOT NULL);", tableName, fieldName));
		executeUpdate(createTableSql.toString(), connection);
		return true;
	}
	
	public String createIntegrationIdTemporaryTable(JdbcConnection connection, List<String> groupIntegrationIds, String tableName, String fieldName)
	{
		try 
		{
			if (createTemporaryTable(connection, tableName, fieldName)) 
			{
				List<String> names = new List<String>();
				names.add(fieldName);
				MysqlInsert mysqlInsert = new MysqlInsert();
				mysqlInsert.setTable(tableName);
				mysqlInsert.setNames(names);
				executeBulkInsert(connection, mysqlInsert.getParamatersOnlySql(), groupIntegrationIds);

			}
		} catch(SQLException e)
		{
			throw new JdbcException(e);
		}
		return tableName;
	}
	
	
}
