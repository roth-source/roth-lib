package roth.lib.java.table;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collection;

import roth.lib.java.deserializer.Deserializer;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import roth.lib.java.mapper.Mapper;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.mapper.MapperException;
import roth.lib.java.mapper.MapperType;
import roth.lib.java.reflector.EntityReflector;
import roth.lib.java.reflector.MapperReflector;
import roth.lib.java.reflector.PropertyReflector;
import roth.lib.java.serializer.Serializer;
import roth.lib.java.time.TimeZone;
import roth.lib.java.util.ReflectionUtil;

public class TableMapper extends Mapper
{
	protected static final String FORMULA_PATTERN = "^([\\=\\+\\-\\@])";
	protected static final String FORMULA_ESCAPE = "'$1";
	protected static final char BYTE_ORDER_MARK = (char) 0xFEFF;
	
	
	protected List<String> columns = new List<>();
	
	public TableMapper()
	{
		this(MapperReflector.get());
	}
	
	public TableMapper(MapperConfig mapperConfig)
	{
		this(MapperReflector.get(), mapperConfig);
	}
	
	public TableMapper(MapperReflector mapperReflector)
	{
		this(mapperReflector, MapperConfig.get());
	}
	
	public TableMapper(MapperReflector mapperReflector, MapperConfig mapperConfig)
	{
		super(MapperType.TABLE, mapperReflector, mapperConfig);
	}
	
	public List<String> getColumns()
	{
		return columns;
	}
	
	@Override
	public void serialize(java.util.Map<String, ?> map, Writer writer)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void serialize(Object value, Writer writer)
	{
		if(value == null) throw new IllegalArgumentException("Value cannot be null");
		try
		{
			if(ReflectionUtil.isArray(value.getClass()) || ReflectionUtil.isCollection(value.getClass()))
			{
				List<?> values = ReflectionUtil.asCollection(value);
				if(!values.isEmpty())
				{
					EntityReflector entityReflector = getMapperReflector().getEntityReflector(values.getFirst().getClass());
					writeHeader(writer, entityReflector);
					writeArray(writer, values, entityReflector);
				}
				else
				{
					throw new TableException("Value array is empty");
				}
			}
			else
			{
				EntityReflector entityReflector = getMapperReflector().getEntityReflector(value.getClass());
				if(entityReflector != null)
				{
					writeHeader(writer, entityReflector);
					writeEntity(writer, value, entityReflector);
				}
				else
				{
					throw new TableException("Value is not an entity");
				}
			}
			writer.flush();
		}
		catch(Exception e)
		{
			throw new TableException(e);
		}
	}
	
