package roth.lib.java.jdbc;

import java.sql.SQLException;

public interface JdbcCloseHandler
{
	void close(JdbcConnection connection) throws SQLException;
	
}
