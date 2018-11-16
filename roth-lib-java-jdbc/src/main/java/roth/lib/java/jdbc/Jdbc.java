package roth.lib.java.jdbc;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Driver;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

import javax.sql.DataSource;

import roth.lib.java.Callback;
import roth.lib.java.Characters;
import roth.lib.java.Init;
import roth.lib.java.jdbc.sql.Delete;
import roth.lib.java.jdbc.sql.Insert;
import roth.lib.java.jdbc.sql.Select;
import roth.lib.java.jdbc.sql.Sql;
import roth.lib.java.jdbc.sql.SqlFactory;
import roth.lib.java.jdbc.sql.Update;
import roth.lib.java.jdbc.sql.Wheres;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import roth.lib.java.mapper.MapperType;
import roth.lib.java.reflector.EntityReflector;
import roth.lib.java.reflector.MapperReflector;
import roth.lib.java.reflector.PropertyReflector;
import roth.lib.java.time.Day;
import roth.lib.java.time.Millisecond;
import roth.lib.java.time.Month;
import roth.lib.java.time.Year;

public abstract class Jdbc implements DataSource, JdbcWrapper, Characters, SqlFactory
{
	protected MapperType mapperType;
	protected MapperReflector mapperReflector;
	protected JdbcDriver driver;
	protected String url;
	protected String username;
	protected String password;
	protected Properties properties;
	protected int maxConnections = 20;
	protected int loginTimeout = 60;
	protected int deadLockRetries = 3;
	protected PrintWriter logWriter;
	protected ConcurrentLinkedDeque<JdbcConnection> availableConnections = new ConcurrentLinkedDeque<JdbcConnection>();
	protected ConcurrentLinkedDeque<JdbcConnection> usedConnections = new ConcurrentLinkedDeque<JdbcConnection>();
	protected JdbcCloseHandler closeHandler = new JdbcCloseHandler()
	{
		@Override
		public void close(JdbcConnection connection)
		{
			usedConnections.remove(connection);
			availableConnections.add(connection);
		}
	};
	