	protected void writeHeader(Writer writer, EntityReflector entityReflector) throws Exception
	{
		if(getMapperConfig().isTableHeader())
		{
			String seperator = BLANK;
			for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(getMapperType()))
			{
				writer.write(seperator);
				writer.write(propertyReflector.getPropertyName(getMapperType()));
				seperator = String.valueOf(getMapperConfig().getDelimiter());
			}
			writer.write(CARRIAGE_RETURN);
			writer.write(NEW_LINE);
		}
	}
	
	protected void writeArray(Writer writer, List<?> values, EntityReflector entityReflector) throws Exception
	{
		for(Object value : values)
		{
			writeEntity(writer, value, entityReflector);
		}
	}
	
	protected void writeEntity(Writer writer, Object value, EntityReflector entityReflector) throws Exception
	{
		String seperator = BLANK;
		for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(getMapperType()))
		{
			writer.write(seperator);
			writeField(writer, ReflectionUtil.getFieldValue(propertyReflector.getField(), value), propertyReflector);
			seperator = String.valueOf(getMapperConfig().getDelimiter());
		}
		writer.write(CARRIAGE_RETURN);
		writer.write(NEW_LINE);
	}
	
	protected void writeField(Writer writer, Object value, PropertyReflector propertyReflector) throws Exception
	{
		String serializedValue = null;
		if(value != null)
		{
			Serializer<?> serializer = propertyReflector.getSerializer(getMapperType(), getMapperReflector(), getMapperConfig());
			if(serializer != null)
			{
				TimeZone timeZone = getTimeZone(propertyReflector);
				String timeFormat = getTimeFormat(propertyReflector);
				serializedValue = serializer.serialize(value, timeZone, timeFormat);
			}
		}
		if(serializedValue != null)
		{
			if(getMapperConfig().isEscapeSerializedValue())
			{
				serializedValue = serializedValue.replaceFirst(FORMULA_PATTERN, FORMULA_ESCAPE);
			}
			if(propertyReflector != null && propertyReflector.hasTrimLength() && serializedValue.length() > propertyReflector.getProperty().trimLength())
			{
				serializedValue = serializedValue.substring(0, propertyReflector.getProperty().trimLength());
			}
			if(isEscaped(serializedValue))
			{
				writer.write(getMapperConfig().getQualifier());
				writer.write(escape(serializedValue));
				writer.write(getMapperConfig().getQualifier());
			}
			else
			{
				writer.write(serializedValue);
			}
		}
	}
	
	protected boolean isEscaped(String value)
	{
		boolean escaped = false;
		List<Character> escapeCharacters = new List<Character>(getMapperConfig().getDelimiter(), getMapperConfig().getQualifier(), CARRIAGE_RETURN, NEW_LINE, SEMI_COLON);
		for(Character escapeCharacter : escapeCharacters)
		{
			if(value.contains(String.valueOf(escapeCharacter)))
			{
				escaped = true;
				break;
			}
		}
		return escaped;
	}
	
	protected String escape(String value)
	{
		String qualifier = String.valueOf(getMapperConfig().getQualifier());
		return value.replace(qualifier, qualifier + qualifier);
	}
	
	@Override
	public Map<String, Object> deserialize(Reader reader)
	{
		throw new UnsupportedOperationException();
	}
	
	public <T> List<T> deserializeList(String data, Class<T> klass)
	{
		return deserializeList(new StringReader(data), klass);
	}
	
	public <T> List<T> deserializeList(File file, Class<T> klass)
	{
		try(FileInputStream input = new FileInputStream(file))
		{
			return deserializeList(input, klass);
		}
		catch(IOException e)
		{
			throw new MapperException(e);
		}
	}
	
	public <T> List<T> deserializeList(InputStream input, Class<T> klass)
	{
		return deserializeList(new InputStreamReader(input, UTF_8), klass);
	}
	
	public <T> List<T> deserializeList(Reader reader, Class<T> klass)
	{
		reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader); 
		List<T> list = new List<T>();
		try
		{
			if(!getMapperConfig().isDeserializeColumnOrder())
			{
				List<String> missingColumns = missingColumns(reader, klass);
				if(!missingColumns.isEmpty())
				{
					throw new TableException(String.format("Missing columns %s", missingColumns.toString()));
				}
			}
			else if(getMapperConfig().isTableHeader())
			{
				readUntil(reader, NEW_LINE, CARRIAGE_RETURN);
				peekNewLine(reader);
			}
			int row = 0;
			T entity = null;
			while((entity = readEntity(reader, klass, ++row)) != null)
			{
				list.add(entity);
			}
		}
		catch(TableException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new TableException(e);
		}
		return list;
	}

	public List<List<String>> deserializeList(String data)
	{
		return deserializeList(new StringReader(data));
	}
	
	public List<List<String>> deserializeList(File file)
	{
		try(FileInputStream input = new FileInputStream(file))
		{
			return deserializeList(input);
		}
		catch(IOException e)
		{
			throw new MapperException(e);
		}
	}
	
	public List<List<String>> deserializeList(InputStream input)
	{
		return deserializeList(new InputStreamReader(input, UTF_8));
	}
	
	public List<List<String>> deserializeList(Reader reader)
	{
		reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader); 
		List<List<String>> list = new List<>();
		try
		{
			if(getMapperConfig().isTableHeader())
			{
				readUntil(reader, NEW_LINE, CARRIAGE_RETURN);
				peekNewLine(reader);
			}
			int row = 0;
			List<String> recordList = null;
			while((recordList = readList(reader, ++row)) != null)
			{
				list.add(recordList);
			}
		}
		catch(TableException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new TableException(e);
		}
		return list;
	}
	
	@Override
	public <T> T deserialize(Reader reader, Type type)
	{
		reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader); 
		T model = null;
		try
		{
			if(!getMapperConfig().isDeserializeColumnOrder())
			{
				List<String> missingColumns = missingColumns(reader, type);
				if(!missingColumns.isEmpty())
				{
					throw new TableException(String.format("Missing columns %s", missingColumns.toString()));
				}
			}
			else if(getMapperConfig().isTableHeader())
			{
				readUntil(reader, NEW_LINE, CARRIAGE_RETURN);
				peekNewLine(reader);
			}
			if(ReflectionUtil.isArray(type) || ReflectionUtil.isCollection(type))
			{
				model = readArray(reader, type);
			}
			else
			{
				model = readEntity(reader, type, 1);
			}
		}
		catch(TableException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new TableException(e);
		}
		return model;
	}
	
	public void readColumns(Reader reader, Type type) throws Exception
	{
		char delimiter = getMapperConfig().getDelimiter();
		char qualifier = getMapperConfig().getQualifier();
		int b;
		char c;
		StringBuilder builder = new StringBuilder();
		read:
		{
			do
			{
				b = reader.read();
				c = (char) b;
				switch(c)
				{
					case BYTE_ORDER_MARK:
					{
						break;
					}
					default:
					{
						if(delimiter == c || NEW_LINE == c || CARRIAGE_RETURN == c || b == -1)
						{
							getColumns().add(builder.toString().trim());
							builder.setLength(0);
							if(NEW_LINE == c)
							{
								break read;
							}
							else if(CARRIAGE_RETURN == c)
							{
								peekNewLine(reader);
								break read;
							}
						}
						else if(qualifier == c)
						{
							builder.append(readEscaped(reader, qualifier, delimiter));
						}
						else
						{
							builder.append(c);
						}
						break;
					}
				}
			}
			while(b > -1);
		}
	}
	
	public List<String> missingColumns(byte[] bytes, Type type) throws Exception
	{
		return missingColumns(new ByteArrayInputStream(bytes), type);
	}
	
	public List<String> missingColumns(String data, Type type) throws Exception
	{
		return missingColumns(new StringReader(data), type);
	}
	
	public List<String> missingColumns(InputStream input, Type type) throws Exception
	{
		return missingColumns(new InputStreamReader(input, UTF_8), type);
	}
	
	public List<String> missingColumns(File file, Type type) throws Exception
	{
		try(FileInputStream input = new FileInputStream(file))
		{
			return missingColumns(input, type);
		}
		catch(IOException e)
		{
			throw new MapperException(e);
		}
	}
	
	public List<String> missingColumns(Reader reader, Type type) throws Exception
	{
		reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader); 
		List<String> missingColumns = new List<>();
		readColumns(reader, type);
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		List<PropertyReflector> propertyReflectors = entityReflector.getPropertyReflectors(getMapperType());
		for(PropertyReflector propertyReflector : propertyReflectors)
		{
			if(propertyReflector.isRequired())
			{
				String name = propertyReflector.getPropertyName(getMapperType());
				boolean found = false;
				for(String column : getColumns())
				{
					if(column.equalsIgnoreCase(name))
					{
						found = true;
						break;
					}
				}
				if(!found)
				{
					missingColumns.add(name);
				}
			}
		}
		return missingColumns;
	}
	
	@SuppressWarnings("unchecked")
	protected <T, E> T readArray(Reader reader, Type type) throws Exception
	{
		if(ReflectionUtil.isCollection(type))
		{
			return readCollection(reader, type);
		}
		else if(ReflectionUtil.isArray(type))
		{
			Type elementType = ReflectionUtil.getElementType(type);
			List<E> collection = readCollection(reader, type);
			E[] array = (E[]) Array.newInstance(ReflectionUtil.getTypeClass(elementType), collection.size());
			int i = 0;
			for(E element : collection)
			{
				array[i++] = element;
			}
			return (T) array;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected <T, E> T readCollection(Reader reader, Type type) throws Exception
	{
		Collection<E> collection = null;
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Type elementType = ReflectionUtil.getElementType(type);
		if(klass.isAssignableFrom(List.class) || ReflectionUtil.isArray(klass))
		{
			collection = new List<E>();
		}
		else
		{
			Constructor<T> constructor = klass.getDeclaredConstructor();
			constructor.setAccessible(true);
			collection = (Collection<E>) constructor.newInstance();
		}
		int row = 0;
		E entity = null;
		while((entity = readEntity(reader, elementType, ++row)) != null)
		{
			collection.add(entity);
		}
		return (T) collection;
	}
	
	protected <T> T readEntity(Reader reader, Type type, int row) throws Exception
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		List<PropertyReflector> propertyReflectors = entityReflector.getPropertyReflectors(getMapperType());
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Constructor<T> constructor = klass.getDeclaredConstructor();
		constructor.setAccessible(true);
		T model = null;
		char delimiter = getMapperConfig().getDelimiter();
		char qualifier = getMapperConfig().getQualifier();
		int column = 0;
		int b;
		char c;
		StringBuilder builder = new StringBuilder();
		read:
		{
			do
			{
				b = reader.read();
				c = (char) b;
				switch(c)
				{
					case BYTE_ORDER_MARK:
					{
						break;
					}
					default:
					{
						if(delimiter == c || NEW_LINE == c || CARRIAGE_RETURN == c || b == -1)
						{
							PropertyReflector propertyReflector = null;
							if(!getMapperConfig().isDeserializeColumnOrder())
							{
								if(column < getColumns().size())
								{
									propertyReflector = entityReflector.getPropertyReflector(getColumns().get(column), getMapperType(), getMapperReflector());
								}
							}
							else
							{
								if(column < propertyReflectors.size())
								{
									propertyReflector = propertyReflectors.get(column);
								}
							}
							column++;
							if(propertyReflector != null)
							{
								Deserializer<?> deserializer = propertyReflector.getDeserializer(getMapperType(), getMapperReflector(), getMapperConfig());
								if(deserializer != null)
								{
									String value = builder.toString();
									if(!value.isEmpty())
									{
										if(getMapperConfig().isTableTrim())
										{
											value = value.trim();
										}
										model = model != null ? model : constructor.newInstance();
										TimeZone timeZone = getTimeZone(propertyReflector);
										String timeFormat = getTimeFormat(propertyReflector);
										value = propertyReflector.filter(value, getMapperType());
										try
										{
											Object deserializedValue = deserializer.deserialize(value, timeZone, timeFormat, propertyReflector.getFieldClass());
											if(deserializedValue != null && deserializedValue instanceof String && propertyReflector.hasTrimLength())
											{
												String tempValue = (String) deserializedValue;
												if(tempValue.length() > propertyReflector.getProperty().trimLength())
												{
													deserializedValue = tempValue.substring(0, propertyReflector.getProperty().trimLength());
												}
											}
											ReflectionUtil.setFieldValue(propertyReflector.getField(), model, deserializedValue);
										}
										catch(Exception e)
										{
											throw new TableException(row, column, e);
										}
									}
								}
							}
							builder.setLength(0);
							if(NEW_LINE == c)
							{
								break read;
							}
							else if(CARRIAGE_RETURN == c)
							{
								peekNewLine(reader);
								break read;
							}
						}
						else if(qualifier == c)
						{
							builder.append(readEscaped(reader, qualifier, delimiter));
						}
						else
						{
							builder.append(c);
						}
						break;
					}
				}
			}
			while(b > -1);
		}
		return model;
	}
	
	protected List<String> readList(Reader reader, int row) throws Exception
	{
		List<String> list = null;
		char delimiter = getMapperConfig().getDelimiter();
		char qualifier = getMapperConfig().getQualifier();
		int column = 0;
		int b;
		char c;
		StringBuilder builder = new StringBuilder();
		read:
		{
			do
			{
				b = reader.read();
				c = (char) b;
				switch(c)
				{
					case BYTE_ORDER_MARK:
					{
						break;
					}
					default:
					{
						if(delimiter == c || NEW_LINE == c || CARRIAGE_RETURN == c || b == -1)
						{
							String value = builder.toString();
							if(!value.isEmpty())
							{
								if(getMapperConfig().isTableTrim())
								{
									value = value.trim();
								}
							}
							if(value.isEmpty())
							{
								value = null;
							}
							if(list == null)
							{
								list = new List<String>().allowNull();
							}
							list.add(value);
							column = column + 1;
							builder.setLength(0);
							if(NEW_LINE == c)
							{
								break read;
							}
							else if(CARRIAGE_RETURN == c)
							{
								peekNewLine(reader);
								break read;
							}
						}
						else if(qualifier == c)
						{
							builder.append(readEscaped(reader, qualifier, delimiter));
						}
						else
						{
							builder.append(c);
						}
						break;
					}
				}
			}
			while(b > -1);
		}
		if(isNullList(list))
		{
			if(b == -1)
			{
				list = null;
			}
			else
			{
				list = new List<>();
			}
		}
		return list;
	}
	
	protected boolean isNullList(List<String> list)
	{
		boolean nullList = false;
		if(list != null && list.size() == 1)
		{
			String value = list.get(0);
			if(value == null)
			{
				nullList = true;
			}
		}
		return nullList;
	}
	
	protected String readEscaped(Reader reader, char qualifier, char delimiter) throws Exception
	{
		StringBuilder builder = new StringBuilder();
		int b;
		char c;
		while((b = reader.read()) > -1)
		{
			c = (char) b;
			if(qualifier == c)
			{
				reader.mark(1);
				b = reader.read();
				c = (char) b;
				if(qualifier != c)
				{
					if(delimiter == c || NEW_LINE == c || CARRIAGE_RETURN == c || b == -1)
					{
						reader.reset();
						break;
					}
					else
					{
						builder.append(qualifier);
					}
				}
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
	protected void peekNewLine(Reader reader)
	{
		try
		{
			reader.mark(1);
			int b = reader.read();
			char c = (char) b;
			if(NEW_LINE != c)
			{
				reader.reset();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String prettyPrint(Reader reader)
	{
		throw new UnsupportedOperationException();
	}
	
}
