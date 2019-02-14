package roth.lib.java.form;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import roth.lib.java.Generic;
import roth.lib.java.deserializer.Deserializer;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import roth.lib.java.mapper.Mapper;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.mapper.MapperType;
import roth.lib.java.reflector.EntityReflector;
import roth.lib.java.reflector.MapperReflector;
import roth.lib.java.reflector.PropertyReflector;
import roth.lib.java.serializer.Serializer;
import roth.lib.java.util.IoUtil;
import roth.lib.java.util.ReflectionUtil;
import roth.lib.java.util.UrlUtil;

public class FormMapper extends Mapper
{
	
	public FormMapper()
	{
		this(MapperReflector.get());
	}
	
	public FormMapper(MapperConfig mapperConfig)
	{
		this(MapperReflector.get(), mapperConfig);
	}
	
	public FormMapper(MapperReflector mapperReflector)
	{
		this(mapperReflector, MapperConfig.get());
	}
	
	public FormMapper(MapperReflector mapperReflector, MapperConfig mapperConfig)
	{
		super(MapperType.FORM, mapperReflector, mapperConfig);
	}
	
	public Map<String, String> getParamMap(Object value)
	{
		Map<String, String> paramMap = new Map<>();
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(value.getClass());
		for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(getMapperType()))
		{
			if(!hasContext() || !propertyReflector.isExcluded(getContext()))
			{
				Class<?> propertyClass = propertyReflector.getFieldClass();
				String propertyName = propertyReflector.getPropertyName(getMapperType());
				if(propertyName != null)
				{
					Serializer<?> serializer = getSerializer(propertyClass, propertyReflector);
					if(serializer != null)
					{
						String serializedValue = null;
						Object fieldValue = ReflectionUtil.getFieldValue(propertyReflector.getField(), value);
						if(fieldValue != null)
						{
							String timeFormat = getTimeFormat(propertyReflector);
							serializedValue = serializer.serialize(fieldValue, getTimeZone(propertyReflector), timeFormat);
						}
						else if(getMapperConfig().isSerializeNulls())
						{
							serializedValue = BLANK;
						}
						if(serializedValue != null)
						{
							paramMap.put(propertyName, serializedValue);
						}
					}
				}
			}
		}
		return paramMap;
	}
	
	@Override
	public void serialize(Object value, Writer writer)
	{
		if(value == null) throw new IllegalArgumentException("Value cannot be null");
		try
		{
			String seperator = BLANK;
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(value.getClass());
			for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(getMapperType()))
			{
				if(!hasContext() || !propertyReflector.isExcluded(getContext()))
				{
					String propertyName = propertyReflector.getPropertyName(getMapperType());
					if(propertyName != null)
					{
						Object fieldValue = ReflectionUtil.getFieldValue(propertyReflector.getField(), value);
						if(fieldValue != null && (ReflectionUtil.isArray(fieldValue.getClass()) || ReflectionUtil.isCollection(fieldValue.getClass())))
						{
							List<?> elementValues = ReflectionUtil.asCollection(fieldValue);
							for(Object elementValue : elementValues)
							{
								seperator = writeProperty(writer, propertyName, elementValue, seperator, propertyReflector);
							}
						}
						else
						{
							seperator = writeProperty(writer, propertyName, fieldValue, seperator, propertyReflector);
						}
					}
				}
			}
			writer.flush();
		}
		catch(IOException e)
		{
			throw new FormException(e);
		}
	}
	
	@Override
	public void serialize(java.util.Map<String, ?> map, Writer writer)
	{
		if(map == null) throw new IllegalArgumentException("Map cannot be null");
		try
		{
			String seperator = BLANK;
			for(Entry<String, ?> entry : map.entrySet())
			{
				String propertyName = entry.getKey();
				if(propertyName != null)
				{
					Object fieldValue = entry.getValue();
					if(fieldValue != null && (ReflectionUtil.isArray(fieldValue.getClass()) || ReflectionUtil.isCollection(fieldValue.getClass())))
					{
						List<?> elementValues = ReflectionUtil.asCollection(fieldValue);
						for(Object elementValue : elementValues)
						{
							seperator = writeProperty(writer, propertyName, elementValue, seperator, null);
						}
					}
					else
					{
						seperator = writeProperty(writer, propertyName, fieldValue, seperator, null);
					}
				}
			}
			writer.flush();
		}
		catch(IOException e)
		{
			throw new FormException(e);
		}
	}
	
	protected String writeProperty(Writer writer, String name, Object value, String seperator, PropertyReflector propertyReflector) throws IOException
	{
		String serializedValue = null;
		if(value != null)
		{
			Serializer<?> serializer = getSerializer(value.getClass(), propertyReflector);
			if(serializer != null)
			{
				if(value != null)
				{
					String timeFormat = getTimeFormat(propertyReflector);
					serializedValue = serializer.serialize(value, getTimeZone(propertyReflector), timeFormat);
				}
			}
		}
		else if(getMapperConfig().isSerializeNulls())
		{
			serializedValue = BLANK;
		}
		if(serializedValue != null)
		{
			seperator = writeField(writer, name, serializedValue, seperator, propertyReflector);
		}
		return seperator;
	}
	
	protected String writeField(Writer writer, String name, String value, String seperator, PropertyReflector propertyReflector) throws IOException
	{
		if(propertyReflector != null && propertyReflector.hasTrimLength() && value.length() > propertyReflector.getProperty().trimLength())
		{
			value = value.substring(0, propertyReflector.getProperty().trimLength());
		}

		writer.write(seperator);
		writer.write(name);
		writer.write(EQUAL);
		writer.write(UrlUtil.encode(value));
		if(BLANK.equals(seperator))
		{
			seperator = String.valueOf(AMPERSAND);
			if(isPrettyPrint())
			{
				seperator += String.valueOf(NEW_LINE);
			}
		}
		return seperator;
	}
	
	@Override
	public <T> T deserialize(Reader reader, Type type)
	{
		T entity = null;
		try
		{
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
			Class<T> klass = ReflectionUtil.getTypeClass(type);
			Constructor<T> constructor = klass.getDeclaredConstructor();
			constructor.setAccessible(true);
			entity = constructor.newInstance();
			int b;
			char c;
			String name = BLANK;
			StringBuilder builder = new StringBuilder();
			while((b = reader.read()) > 0)
			{
				c = (char) b;
				switch(c)
				{
					case NEW_LINE:
					{
						break;
					}
					case EQUAL:
					{
						name = builder.toString();
						builder.setLength(0);
						break;
					}
					case AMPERSAND:
					{
						setValue(entity, entityReflector.getPropertyReflector(name, getMapperType(), getMapperReflector()), UrlUtil.decode(builder.toString()));
						name = BLANK;
						builder.setLength(0);
						break;
					}
					default:
					{
						builder.append(c);
						break;
					}
				}
			}
			setValue(entity, entityReflector.getPropertyReflector(name, getMapperType(), getMapperReflector()), UrlUtil.decode(builder.toString()));
		}
		catch(Exception e)
		{
			throw new FormException(e);
		}
		return entity;
	}
	
	protected void setValue(Object model, PropertyReflector propertyReflector, String value) throws Exception
	{
		if(propertyReflector != null)
		{
			Deserializer<?> deserializer = getDeserializer(propertyReflector.getFieldClass(), propertyReflector);
			if(deserializer != null)
			{
				String timeFormat = getTimeFormat(propertyReflector);
				value = propertyReflector.filter(value, getMapperType());
				ReflectionUtil.setFieldValue(propertyReflector.getField(), model, deserializer.deserialize(value, getTimeZone(propertyReflector), timeFormat, propertyReflector.getFieldClass()));
			}
		}
	}
	
	@Override
	public Map<String, Object> deserialize(Reader reader)
	{
		Map<String, Object> map = new Map<String, Object>();
		try
		{
			int b;
			char c;
			String name = null;
			StringBuilder builder = new StringBuilder();
			while((b = reader.read()) > 0)
			{
				c = (char) b;
				switch(c)
				{
					case NEW_LINE:
					{
						break;
					}
					case EQUAL:
					{
						name = builder.toString();
						builder.setLength(0);
						break;
					}
					case AMPERSAND:
					{
						map.put(name, UrlUtil.decode(builder.toString()));
						name = null;
						builder.setLength(0);
						break;
					}
					default:
					{
						builder.append(c);
						break;
					}
				}
			}
			map.put(name, UrlUtil.decode(builder.toString()));
		}
		catch(Exception e)
		{
			throw new FormException(e);
		}
		return map;
	}
	
	public <T> T deserialize(java.util.Map<String, String> map, Class<?> klass)
	{
		return deserialize(map, (Type) klass);
	}
	
	public <T> T deserialize(java.util.Map<String, String> map, Generic<T> generic)
	{
		return deserialize(map, generic.getType());
	}
	
	public <T> T deserialize(java.util.Map<String, String> map, Type type)
	{
		T entity = null;
		try
		{
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
			Class<T> klass = ReflectionUtil.getTypeClass(type);
			Constructor<T> constructor = klass.getDeclaredConstructor();
			constructor.setAccessible(true);
			entity = constructor.newInstance();
			for(Entry<String, String> entry : map.entrySet())
			{
				String name = entry.getKey();
				String value = entry.getValue();
				setValue(entity, entityReflector.getPropertyReflector(name, getMapperType(), getMapperReflector()), value);
			}
		}
		catch(Exception e)
		{
			throw new FormException(e);
		}
		return entity;
	}
	
	
	@Override
	public String prettyPrint(Reader reader)
	{
		try
		{
			String form = IoUtil.toString(reader);
			return form.replaceAll("&(?!\\r|\\n)", "&\n");
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
}
