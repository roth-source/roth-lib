package roth.lib.java.mapper;

import java.util.Map.Entry;

import roth.lib.java.Characters;
import roth.lib.java.deserializer.Deserializer;
import roth.lib.java.lang.Map;
import roth.lib.java.serializer.Serializer;
import roth.lib.java.time.TimeZone;
import roth.lib.java.util.IdUtil;

public class MapperConfig implements Characters
{
	protected static MapperConfig instance;
	
	protected boolean serializeNulls = false;
	protected boolean serializeEmptyArray = true;
	protected boolean serializeEmptyMap = true;
	protected boolean escapeSerializedValue = true;
	protected boolean tableHeader = true;
	protected boolean tableTrim = true;
	protected boolean deserializeColumnOrder = true;
	protected char delimiter = COMMA;
	protected char qualifier = QUOTE;
	protected Map<Class<?>, Serializer<?>> serializerMap = new Map<Class<?>, Serializer<?>>();
	protected Map<Class<?>, Deserializer<?>> deserializerMap = new Map<Class<?>, Deserializer<?>>();
	protected boolean writeXmlHeader = true;
	protected String boundary = IdUtil.random(10);
	protected TimeZone timeZone = TimeZone.DEFAULT;
	
	public MapperConfig()
	{
		
	}
	
	public static MapperConfig get()
	{
		if(instance == null)
		{
			instance = new MapperConfig();
		}
		return instance;
	}
	
	public static void set(MapperConfig mapperConfig)
	{
		instance = mapperConfig;
	}
	
	public boolean isSerializeNulls()
	{
		return serializeNulls;
	}
	
	public boolean isSerializeEmptyArray()
	{
		return serializeEmptyArray;
	}
	
	public boolean isSerializeEmptyMap()
	{
		return serializeEmptyMap;
	}
	
	public boolean isEscapeSerializedValue()
	{
		return escapeSerializedValue;
	}
	
	public boolean isTableHeader()
	{
		return tableHeader;
	}
	
	public boolean isTableTrim()
	{
		return tableTrim;
	}
	
	public boolean isDeserializeColumnOrder()
	{
		return deserializeColumnOrder;
	}
	
	public char getDelimiter()
	{
		return delimiter;
	}
	
	public char getQualifier()
	{
		return qualifier;
	}
	
	public boolean isWriteXmlHeader()
	{
		return writeXmlHeader;
	}
	
	public String getBoundary()
	{
		return boundary;
	}
	
	public Serializer<?> getSerializer(Class<?> klass)
	{
		Serializer<?> serializer = serializerMap.get(klass);
		if(serializer == null)
		{
			for(Entry<Class<?>, Serializer<?>> serializerEntry : serializerMap.entrySet())
			{
				if(serializerEntry.getKey().isAssignableFrom(klass))
				{
					serializer = serializerEntry.getValue();
					break;
				}
			}
		}
		return serializer;
	}
	
	public boolean isSerializable(Class<?> klass)
	{
		return getSerializer(klass) != null;
	}
	
	public Deserializer<?> getDeserializer(Class<?> klass)
	{
		Deserializer<?> deserializer = deserializerMap.get(klass);
		if(deserializer == null)
		{
			if(Enum.class.isAssignableFrom(klass))
			{
				deserializer = deserializerMap.get(Enum.class);
			}
		}
		return deserializer;
	}
	
	public MapperConfig setSerializeNulls(boolean serializeNulls)
	{
		this.serializeNulls = serializeNulls;
		return this;
	}
	
	public MapperConfig setSerializeEmptyArray(boolean serializeEmptyArray)
	{
		this.serializeEmptyArray = serializeEmptyArray;
		return this;
	}
	
	public MapperConfig setEscapeSerializedValue(boolean escapeSerializedValue)
	{
		this.escapeSerializedValue = escapeSerializedValue;
		return this;
	}
	
	public MapperConfig setSerializeEmptyMap(boolean serializeEmptyMap)
	{
		this.serializeEmptyMap = serializeEmptyMap;
		return this;
	}
	
	public MapperConfig setTableHeader(boolean tableHeader)
	{
		this.tableHeader = tableHeader;
		return this;
	}
	
	public MapperConfig setTableTrim(boolean tableTrim)
	{
		this.tableTrim = tableTrim;
		return this;
	}
	
	public MapperConfig setDeserializeColumnOrder(boolean deserializeColumnOrder)
	{
		this.deserializeColumnOrder = deserializeColumnOrder;
		return this;
	}
	
	public MapperConfig setDelimiter(Character delimiter)
	{
		this.delimiter = delimiter;
		return this;
	}
	
	public MapperConfig setQualifier(Character qualifier)
	{
		this.qualifier = qualifier;
		return this;
	}
	
	public MapperConfig setWriteXmlHeader(boolean writeXmlHeader)
	{
		this.writeXmlHeader = writeXmlHeader;
		return this;
	}
	
	public MapperConfig setBoundary(String boundary)
	{
		this.boundary = boundary;
		return this;
	}
	
	public <T> MapperConfig putSerializer(Class<T> klass, Serializer<T> serializer)
	{
		serializerMap.put(klass, serializer);
		return this;
	}
	
	public <T> MapperConfig putDeserializer(Class<T> klass, Deserializer<T> deserializer)
	{
		deserializerMap.put(klass, deserializer);
		return this;
	}
	
	public TimeZone getTimeZone()
	{
		return timeZone;
	}
	
	public MapperConfig setTimeZone(TimeZone timeZone)
	{
		this.timeZone = timeZone;
		return this;
	}
	
}