	{
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				close();
			}
		}));
	}
	
	protected Jdbc(MapperType mapperType)
	{
		this.mapperType = mapperType;
	}
	
	public Jdbc(MapperType mapperType, String driver, String url)
	{
		this.mapperType = mapperType;
		init(driver, url);
	}
	
	public Jdbc(MapperType mapperType, String driver, String url, Properties properties)
	{
		this.mapperType = mapperType;
		init(driver, url, properties);
	}
	
	public Jdbc(MapperType mapperType, String driver, String url, String username, String password)
	{
		this.mapperType = mapperType;
		init(driver, url, username, password);
	}
	
	public Jdbc(MapperType mapperType, String driver, String url, String username, String password, Properties properties)
	{
		this.mapperType = mapperType;
		init(driver, url, username, password, properties);
	}
	
	protected void init(String driver, String url)
	{
		init(driver, url, new Properties());
	}
	
	protected void init(String driver, String url, Properties properties)
	{
		init(driver, url, null, null, properties);
	}
	
	protected void init(String driver, String url, String username, String password)
	{
		init(driver, url, username, password, new Properties());
	}
	
	protected void init(String driver, String url, String username, String password, Properties properties)
	{
		this.driver = createDriver(driver);
		this.url = url;
		this.username = username;
		this.password = password;
		this.properties = properties;
		this.mapperReflector = MapperReflector.get();
	}
	
	protected abstract boolean isDeadLockException(SQLException e);
	
	public MapperType getMapperType()
	{
		return mapperType;
	}
	
	public MapperReflector getMapperReflector()
	{
		return mapperReflector;
	}
	
	public JdbcDriver createDriver(String driverName)
	{
		try
		{
			Class<?> driverClass = Class.forName(driverName);
			Object driver = driverClass.newInstance();
			if(driver instanceof Driver)
			{
				return wrap((Driver) driver);
			}
			else
			{
				throw new IllegalArgumentException(driverName + " is not instance of Driver class");
			}
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
	
	public void setMaxConnections(int maxConnections)
	{
		this.maxConnections = maxConnections;
	}
	
	public void setDeadLockRetries(int deadLockRetries)
	{
		this.deadLockRetries = deadLockRetries;
	}
	
	@Override
	public JdbcConnection getConnection() throws SQLException
	{
		return getConnection(username, password);
	}
	
	@Override
	public JdbcConnection getConnection(String username, String password) throws SQLException
	{
		JdbcConnection connection = getAvailableConnection();
		if(connection == null)
		{
			if(usedConnections.size() < maxConnections)
			{
				connection = createConnection(username, password);
			}
			long start = System.currentTimeMillis();
			long timeout = loginTimeout * 1000;
			while(connection == null && System.currentTimeMillis() - start < timeout)
			{
				try
				{
					Thread.sleep(500);
					connection = getAvailableConnection();
				} 
				catch(InterruptedException e)
				{
					throw new SQLException(e);
				}
			}
			if(connection == null)
			{
				throw new SQLException(String.format("could not get connection within timeout of %d seconds", loginTimeout));
			}
		}
		return connection;
	}
	
	protected JdbcConnection getAvailableConnection()
	{
		JdbcConnection connection = null;
		while((connection = availableConnections.poll()) != null)
		{
			try
			{
				if(connection.isValid(loginTimeout))
				{
					usedConnections.add(connection);
					break;
				}
				else
				{
					connection.closeWrapped();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	protected JdbcConnection createConnection(String username, String password) throws SQLException
	{
		if(username != null)
		{
			properties.put("user", username);
		}
		if(password != null)
		{
			properties.put("password", password);
		}
		JdbcConnection connection = wrap(driver.connect(url, properties));
		connection.setCloseHandler(closeHandler);
		connection.setAutoCommit(false);
		usedConnections.add(connection);
		return connection;
	}
	
	@Override
	public PrintWriter getLogWriter()
	{
		return logWriter;
	}
	
	@Override
	public void setLogWriter(PrintWriter logWriter)
	{
		this.logWriter = logWriter;
	}
	
	public boolean hasLogWriter()
	{
		return logWriter != null;
	}
	
	@Override
	public void setLoginTimeout(int loginTimeout)
	{
		this.loginTimeout = loginTimeout;
	}
	
	@Override
	public int getLoginTimeout()
	{
		return loginTimeout;
	}
	
	public int getDeadLockRetries()
	{
		return deadLockRetries;
	}
	
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		throw new SQLFeatureNotSupportedException();
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		throw new SQLException();
	}
	
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return false;
	}
	
	public List<String> getGeneratedColumns(Type type)
	{
		List<String> generatedColumns = new List<String>();
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		if(entityReflector != null)
		{
			for(PropertyReflector propertyReflector : entityReflector.getGeneratedReflectors(getMapperType()))
			{
				generatedColumns.add(propertyReflector.getPropertyName(getMapperType()));
			}
		}
		return generatedColumns;
	}
	
	public void setGeneratedFields(JdbcResultSet resultSet, List<String> generatedColumns, JdbcModel model)
	{
		try
		{
			if(resultSet.next())
			{
				EntityReflector entityReflector = getMapperReflector().getEntityReflector(model.getClass());
				for(String name : generatedColumns)
				{
					PropertyReflector propertyReflector = entityReflector.getPropertyReflector(name, getMapperType(), getMapperReflector());
					if(propertyReflector != null)
					{
						try
						{
							Field field = propertyReflector.getField();
							Object value = resultSet.getValue(1, field.getType());
							field.set(model, value);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public Wheres toIdWheres(Class<?> klass, Object...values)
	{
		Wheres wheres = newWheres();
		if(values != null)
		{
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(klass);
			int i = 0;
			for(PropertyReflector idReflector : entityReflector.getIdReflectors(getMapperType()))
			{
				String propertyName = idReflector.getPropertyName(getMapperType());
				wheres.andWhere(newWhere().setOpType(OP_EQ).setName(propertyName).addValues(values[i++]));
			}
		}
		return wheres;
	}
	
	public Wheres toIdWheres(JdbcModel model)
	{
		Wheres wheres = newWheres();
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(model.getClass());
		for(PropertyReflector idReflector : entityReflector.getIdReflectors(getMapperType()))
		{
			try
			{
				String propertyName = idReflector.getPropertyName(getMapperType());
				String fieldName = idReflector.getFieldName();
				Field field = idReflector.getField();
				Object value = null;
				if(model.getDirtyIdMap().containsKey(fieldName))
				{
					value = model.getDirtyIdMap().get(fieldName);
				}
				else
				{
					value = field.get(model);
				}
				if(value != null)
				{
					wheres.andWhere(newWhere().setOpType(OP_EQ).setName(propertyName).addValues(value));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return wheres;
	}
	
	public Select toSelect(JdbcModel model)
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(model.getClass());
		Wheres wheres = toIdWheres(model);
		if(!wheres.isEmpty())
		{
			return newSelect().from(entityReflector.getEntityName()).wheres(wheres);
		}
		return null;
	}
	
	public Insert toInsert(JdbcModel model)
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(model.getClass());
		Map<String, Object> nameValues = new Map<String, Object>();
		for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(getMapperType()))
		{
			String column = propertyReflector.getPropertyName(getMapperType());
			Field field = propertyReflector.getField();
			try
			{
				Object value = field.get(model);
				nameValues.put(column, value);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(!nameValues.isEmpty())
		{
			return newInsert().setTable(entityReflector.getEntityName()).setNameValues(nameValues);
		}
		return null;
	}
	
	public Update toUpdate(JdbcModel model)
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(model.getClass());
		Map<String, Object> nameValues = new Map<String, Object>();
		if(model.isDirty())
		{
			for(String name : model.getDirtyNames())
			{
				PropertyReflector propertyReflector = entityReflector.getFieldReflector(name, getMapperType(), getMapperReflector());
				if(propertyReflector != null)
				{
					String column = propertyReflector.getPropertyName(getMapperType());
					Field field = propertyReflector.getField();
					try
					{
						Object value = field.get(model);
						nameValues.put(column, value);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		Wheres wheres = null;
		if(!nameValues.isEmpty() && !(wheres = toIdWheres(model)).isEmpty())
		{
			return newUpdate().setTable(entityReflector.getEntityName()).setNameValues(nameValues).wheres(wheres);
		}
		return null;
	}
	
	public Delete toDelete(JdbcModel model)
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(model.getClass());
		Wheres wheres = toIdWheres(model);
		if(!wheres.isEmpty())
		{
			return newDelete().setTable(entityReflector.getEntityName()).wheres(wheres);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> void fromDb(JdbcResultSet resultSet, Callback<T> callback)
	{
		try
		{
			Class<T> klass = callback.getKlass();
			ResultSetMetaData metaData = resultSet.getMetaData();
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(klass);
			if(entityReflector != null)
			{
				while(resultSet.next())
				{
					T model = fromDb(resultSet, klass, metaData, entityReflector);
					if(model != null)
					{
						callback.call(model);
					}
				}
			}
			else
			{
				while(resultSet.next())
				{
					Object value = resultSet.getValue(1, klass);
					if(value != null && value.getClass().isAssignableFrom(klass))
					{
						callback.call((T) value);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> fromDb(JdbcResultSet resultSet, Class<T> klass)
	{
		List<T> models = new List<T>();
		try
		{
			ResultSetMetaData metaData = resultSet.getMetaData();
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(klass);
			if(entityReflector != null)
			{
				while(resultSet.next())
				{
					T model = fromDb(resultSet, klass, metaData, entityReflector);
					if(model != null)
					{
						models.add(model);
					}
				}
			}
			else
			{
				while(resultSet.next())
				{
					Object value = resultSet.getValue(1, klass);
					if(value != null && value.getClass().isAssignableFrom(klass))
					{
						models.add((T) value);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return models;
	}
	
	public <T> T fromDb(JdbcResultSet resultSet, Class<T> klass, ResultSetMetaData metaData, EntityReflector entityReflector)
	{
		T model = null;
		try
		{
			Constructor<T> constructor = null;
			constructor = klass.getDeclaredConstructor();
			constructor.setAccessible(true);
			model = constructor.newInstance();
			if(model instanceof JdbcModel)
			{
				((JdbcModel) model).setDb(this).persisted();
			}
			for(int i = 1; i <= metaData.getColumnCount(); i++)
			{
				String columnLabel = metaData.getColumnLabel(i).toUpperCase();
				PropertyReflector propertyReflector = entityReflector.getPropertyReflector(columnLabel, getMapperType(), getMapperReflector());
				if(propertyReflector != null)
				{
					Field field = propertyReflector.getField();
					Object value = resultSet.getValue(columnLabel, field.getType());
					field.set(model, value);
				}
			}
			if(model instanceof Init)
			{
				((Init) model).init();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return model;
	}
	
	public List<Map<String, Object>> fromDb(JdbcResultSet resultSet)
	{
		List<Map<String, Object>> dataMaps = new List<Map<String, Object>>();
		try
		{
			while(resultSet.next())
			{
				Map<String, Object> dataMap = new Map<String, Object>();
				try
				{
					ResultSetMetaData metaData = resultSet.getMetaData();
					for(int i = 1; i <= metaData.getColumnCount(); i++)
					{
						String columnLabel = metaData.getColumnLabel(i);
						dataMap.put(columnLabel, resultSet.getObject(columnLabel));
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				if(!dataMap.isEmpty())
				{
					dataMaps.add(dataMap);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return dataMaps;
	}
	
	public JdbcPreparedStatement prepareStatement(JdbcConnection connection, Sql sql) throws SQLException
	{
		return prepareStatement(connection, sql.toString(), sql.getValues());
	}
	
	public JdbcPreparedStatement prepareStatement(JdbcConnection connection, Sql sql,  List<String> generated) throws SQLException
	{
		return prepareStatement(connection, sql.toString(), sql.getValues(), generated);
	}
	
	public JdbcPreparedStatement prepareStatement(JdbcConnection connection, String sql, Collection<Object> values) throws SQLException
	{
		return prepareStatement(connection, sql, values, null);
	}
	
	public JdbcPreparedStatement prepareStatement(JdbcConnection connection, String sql, Collection<Object> values, List<String> generatedColumns) throws SQLException
	{
		JdbcPreparedStatement preparedStatement = null;
		if(generatedColumns != null && !generatedColumns.isEmpty())
		{
			preparedStatement = connection.prepareStatement(sql, generatedColumns.toArray(new String[0]));
		}
		else
		{
			preparedStatement = connection.prepareStatement(sql);
			
		}
		preparedStatement = setValues(preparedStatement, values);
		if(hasLogWriter() && sql != null)
		{
			debugSql(sql, values, preparedStatement);
		}
		return preparedStatement;
	}
	
	protected void debugSql(String sql, Collection<Object> values)
	{
		debugSql(sql, values, null);
	}
	
	protected void debugSql(String sql, Collection<Object> values, JdbcPreparedStatement preparedStatement)
	{
		getLogWriter().println();
		try
		{
			if(values != null && !values.isEmpty())
			{
				getLogWriter().println(String.format(sql.replaceAll("\\?", "%s"), serializeValues(values)));
			}
			else
			{
				getLogWriter().println(sql);
			}
		}
		catch(Exception e)
		{
			if(preparedStatement != null)
			{
				getLogWriter().println(preparedStatement.toString());
			}
		}
	}
	
	protected Object[] serializeValues(Collection<Object> values)
	{
		List<String> serializedValues = new List<String>().setAllowNull(true);
		for(Object value : values)
		{
			if(value == null)
			{
				serializedValues.add(null);
			}
			else if(value instanceof String)
			{
				serializedValues.add(SINGLE_QUOTE + (String) value + SINGLE_QUOTE);
			}
			else if(value instanceof Enum)
			{
				serializedValues.add(SINGLE_QUOTE + value.toString() + SINGLE_QUOTE);
			}
			else if(value instanceof Number)
			{
				serializedValues.add(value.toString());
			}
			else if(value instanceof Boolean)
			{
				serializedValues.add(value.toString());
			}
			else if(value instanceof byte[])
			{
				serializedValues.add("''");
			}
			else if(value instanceof Blob)
			{
				serializedValues.add("''");
			}
			else if(value instanceof Clob)
			{
				serializedValues.add("''");
			}
			else if(value instanceof InputStream)
			{
				serializedValues.add("''");
			}
			else if(value instanceof Reader)
			{
				serializedValues.add("''");
			}
			else
			{
				if(value instanceof Year || value instanceof Month || value instanceof Day)
				{
					value = ((roth.lib.java.time.Time) value).toSqlDate();
				}
				else if(value instanceof roth.lib.java.time.Time)
				{
					value = ((roth.lib.java.time.Time) value).toSqlTimestamp();
				}
				else if(value instanceof Calendar)
				{
					value = new Timestamp(((Calendar) value).getTimeInMillis());
				}
				if(value instanceof java.sql.Date)
				{
					serializedValues.add(SINGLE_QUOTE + new SimpleDateFormat(Day.DEFAULT_PATTERN).format((java.sql.Date) value) + SINGLE_QUOTE);
				}
				else if(value instanceof java.sql.Time)
				{
					serializedValues.add(SINGLE_QUOTE + new SimpleDateFormat("HH:mm:ss.SSS").format((java.sql.Time) value) + SINGLE_QUOTE);
				}
				else if(value instanceof java.sql.Timestamp)
				{
					serializedValues.add(SINGLE_QUOTE + new SimpleDateFormat(Millisecond.DEFAULT_PATTERN).format((java.sql.Timestamp) value) + SINGLE_QUOTE);
				}
				else if(value instanceof java.util.Date)
				{
					serializedValues.add(SINGLE_QUOTE + new SimpleDateFormat(Millisecond.DEFAULT_PATTERN).format((java.util.Date) value) + SINGLE_QUOTE);
				}
				else
				{
					serializedValues.add("''");
				}
			}
		}
		return serializedValues.toArray(new String[0]);
	}
	
	public JdbcPreparedStatement setValues(JdbcPreparedStatement preparedStatement, Collection<Object> values) throws SQLException
	{
		if(values != null)
		{
			int i = 1;
			for(Object value : values)
			{
				preparedStatement.setObject(i++, value);
			}
		}
		return preparedStatement;
		
	}
	
	public JdbcPreparedStatement setValues(String sql, JdbcPreparedStatement preparedStatement, Collection<Object> values) throws SQLException
	{
		if(values != null)
		{
			int i = 1;
			for(Object value : values)
			{
				preparedStatement.setObject(i++, value);
			}
		}
		if(hasLogWriter() && sql != null)
		{
			debugSql(sql, values, preparedStatement);
		}
		return preparedStatement;
		
	}	
	
	public String table(Class<?> klass)
	{
		String table = null;
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(klass);
		if(entityReflector != null)
		{
			table = entityReflector.getEntityName();
			if(table == null)
			{
				throw new JdbcException("Entity name not found");
			}
		}
		else
		{
			throw new JdbcException("Entity annotation not found");
		}
		return table;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends JdbcModel> T query(T model)
	{
		Select select = toSelect(model);
		if(select != null)
		{
			return query(select, (Class<T>) model.getClass());
		}
		return null;
	}
	
	public <T> T query(Select select, Class<T> klass)
	{
		select.limit(1);
		return query(select.toString(), select.getValues(), klass);
	}
	
	public <T> T query(String sql, Class<T> klass)
	{
		return query(sql, (Collection<Object>) null, klass);
	}
	
	public <T> T query(String sql, Map<String, Object> valueMap, Class<T> klass)
	{
		List<T> models = queryAll(sql, valueMap, klass);
		return !models.isEmpty() ? models.get(0) : null;
	}
	
	public <T> T query(String sql, Collection<Object> values, Class<T> klass)
	{
		List<T> models = queryAll(sql, values, klass);
		return !models.isEmpty() ? models.get(0) : null;
	}
	
	public <T> T query(Select select, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		select.limit(1);
		return query(select.toString(), select.getValues(), klass, connection);
	}
	
	public <T> T query(String sql, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		return query(sql, (Collection<Object>) null, klass, connection);
	}
	
	public <T> T query(String sql, Map<String, Object> valueMap, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		List<T> models = queryAll(sql, valueMap, klass, connection);
		return !models.isEmpty() ? models.get(0) : null;
	}
	
	public <T> T query(String sql, Collection<Object> values, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		List<T> models = queryAll(sql, values, klass, connection);
		return !models.isEmpty() ? models.get(0) : null;
	}
	
	public <T> List<T> queryAll(Select select, Class<T> klass)
	{
		return queryAll(select.toString(), select.getValues(), klass);
	}
	
	public <T> List<T> queryAll(String sql, Class<T> klass)
	{
		return queryAll(sql, (Collection<Object>) null, klass);
	}
	
	public <T> List<T> queryAll(String sql, Map<String, Object> valueMap, Class<T> klass)
	{
		List<T> models = new List<T>();
		try(JdbcConnection connection = getConnection())
		{
			models = queryAll(sql, valueMap, klass, connection);
			connection.commit();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
		return models;
	}
	
	public <T> List<T> queryAll(String sql, Collection<Object> values, Class<T> klass)
	{
		List<T> models = new List<T>();
		try(JdbcConnection connection = getConnection())
		{
			models = queryAll(sql, values, klass, connection);
			connection.commit();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
		return models;
	}
	
	public <T> List<T> queryAll(Select select, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		return queryAll(select.toString(), select.getValues(), klass, connection);
	}
	
	public <T> List<T> queryAll(String sql, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		return queryAll(sql, (Collection<Object>) null, klass, connection);
	}
	
	public <T> List<T> queryAll(String sql, Map<String, Object> valueMap, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		JdbcNamedQuery namedQuery = namedQuery(sql, valueMap);
		return queryAll(namedQuery.getSql(), namedQuery.getValues(), klass, connection);
	}
	
	public <T> List<T> queryAll(String sql, Collection<Object> values, Class<T> klass, JdbcConnection connection) throws SQLException
	{
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values))
		{
			try(JdbcResultSet resultSet = preparedStatement.executeQuery())
			{
				List<T> result = fromDb(resultSet, klass);
				return result;
			}
		}
	}
	
	public <T> void queryAll(Select select, Callback<T> callback)
	{
		queryAll(select.toString(), select.getValues(), callback);
	}
	
	public <T> void queryAll(String sql, Callback<T> callback)
	{
		queryAll(sql, (Collection<Object>) null, callback);
	}
	
	public <T> void queryAll(String sql, Map<String, Object> valueMap, Callback<T> callback)
	{
		try(JdbcConnection connection = getConnection())
		{
			queryAll(sql, valueMap, callback, connection);
			connection.commit();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
	}
	
	public <T> void queryAll(String sql, Collection<Object> values, Callback<T> callback)
	{
		try(JdbcConnection connection = getConnection())
		{
			queryAll(sql, values, callback, connection);
			connection.commit();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
	}
	
	public <T> void queryAll(Select select, Callback<T> callback, JdbcConnection connection) throws SQLException
	{
		queryAll(select.toString(), select.getValues(), callback, connection);
	}
	
	public <T> void queryAll(String sql, Callback<T> callback, JdbcConnection connection) throws SQLException
	{
		queryAll(sql, (Collection<Object>) null, callback, connection);
	}
	
	public <T> void queryAll(String sql, Map<String, Object> valueMap, Callback<T> callback, JdbcConnection connection) throws SQLException
	{
		JdbcNamedQuery namedQuery = namedQuery(sql, valueMap);
		queryAll(namedQuery.getSql(), namedQuery.getValues(), callback, connection);
	}
	
	public <T> void queryAll(String sql, Collection<Object> values, Callback<T> callback, JdbcConnection connection) throws SQLException
	{
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values))
		{
			try(JdbcResultSet resultSet = preparedStatement.executeQuery())
			{
				fromDb(resultSet, callback);
			}
		}
	}
	
	public Map<String, Object> query(Select select)
	{
		select.limit(1);
		return query(select.toString(), select.getValues());
	}
	
	public Map<String, Object> query(String sql)
	{
		return query(sql, (Collection<Object>) null);
	}
	
	public Map<String, Object> query(String sql, Map<String, Object> valueMap)
	{
		List<Map<String, Object>> maps = queryAll(sql, valueMap);
		return !maps.isEmpty() ? maps.get(0) : null;
	}
	
	public Map<String, Object> query(String sql, Collection<Object> values)
	{
		List<Map<String, Object>> maps = queryAll(sql, values);
		return !maps.isEmpty() ? maps.get(0) : null;
	}
	
	public Map<String, Object> query(Select select, JdbcConnection connection) throws SQLException
	{
		select.limit(1);
		return query(select.toString(), select.getValues(), connection);
	}
	
	public Map<String, Object> query(String sql, JdbcConnection connection) throws SQLException
	{
		return query(sql, (Collection<Object>) null, connection);
	}
	
	public Map<String, Object> query(String sql, Map<String, Object> valueMap, JdbcConnection connection) throws SQLException
	{
		List<Map<String, Object>> maps = queryAll(sql, valueMap, connection);
		return !maps.isEmpty() ? maps.get(0) : null;
	}
	
	public Map<String, Object> query(String sql, Collection<Object> values, JdbcConnection connection) throws SQLException
	{
		List<Map<String, Object>> maps = queryAll(sql, values, connection);
		return !maps.isEmpty() ? maps.get(0) : null;
	}
	
	public List<Map<String, Object>> queryAll(Select select)
	{
		return queryAll(select.toString(), select.getValues());
	}
	
	public List<Map<String, Object>> queryAll(String sql, Map<String, Object> valueMap)
	{
		List<Map<String, Object>> maps = new List<Map<String, Object>>();
		try(JdbcConnection connection = getConnection())
		{
			maps = queryAll(sql, valueMap, connection);
			connection.commit();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
		return maps;
	}
	
	public List<Map<String, Object>> queryAll(String sql, Collection<Object> values)
	{
		List<Map<String, Object>> maps = new List<Map<String, Object>>();
		try(JdbcConnection connection = getConnection())
		{
			maps = queryAll(sql, values, connection);
			connection.commit();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
		return maps;
	}
	
	public List<Map<String, Object>> queryAll(Select select, JdbcConnection connection) throws SQLException
	{
		return queryAll(select.toString(), select.getValues(), connection);
	}
	
	public List<Map<String, Object>> queryAll(String sql, JdbcConnection connection) throws SQLException
	{
		return queryAll(sql, (Collection<Object>) null, connection);
	}
	
	public List<Map<String, Object>> queryAll(String sql, Map<String, Object> valueMap, JdbcConnection connection) throws SQLException
	{
		JdbcNamedQuery namedQuery = namedQuery(sql, valueMap);
		return queryAll(namedQuery.getSql(), namedQuery.getValues(), connection);
	}
	
	public List<Map<String, Object>> queryAll(String sql, Collection<Object> values, JdbcConnection connection) throws SQLException
	{
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values))
		{
			try(JdbcResultSet resultSet = preparedStatement.executeQuery())
			{
				List<Map<String, Object>> result = fromDb(resultSet);
				return result;
			}
		}
	}
	
	public int executeInsert(JdbcModel model)
	{
		Insert insert = toInsert(model);
		if(insert != null)
		{
			return executeInsert(insert, model);
		}
		return 0;
	}
	
	public int executeInsert(Insert insert)
	{
		return executeInsert(insert, (JdbcModel) null);
	}
	
	public int executeInsert(Insert insert, JdbcModel model)
	{
		return executeInsert(insert.toString(), insert.getValues(), model);
	}
	
	public int executeInsert(String sql)
	{
		return executeInsert(sql, (Collection<Object>) null);
	}
	
	public int executeInsert(String sql, Collection<Object> values)
	{
		return executeInsert(sql, values, (JdbcModel) null);
	}
	
	protected int executeInsert(String sql, Collection<Object> values, JdbcModel model)
	{
		return executeInsert(sql, values, model, 0);
	}
	
	protected int executeInsert(String sql, Collection<Object> values, JdbcModel model, int attempt)
	{
		int result = 0;
		try(JdbcConnection connection = getConnection())
		{
			try
			{
				result = executeInsert(sql, values, model, connection);
				connection.commit();
			}
			catch(SQLException e)
			{
				connection.rollback();
				connection.close();
				throw e;
			}
		}
		catch(SQLException e)
		{
			if(isDeadLockException(e) && attempt++ < getDeadLockRetries())
			{
				result = executeInsert(sql, values, model, attempt);
			}
			else
			{
				throw new JdbcException(e);
			}
		}
		return result;
	}
	
	public int executeInsert(JdbcModel model, JdbcConnection connection) throws SQLException
	{
		Insert insert = toInsert(model);
		if(insert != null)
		{
			return executeInsert(insert, model, connection);
		}
		return 0;
	}
	
	public int executeInsert(Insert insert, JdbcConnection connection) throws SQLException
	{
		return executeInsert(insert, (JdbcModel) null, connection);
	}
	
	public int executeInsert(Insert insert, JdbcModel model, JdbcConnection connection) throws SQLException
	{
		return executeInsert(insert.toString(), insert.getValues(), model, connection);
	}
	
	public int executeInsert(String sql, JdbcConnection connection) throws SQLException
	{
		return executeInsert(sql, (Collection<Object>) null, connection);
	}
	
	public int executeInsert(String sql, Collection<Object> values, JdbcConnection connection) throws SQLException
	{
		return executeInsert(sql, values, (JdbcModel) null, connection);
	}
	
	protected int executeInsert(String sql, Collection<Object> values, JdbcModel model, JdbcConnection connection) throws SQLException
	{
		int result = 0;
		List<String> generatedColumns = new List<String>();
		if(model != null)
		{
			generatedColumns = getGeneratedColumns(model.getClass());
		}
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values, generatedColumns))
		{
			result = preparedStatement.executeUpdate();
			if(model != null)
			{
				model.persisted();
				model.resetDirty();
				if(!generatedColumns.isEmpty())
				{
					try(JdbcResultSet resultSet = preparedStatement.getGeneratedKeys())
					{
						setGeneratedFields(resultSet, generatedColumns, model);
					}
				}
			}
		}
		return result;
	}
	
	public int executeBulkInsert(List<? extends JdbcModel> insertObjects, JdbcConnection connection, int batchSize)
	{
		if (insertObjects == null || insertObjects.size() == 0) {
			return 0;
		}
		if (insertObjects.size() <= batchSize) {
			return executeBulkInsert(insertObjects, connection, 0, insertObjects.size());
		}
		int numBatches = insertObjects.size() / batchSize;
		int count = 0;
		if (insertObjects.size() % batchSize > 0)
			numBatches++;
		for (int i = 0; i < numBatches; i++) 
		{
			count += executeBulkInsert(insertObjects, connection, i*batchSize, (i+1)*batchSize);
		}
		return count;
	}

	private int executeBulkInsert(List<? extends JdbcModel> insertObjects, JdbcConnection connection, int start, int end)
	{
		int results = 0;
		if (end > insertObjects.size()) {
			end = insertObjects.size();
		}
		List<String> generatedColumns = new List<String>();
		try 
		{
			if (insertObjects.size() == 0) {
				return 0;
			}
			JdbcModel model = insertObjects.get(start);
			Insert insert = toInsert(model);
			List<List<Object>> parameters = new List<List<Object>>();
			JdbcModel insertObject;
			for(int i = start; i < end; i++) 
			{
				insertObject = insertObjects.get(i);
				if (insertObject.getClass().equals(model.getClass()) == false)
				{
					throw new JdbcException("All objects must be same for bulk insert");
				}
				
				insertObject.preSave(connection);
				insertObject.preInsert(connection);
				
				
				parameters.add(toInsert(insertObject).getValues());
				
			}
			String sql = toBulkValuesString(insert.getTable(), insert.getNames(), parameters);
			if(hasLogWriter() && sql != null)
			{
				debugSql(sql, null);
			}
			
			generatedColumns = getGeneratedColumns(model.getClass());
			JdbcPreparedStatement preparedStatement = connection.prepareStatement(sql, generatedColumns.toArray(new String[0]));
			results = preparedStatement.executeUpdate();
			JdbcResultSet resultSet = preparedStatement.getGeneratedKeys();
			JdbcModel updatedModel;
			for(int j = start; j < end; j++) 
			{
				updatedModel = insertObjects.get(j);
				updatedModel.postInsert(connection, results);
				updatedModel.postSave(connection, results);
				updatedModel.persisted();
				updatedModel.resetDirty();
				if (resultSet != null && generatedColumns.size() > 0)
				{
					setGeneratedFields(resultSet, generatedColumns, updatedModel);
				}
			}
	
			preparedStatement.close();
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}

		return results;
	}
	
	public String toBulkValuesString(String table, List<String> names, List<List<Object>> bulkValues)
	{
		String baseParams = "(" + Sql.param(names.size()) + ")";
		
		StringBuilder builder = new StringBuilder();
		builder.append(INSERT + Sql.tick(table) + " (" + Sql.tick(names) + ")");
		builder.append(LF + VALUES );
		boolean firstTime = true;
		for (List<Object> sublist : bulkValues) 
		{
			if (!firstTime) {
				builder.append(", ");
			}
			builder.append(LF + String.format(baseParams.replaceAll("\\?", "%s"), serializeValues(sublist)));
			firstTime = false;
		}
		builder.append(END);
		return builder.toString();		
	}	
		
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int executeBulkInsert(JdbcConnection connection, String sql, List<? extends Object> values)
	{
		int count = 0;
		try 
		{
			int[] results;
			JdbcPreparedStatement preparedStatement = connection.prepareStatement(sql);
			for (Object obj : values)
			{
				if (obj instanceof Collection)
				{
					preparedStatement = setValues(sql, preparedStatement, (Collection)obj);
				} 
				else
				{
					List<Object> c = new List<Object>();
					c.add(obj);
					preparedStatement = setValues(sql, preparedStatement, c);
				}
			
				preparedStatement.addBatch();				
			}
			results = preparedStatement.executeBatch();
			for (int r: results)
			{
				count += r;
			}
			preparedStatement.close();
			
		}
		catch(SQLException e)
		{
			throw new JdbcException(e);
		}
		return count;
	}
	
	public int executeUpdate(JdbcModel model)
	{
		Update update = toUpdate(model);
		if(update != null)
		{
			return executeUpdate(update, model);
		}
		return 0;
	}
	
	public int executeUpdate(Update update)
	{
		return executeUpdate(update, (JdbcModel) null);
	}
	
	public int executeUpdate(Update update, JdbcModel model)
	{
		return executeUpdate(update.toString(), update.getValues(), model);
	}
	
	public int executeUpdate(String sql)
	{
		return executeUpdate(sql, (Collection<Object>) null);
	}
	
	public int executeUpdate(String sql, Collection<Object> values)
	{
		return executeUpdate(sql, values, (JdbcModel) null);
	}
	
	protected int executeUpdate(String sql, Collection<Object> values, JdbcModel model)
	{
		return executeUpdate(sql, values, model, 0);
	}
	
	protected int executeUpdate(String sql, Collection<Object> values, JdbcModel model, int attempt)
	{
		int result = 0;
		try(JdbcConnection connection = getConnection())
		{
			try
			{
				result = executeUpdate(sql, values, model, connection);
				connection.commit();
			}
			catch(SQLException e)
			{
				connection.rollback();
				connection.close();
				throw e;
			}
		}
		catch(SQLException e)
		{
			if(isDeadLockException(e) && attempt++ < getDeadLockRetries())
			{
				result = executeUpdate(sql, values, model, attempt);
			}
			else
			{
				throw new JdbcException(e);
			}
		}
		return result;
	}
	
	public int executeUpdate(JdbcModel model, JdbcConnection connection) throws SQLException
	{
		Update update = toUpdate(model);
		if(update != null)
		{
			return executeUpdate(update, model, connection);
		}
		return 0;
	}
	
	public int executeUpdate(Update update, JdbcConnection connection) throws SQLException
	{
		return executeUpdate(update, (JdbcModel) null, connection);
	}
	
	public int executeUpdate(Update update, JdbcModel model, JdbcConnection connection) throws SQLException
	{
		return executeUpdate(update.toString(), update.getValues(), model, connection);
	}
	
	public int executeUpdate(String sql, JdbcConnection connection) throws SQLException
	{
		return executeUpdate(sql, (Collection<Object>) null, connection);
	}
	
	public int executeUpdate(String sql, Collection<Object> values, JdbcConnection connection) throws SQLException
	{
		return executeUpdate(sql, values, (JdbcModel) null, connection);
	}
	
	protected int executeUpdate(String sql, Collection<Object> values, JdbcModel model, JdbcConnection connection) throws SQLException
	{
		int result = 0;
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values))
		{
			result = preparedStatement.executeUpdate();
			if(model != null)
			{
				model.persisted();
				model.resetDirty();
			}
		}
		return result;
	}
	
	public int executeDelete(JdbcModel model)
	{
		Delete delete = toDelete(model);
		if(delete != null)
		{
			return executeDelete(delete, model);
		}
		return 0;
	}
	
	public int executeDelete(Delete delete)
	{
		return executeDelete(delete, (JdbcModel) null);
	}
	
	public int executeDelete(Delete delete, JdbcModel model)
	{
		return executeDelete(delete.toString(), delete.getValues(), model);
	}
	
	public int executeDelete(String sql)
	{
		return executeDelete(sql, (Collection<Object>) null);
	}
	
	public int executeDelete(String sql, Collection<Object> values)
	{
		return executeDelete(sql, values, (JdbcModel) null);
	}
	
	protected int executeDelete(String sql, Collection<Object> values, JdbcModel model)
	{
		return executeDelete(sql, values, model, 0);
	}
	
	protected int executeDelete(String sql, Collection<Object> values, JdbcModel model, int attempt)
	{
		int result = 0;
		try(JdbcConnection connection = getConnection())
		{
			try
			{
				result = executeDelete(sql, values, model, connection);
				connection.commit();
			}
			catch(SQLException e)
			{
				connection.rollback();
				connection.close();
				throw e;
			}
		}
		catch(SQLException e)
		{
			if(isDeadLockException(e) && attempt++ < getDeadLockRetries())
			{
				result = executeDelete(sql, values, model, attempt);
			}
			else
			{
				throw new JdbcException(e);
			}
		}
		return result;
	}
	
	public int executeDelete(JdbcModel model, JdbcConnection connection) throws SQLException
	{
		Delete delete = toDelete(model);
		if(delete != null)
		{
			return executeDelete(delete, connection);
		}
		return 0;
	}
	
	public int executeDelete(Delete delete, JdbcConnection connection) throws SQLException
	{
		return executeDelete(delete, (JdbcModel) null, connection);
	}
	
	public int executeDelete(Delete delete, JdbcModel model, JdbcConnection connection) throws SQLException
	{
		return executeDelete(delete.toString(), delete.getValues(), model, connection);
	}
	
	public int executeDelete(String sql, JdbcConnection connection) throws SQLException
	{
		return executeDelete(sql, (Collection<Object>) null, connection);
	}
	
	public int executeDelete(String sql, Collection<Object> values, JdbcConnection connection) throws SQLException
	{
		return executeDelete(sql, values, (JdbcModel) null, connection);
	}
	
	protected int executeDelete(String sql, Collection<Object> values, JdbcModel model, JdbcConnection connection) throws SQLException
	{
		int result = 0;
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values))
		{
			result = preparedStatement.executeUpdate();
			if(model != null)
			{
				model.deleted();
				model.resetDirty();
			}
		}
		return result;
	}
	

	public int execute(String sql)
	{
		return execute(sql, (Collection<Object>) null);
	}
	
	public int execute(String sql, Collection<Object> values)
	{
		return execute(sql, values, 0);
	}
	
	protected int execute(String sql, Collection<Object> values, int attempt)
	{
		int result = 0;
		try(JdbcConnection connection = getConnection())
		{
			try
			{
				result = execute(sql, values, connection);
				connection.commit();
			}
			catch(SQLException e)
			{
				connection.rollback();
				connection.close();
				throw e;
			}
		}
		catch(SQLException e)
		{
			if(isDeadLockException(e) && attempt++ < getDeadLockRetries())
			{
				result = execute(sql, values, attempt);
			}
			else
			{
				throw new JdbcException(e);
			}
		}
		return result;
	}
	
	protected int execute(String sql, JdbcConnection connection) throws SQLException
	{
		return execute(sql, (Collection<Object>) null, connection);
	}
	
	protected int execute(String sql, Collection<Object> values, JdbcConnection connection) throws SQLException
	{
		int result = 0;
		try(JdbcPreparedStatement preparedStatement = prepareStatement(connection, sql, values))
		{
			result = preparedStatement.executeUpdate();
		}
		return result;
	}
	
	public void close()
	{
		for(JdbcConnection dbConnection : availableConnections)
		{
			try
			{
				dbConnection.connection.close();
			}
			catch(Exception e)
			{
				
			}
		}
		for(JdbcConnection dbConnection : usedConnections)
		{
			try
			{
				dbConnection.connection.close();
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public JdbcNamedQuery namedQuery(String sql, Map<String, Object> valueMap)
	{
		return new JdbcNamedQuery(sql, valueMap);
	}
	
}
