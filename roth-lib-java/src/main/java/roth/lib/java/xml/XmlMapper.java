package roth.lib.java.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map.Entry;

import roth.lib.java.deserializer.Deserializer;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import roth.lib.java.mapper.Mapper;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.mapper.MapperType;
import roth.lib.java.reflector.AttributesReflector;
import roth.lib.java.reflector.EntityReflector;
import roth.lib.java.reflector.MapperReflector;
import roth.lib.java.reflector.PropertyReflector;
import roth.lib.java.serializer.Serializer;
import roth.lib.java.time.TimeZone;
import roth.lib.java.util.ReflectionUtil;
import roth.lib.java.xml.tag.CdataTag;
import roth.lib.java.xml.tag.CloseTag;
import roth.lib.java.xml.tag.CommentTag;
import roth.lib.java.xml.tag.DeclarationTag;
import roth.lib.java.xml.tag.EmptyTag;
import roth.lib.java.xml.tag.OpenTag;
import roth.lib.java.xml.tag.Tag;

public class XmlMapper extends Mapper
{
	protected static final String XML_HEADER					= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	protected static final String ESCAPED_QUOTE					= "quot";
	protected static final String ESCAPED_SINGLE_QUOTE			= "apos";
	protected static final String ESCAPED_LEFT_ANGLE_BRACKET	= "lt";
	protected static final String ESCAPED_RIGHT_ANGLE_BRACKET	= "gt";
	protected static final String ESCAPED_AMPERSAND				= "amp";
	
	public XmlMapper()
	{
		this(MapperReflector.get());
	}
	
	public XmlMapper(MapperConfig mapperConfig)
	{
		this(MapperReflector.get(), mapperConfig);
	}
	
	public XmlMapper(MapperReflector mapperReflector)
	{
		this(mapperReflector, MapperConfig.get());
	}
	
	public XmlMapper(MapperReflector mapperReflector, MapperConfig mapperConfig)
	{
		super(MapperType.XML, mapperReflector, mapperConfig);
	}
	
	@Override
	public void serialize(Object value, Writer writer)
	{
		if(value == null) throw new IllegalArgumentException("Value cannot be null");
		try
		{
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(value.getClass());
			if(entityReflector != null)
			{
				String rootName = entityReflector.getEntityName();
				if(rootName == null || rootName.isEmpty())
				{
					rootName = "root";
				}
				writeXmlHeader(writer);
				writeNewLine(writer);
				writeOpenTag(writer, rootName, getAttributeMap(value, entityReflector));
				writeEntity(writer, value, entityReflector);
				writeNewLine(writer);
				writeCloseTag(writer, rootName);
				writer.flush();
			}
			else
			{
				throw new XmlException("Value is not an entity");
			}
		}
		catch(IOException e)
		{
			throw new XmlException(e);
		}
	}
	
	@Override
	public void serialize(java.util.Map<String, ?> map, Writer writer)
	{
		serialize(map, "root", writer);
	}
	
	public void serialize(java.util.Map<String, ?> map, String rootName, Writer writer)
	{
		if(map == null) throw new IllegalArgumentException("Map cannot be null");
		try
		{
			writeXmlHeader(writer);
			writeNewLine(writer);
			writeOpenTag(writer, rootName);
			writeMap(writer, map, null);
			writeNewLine(writer);
			writeCloseTag(writer, rootName);
			writer.flush();
		}
		catch(IOException e)
		{
			throw new XmlException(e);
		}
	}
	
	protected void writeXmlHeader(Writer writer) throws IOException
	{
		if(getMapperConfig().isWriteXmlHeader())
		{
			writer.write(XML_HEADER);
		}
	}
	
	protected boolean writeEntity(Writer writer, Object value, EntityReflector entityReflector) throws IOException
	{
		boolean empty = true;
		for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(getMapperType()))
		{
			if(!hasContext() || !propertyReflector.isExcluded(getContext()))
			{
				String propertyName = propertyReflector.getPropertyName(getMapperType());
				Object propertyValue = ReflectionUtil.getFieldValue(propertyReflector.getField(), value);
				writeProperty(writer, propertyName, propertyValue, propertyReflector);
				empty = false;
			}
		}
		return empty;
	}
	
	protected void writeProperty(Writer writer, String name, Object value, PropertyReflector propertyReflector) throws IOException
	{
		if(name != null && (value != null || getMapperConfig().isSerializeNulls()))
		{
			incrementTabs();
			if(value == null)
			{
				writeNewLine(writer);
				writeOpenTag(writer, name);
				writer.write(BLANK);
				writeCloseTag(writer, name);
			}
			else if(getMapperReflector().isEntity(value.getClass()))
			{
				EntityReflector entityReflector = getMapperReflector().getEntityReflector(value.getClass());
				String propertyName = entityReflector.getPropertyName();
				if(propertyName != null)
				{
					name = propertyName;
				}
				writeNewLine(writer);
				if(value instanceof XmlValue)
				{
					XmlValue<?> xmlValue = (XmlValue<?>) value;
					if(xmlValue.getValue() != null)
					{
						writeOpenTag(writer, name, getAttributeMap(value, entityReflector));
						Serializer<?> serializer = getSerializer(xmlValue.getValue().getClass(), propertyReflector);
						if(serializer != null)
						{
							TimeZone timeZone = getTimeZone(propertyReflector);
							String timeFormat = getTimeFormat(propertyReflector);
							String serializedValue = serializer.serialize(xmlValue.getValue(), timeZone, timeFormat);
							if(serializedValue != null)
							{
								writeValue(writer, serializedValue, propertyReflector);
							}
						}
						writeCloseTag(writer, name);
					}
					else
					{
						writeEmptyTag(writer, name, getAttributeMap(value, entityReflector));
					}
				}
				else if(value instanceof XmlCdata)
				{
					writeOpenTag(writer, name, getAttributeMap(value, entityReflector));
					XmlCdata xmlCdata = (XmlCdata) value;
					if(xmlCdata != null)
					{
						writer.write("<![CDATA[ " + xmlCdata.getCdata() + " ]]>");
					}
					writeCloseTag(writer, name);
				}
				else
				{
					writeOpenTag(writer, name, getAttributeMap(value, entityReflector));
					boolean empty = writeEntity(writer, value, entityReflector);
					if(!empty)
					{
						writeNewLine(writer);
					}
					writeCloseTag(writer, name);
				}
			}
			else if(ReflectionUtil.isArray(value.getClass()) || ReflectionUtil.isCollection(value.getClass()))
			{
				List<?> values = ReflectionUtil.asCollection(value);
				String elementsName = propertyReflector != null ? propertyReflector.getElementsName() : null;
				if(elementsName != null)
				{
					writeNewLine(writer);
					writeOpenTag(writer, name);
					if(!values.isEmpty())
					{
						writeArray(writer, elementsName, values, propertyReflector);
						writeNewLine(writer);
					}
					writeCloseTag(writer, name);
				}
				else if(!values.isEmpty())
				{
					decrementTabs();
					writeArray(writer, name, values, propertyReflector);
					incrementTabs();
				}
			}
			else if(ReflectionUtil.isMap(value.getClass()))
			{
				Map<?, ?> valueMap = ReflectionUtil.asMap(value);
				writeNewLine(writer);
				writeOpenTag(writer, name);
				if(!valueMap.isEmpty())
				{
					writeMap(writer, valueMap, propertyReflector);
					writeNewLine(writer);
				}
				writeCloseTag(writer, name);
			}
			else
			{
				Serializer<?> serializer = getSerializer(value.getClass(), propertyReflector);
				if(serializer != null)
				{
					TimeZone timeZone = getTimeZone(propertyReflector);
					String timeFormat = getTimeFormat(propertyReflector);
					String serializedValue = serializer.serialize(value, timeZone, timeFormat);
					if(serializedValue != null)
					{
						writeNewLine(writer);
						writeOpenTag(writer, name);
						writeValue(writer, serializedValue, propertyReflector);
						writeCloseTag(writer, name);
					}
					else if(getMapperConfig().isSerializeNulls())
					{
						writeNewLine(writer);
						writeOpenTag(writer, name);
						writer.write(BLANK);
						writeCloseTag(writer, name);
					}
				}
			}
			decrementTabs();
		}
	}
	
	protected void writeOpenTag(Writer writer, String name) throws IOException
	{
		writeOpenTag(writer, name, null);
	}
	
	protected void writeOpenTag(Writer writer, String name, java.util.Map<String, String> attributeMap) throws IOException
	{
		writeOpenTag(writer, name, attributeMap, false);
	}
	
	protected void writeEmptyTag(Writer writer, String name, java.util.Map<String, String> attributeMap) throws IOException
	{
		writeOpenTag(writer, name, attributeMap, true);
	}
	
	protected void writeOpenTag(Writer writer, String name, java.util.Map<String, String> attributeMap, boolean empty) throws IOException
	{
		writer.write(LEFT_ANGLE_BRACKET);
		writer.write(name);
		if(attributeMap != null)
		{
			for(Entry<String, String> attributeEntry : attributeMap.entrySet())
			{
				writer.write(SPACE);
				writer.write(attributeEntry.getKey());
				writer.write(EQUAL);
				writer.write(QUOTE);
				writeValue(writer, attributeEntry.getValue(), null);
				writer.write(QUOTE);
			}
		}
		if(empty)
		{
			writer.write(SPACE);
			writer.write(SLASH);
		}
		writer.write(RIGHT_ANGLE_BRACKET);
	}
	
	protected void writeCloseTag(Writer writer, String name) throws IOException
	{
		writer.write(LEFT_ANGLE_BRACKET);
		writer.write(SLASH);
		writer.write(name);
		writer.write(RIGHT_ANGLE_BRACKET);
	}
	
	protected void writeNewLine(Writer writer) throws IOException
	{
		if(isPrettyPrint())
		{
			writer.write(NEW_LINE);
			for(int i = 0; i < tabs; i++)
			{
				writer.write(TAB);
			}
		}
	}
	
	protected void writeArray(Writer writer, String name, Collection<?> values, PropertyReflector propertyReflector) throws IOException
	{
		for(Object value : values)
		{
			writeProperty(writer, name, value, propertyReflector);
		}
	}
	
	protected void writeMap(Writer writer, java.util.Map<?, ?> valueMap, PropertyReflector propertyReflector) throws IOException
	{
		for(Entry<?, ?> valueEntry : valueMap.entrySet())
		{
			String name = null;
			Object key = valueEntry.getKey();
			if(key instanceof String)
			{
				name = (String) key;
			}
			else
			{
				Serializer<?> serializer = getSerializer(key.getClass(), propertyReflector);
				if(serializer != null)
				{
					TimeZone timeZone = getTimeZone(propertyReflector);
					String timeFormat = getTimeFormat(propertyReflector);
					name = serializer.serialize(key, timeZone, timeFormat);
				}
			}
			if(name != null)
			{
				writeProperty(writer, name, valueEntry.getValue(), propertyReflector);
			}
		}
	}
	
	protected void writeValue(Writer writer, String value, PropertyReflector propertyReflector) throws IOException
	{
		if(value != null)
		{
			if(propertyReflector != null && propertyReflector.hasTrimLength() && value.length() > propertyReflector.getProperty().trimLength())
			{
				value = value.substring(0, propertyReflector.getProperty().trimLength());
			}
			StringReader reader = new StringReader(value);
			int b;
			char c;
			while((b = reader.read()) > -1)
			{
				c = (char) b;
				switch(c)
				{
					case QUOTE:
					{
						writer.write(AMPERSAND);
						writer.write(ESCAPED_QUOTE);
						writer.write(SEMI_COLON);
						break;
					}
					case SINGLE_QUOTE:
					{
						writer.write(AMPERSAND);
						writer.write(ESCAPED_SINGLE_QUOTE);
						writer.write(SEMI_COLON);
						break;
					}
					case LEFT_ANGLE_BRACKET:
					{
						writer.write(AMPERSAND);
						writer.write(ESCAPED_LEFT_ANGLE_BRACKET);
						writer.write(SEMI_COLON);
						break;
					}
					case RIGHT_ANGLE_BRACKET:
					{
						writer.write(AMPERSAND);
						writer.write(ESCAPED_RIGHT_ANGLE_BRACKET);
						writer.write(SEMI_COLON);
						break;
					}
					case AMPERSAND:
					{
						writer.write(AMPERSAND);
						writer.write(ESCAPED_AMPERSAND);
						writer.write(SEMI_COLON);
						break;
					}
					default:
					{
						writer.write(c);
						break;
					}
				}
			}
		}
	}
	
	@Override
	public <T> T deserialize(Reader reader, Type type)
	{
		T model = null;
		try
		{
			readUntil(reader, LEFT_ANGLE_BRACKET);
			Tag tag = null;
			while((tag = readTag(reader)) != null)
			{
				if(tag instanceof OpenTag)
				{
					model = readEntity(reader, (OpenTag) tag, type);
					break;
				}
				else if(tag instanceof EmptyTag)
				{
					EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
					Class<T> klass = ReflectionUtil.getTypeClass(type);
					Constructor<T> constructor = klass.getDeclaredConstructor();
					constructor.setAccessible(true);
					model = constructor.newInstance();
					setAttributeMap(model, entityReflector, ((EmptyTag) tag).getAttributeMap());
					break;
				}
				else if(tag instanceof CloseTag)
				{
					break;
				}
				readUntil(reader, LEFT_ANGLE_BRACKET);
			}
		}
		catch(Exception e)
		{
			throw new XmlException(e);
		}
		return model;
	}
	
	@Override
	public Map<String, Object> deserialize(Reader reader)
	{
		Map<String, Object> map = new Map<String, Object>();
		try
		{
			readUntil(reader, LEFT_ANGLE_BRACKET);
			Tag tag = null;
			while((tag = readTag(reader)) != null)
			{
				if(tag instanceof OpenTag)
				{
					map = readMap(reader, String.class, null);
					break;
				}
				else if(tag instanceof CloseTag)
				{
					break;
				}
				readUntil(reader, LEFT_ANGLE_BRACKET);
			}
		}
		catch(Exception e)
		{
			throw new XmlException(e);
		}
		return map;
	}
	
	protected <T> T readEntity(Reader reader, EmptyTag emptyTag, Type type) throws Exception
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Constructor<T> constructor = klass.getDeclaredConstructor();
		constructor.setAccessible(true);
		T model = constructor.newInstance();
		setAttributeMap(model, entityReflector, emptyTag.getAttributeMap());
		return model;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> T readEntity(Reader reader, OpenTag openTag, Type type) throws Exception
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Constructor<T> constructor = klass.getDeclaredConstructor();
		constructor.setAccessible(true);
		T model = constructor.newInstance();
		setAttributeMap(model, entityReflector, openTag.getAttributeMap());
		readUntil(reader, LEFT_ANGLE_BRACKET);
		Tag tag = null;
		while((tag = readTag(reader)) != null)
		{
			if(tag instanceof OpenTag)
			{
				OpenTag propertyOpenTag = (OpenTag) tag;
				PropertyReflector propertyReflector = entityReflector.getPropertyReflector(propertyOpenTag.getName(), getMapperType(), getMapperReflector());
				if(propertyReflector != null)
				{
					Field field = propertyReflector.getField();
					Type fieldType = propertyReflector.getFieldType();
					Class<?> fieldClass = propertyReflector.getFieldClass();
					if(getMapperReflector().isEntity(fieldType))
					{
						Object value = null;
						if(XmlValue.class.isAssignableFrom(fieldClass))
						{
							value = readXmlValue(reader, propertyOpenTag, fieldType);
						}
						else if(XmlCdata.class.isAssignableFrom(fieldClass))
						{
							value = readXmlCdata(reader, propertyOpenTag, fieldType);
						}
						else
						{
							value = readEntity(reader, propertyOpenTag, fieldType);
						}
						ReflectionUtil.setFieldValue(field, model, value);
						setDeserializedName(model, propertyReflector.getFieldName());
					}
					else if(ReflectionUtil.isCollection(fieldType))
					{
						String elementsName = propertyReflector.getElementsName();
						if(elementsName != null)
						{
							Object value = readCollection(reader, fieldType, propertyReflector);
							ReflectionUtil.setFieldValue(field, model, value);
							setDeserializedName(model, propertyReflector.getFieldName());
						}
						else
						{
							Object value = readCollectionElement(reader, propertyOpenTag, fieldType, propertyReflector);
							if(value != null)
							{
								Collection collection = (Collection) field.get(model);
								if(collection == null)
								{
									Constructor<?> fieldConstructor = ReflectionUtil.getTypeClass(fieldType).getDeclaredConstructor();
									fieldConstructor.setAccessible(true);
									collection = (Collection) fieldConstructor.newInstance();
									ReflectionUtil.setFieldValue(field, model, collection);
									setDeserializedName(model, propertyReflector.getFieldName());
								}
								collection.add(value);
							}
						}
					}
					else if(ReflectionUtil.isArray(fieldType))
					{
						Object value = readArray(reader, fieldType, propertyReflector);
						ReflectionUtil.setFieldValue(field, model, value);
						setDeserializedName(model, propertyReflector.getFieldName());
					}
					else if(ReflectionUtil.isMap(fieldType))
					{
						Object value = readMap(reader, fieldType, propertyReflector);
						ReflectionUtil.setFieldValue(field, model, value);
						setDeserializedName(model, propertyReflector.getFieldName());
					}
					else
					{
						Deserializer<?> deserializer = getDeserializer(fieldClass, propertyReflector);
						if(deserializer != null)
						{
							String value = readEscaped(reader, LEFT_ANGLE_BRACKET);
							readTag(reader);
							TimeZone timeZone = getTimeZone(propertyReflector);
							String timeFormat = getTimeFormat(propertyReflector);
							value = propertyReflector.filter(value, getMapperType());
							ReflectionUtil.setFieldValue(field, model, deserializer.deserialize(value, timeZone, timeFormat, fieldClass));
							setDeserializedName(model, propertyReflector.getFieldName());
						}
						else
						{
							readUnmapped(reader, propertyOpenTag);
						}
					}
				}
				else
				{
					readUnmapped(reader, propertyOpenTag);
				}
			}
			else if(tag instanceof EmptyTag)
			{
				EmptyTag propertyEmptyTag = (EmptyTag) tag;
				PropertyReflector propertyReflector = entityReflector.getPropertyReflector(propertyEmptyTag.getName(), getMapperType(), getMapperReflector());
				if(propertyReflector != null)
				{
					Field field = propertyReflector.getField();
					Type fieldType = propertyReflector.getFieldType();
					if(getMapperReflector().isEntity(fieldType))
					{
						Object value = readEntity(reader, propertyEmptyTag, fieldType);
						ReflectionUtil.setFieldValue(field, model, value);
						setDeserializedName(model, propertyReflector.getFieldName());
					}
					else if(ReflectionUtil.isCollection(fieldType))
					{
						String elementsName = propertyReflector.getElementsName();
						if(elementsName == null)
						{
							Object value = readEntity(reader, propertyEmptyTag, ReflectionUtil.getElementType(fieldType));
							if(value != null)
							{
								Collection collection = (Collection) field.get(model);
								if(collection == null)
								{
									Constructor<?> fieldConstructor = ReflectionUtil.getTypeClass(fieldType).getDeclaredConstructor();
									fieldConstructor.setAccessible(true);
									collection = (Collection) fieldConstructor.newInstance();
									ReflectionUtil.setFieldValue(field, model, collection);
									setDeserializedName(model, propertyReflector.getFieldName());
								}
								collection.add(value);
							}
						}
					}
				}
			}
			else if(tag instanceof CloseTag)
			{
				break;
			}
			readUntil(reader, LEFT_ANGLE_BRACKET);
		}
		return model;
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, String> getAttributeMap(Object model, EntityReflector entityReflector)
	{
		Map<String, String> attributeMap = new Map<String, String>();
		if(model != null)
		{
			AttributesReflector attributesReflector = entityReflector.getAttributesReflector();
			if(attributesReflector != null)
			{
				try
				{
					Object attributesObject = attributesReflector.getField().get(model);
					if(attributesObject instanceof Map)
					{
						attributeMap.putAll((Map<String, String>) attributesObject);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			for(PropertyReflector attributeReflector : entityReflector.getAttributeReflectors(getMapperType()))
			{
				try
				{
					Object value = attributeReflector.getField().get(model);
					if(value != null)
					{
						Serializer<?> serializer = getSerializer(value.getClass(), attributeReflector);
						if(serializer != null)
						{
							TimeZone timeZone = getTimeZone(attributeReflector);
							String timeFormat = getTimeFormat(attributeReflector);
							String serializedValue = serializer.serialize(value, timeZone, timeFormat);
							if(serializedValue != null)
							{
								attributeMap.put(attributeReflector.getPropertyName(getMapperType()), serializedValue);
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return attributeMap;
	}
	
	@SuppressWarnings("unchecked")
	protected void setAttributeMap(Object model, EntityReflector entityReflector, Map<String, String> attributeMap)
	{
		if(model != null && attributeMap != null && !attributeMap.isEmpty())
		{
			attributeMap = new Map<String, String>(attributeMap);
			for(PropertyReflector attributeReflector : entityReflector.getAttributeReflectors(getMapperType()))
			{
				Field field = attributeReflector.getField();
				Class<?> fieldClass = attributeReflector.getFieldClass();
				Deserializer<?> deserializer = getDeserializer(fieldClass, attributeReflector);
				if(deserializer != null)
				{
					TimeZone timeZone = getTimeZone(attributeReflector);
					String timeFormat = getTimeFormat(attributeReflector);
					String name = attributeReflector.getPropertyName(getMapperType());
					String value = attributeReflector.filter(attributeMap.get(name), getMapperType());
					Object deserializedValue = deserializer.deserialize(value, timeZone, timeFormat, fieldClass);
					if(deserializedValue != null)
					{
						ReflectionUtil.setFieldValue(field, model, deserializedValue);
					}
					attributeMap.remove(name);
				}
			}
			AttributesReflector attributesReflector = entityReflector.getAttributesReflector();
			if(attributesReflector != null)
			{
				Field attributesField = attributesReflector.getField();
				if(attributesField != null)
				{
					try
					{
						Object attributesObject = attributesField.get(model);
						if(attributesObject instanceof Map)
						{
							((Map<String, String>) attributesObject).putAll(attributeMap);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	protected <T> T readXmlValue(Reader reader, OpenTag openTag, Type type) throws Exception
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Constructor<T> constructor = klass.getDeclaredConstructor();
		constructor.setAccessible(true);
		T model = constructor.newInstance();
		setAttributeMap(model, entityReflector, openTag.getAttributeMap());
		PropertyReflector propertyReflector = entityReflector.getFieldReflector("value", getMapperType(), getMapperReflector());
		if(propertyReflector != null)
		{
			Field field = propertyReflector.getField();
			Type xmlValueType = klass.getGenericSuperclass();
			if(xmlValueType instanceof ParameterizedType)
			{
				Type fieldType = ((ParameterizedType) xmlValueType).getActualTypeArguments()[0];
				Class<?> fieldClass = ReflectionUtil.getTypeClass(fieldType);
				Deserializer<?> deserializer = getDeserializer(fieldClass, propertyReflector);
				if(deserializer != null)
				{
					String value = readEscaped(reader, LEFT_ANGLE_BRACKET);
					readTag(reader);
					if(deserializer != null)
					{
						TimeZone timeZone = getTimeZone(propertyReflector);
						String timeFormat = getTimeFormat(propertyReflector);
						value = propertyReflector.filter(value, getMapperType());
						ReflectionUtil.setFieldValue(field, model, deserializer.deserialize(value, timeZone, timeFormat, fieldClass));
						setDeserializedName(model, propertyReflector.getFieldName());
					}
				}
				else
				{
					readUnmapped(reader, openTag);
				}
			}
			else
			{
				readUnmapped(reader, openTag);
			}
		}
		else
		{
			readUnmapped(reader, openTag);
		}
		return model;
	}
	
	protected <T> T readXmlCdata(Reader reader, OpenTag openTag, Type type) throws Exception
	{
		EntityReflector entityReflector = getMapperReflector().getEntityReflector(type);
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Constructor<T> constructor = klass.getDeclaredConstructor();
		constructor.setAccessible(true);
		T model = constructor.newInstance();
		setAttributeMap(model, entityReflector, openTag.getAttributeMap());
		PropertyReflector propertyReflector = entityReflector.getFieldReflector("cdata", getMapperType(), getMapperReflector());
		if(propertyReflector != null)
		{
			Field field = propertyReflector.getField();
			ReflectionUtil.setFieldValue(field, model, readCdata(reader));
			setDeserializedName(model, propertyReflector.getFieldName());
		}
		else
		{
			readUnmapped(reader, openTag);
		}
		return model;
	}
	
	@SuppressWarnings("unchecked")
	protected <T, K, E> T readMap(Reader reader, Type type, PropertyReflector propertyReflector) throws Exception
	{
		java.util.Map<K, E> map = null;
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Type keyType = ReflectionUtil.getKeyType(type);
		Class<K> keyClass = ReflectionUtil.getTypeClass(keyType);
		Type elementType = ReflectionUtil.getElementType(type);
		Class<E> elementClass = ReflectionUtil.getTypeClass(elementType);
		if(klass.isAssignableFrom(Map.class))
		{
			map = new Map<K, E>();
		}
		else
		{
			Constructor<T> constructor = klass.getDeclaredConstructor();
			constructor.setAccessible(true);
			map = (java.util.Map<K, E>) constructor.newInstance();
		}
		readUntil(reader, LEFT_ANGLE_BRACKET);
		Tag tag = null;
		while((tag = readTag(reader)) != null)
		{
			if(tag instanceof OpenTag)
			{
				OpenTag fieldOpenTag = (OpenTag) tag;
				K key = null;
				Deserializer<?> keyDeserializer = getDeserializer(keyClass, propertyReflector);
				if(keyDeserializer != null)
				{
					TimeZone timeZone = getTimeZone(propertyReflector);
					String timeFormat = getTimeFormat(propertyReflector);
					key = (K) keyDeserializer.deserialize(fieldOpenTag.getName(), timeZone, timeFormat);
				}
				if(getMapperReflector().isEntity(elementType))
				{
					E value = null;
					if(XmlValue.class.isAssignableFrom(elementClass))
					{
						value = readXmlValue(reader, fieldOpenTag, elementType);
					}
					else if(XmlCdata.class.isAssignableFrom(elementClass))
					{
						value = readXmlCdata(reader, fieldOpenTag, elementType);
					}
					else
					{
						value = readEntity(reader, fieldOpenTag, elementType);
					}
					if(value != null)
					{
						map.put(key, value);
					}
				}
				else if(ReflectionUtil.isCollection(elementType))
				{
					E value = readCollection(reader, elementType, propertyReflector);
					if(value != null)
					{
						map.put(key, value);
					}
				}
				else if(ReflectionUtil.isArray(elementType))
				{
					E value = readArray(reader, elementType, propertyReflector);
					if(value != null)
					{
						map.put(key, value);
					}
				}
				else if(ReflectionUtil.isMap(elementType))
				{
					E value = readMap(reader, elementType, propertyReflector);
					if(value != null)
					{
						map.put(key, value);
					}
				}
				else
				{
					Deserializer<E> deserializer = (Deserializer<E>) getDeserializer(elementClass, propertyReflector);
					if(deserializer != null)
					{
						String value = readEscaped(reader, LEFT_ANGLE_BRACKET);
						readTag(reader);
						if(deserializer != null)
						{
							TimeZone timeZone = getTimeZone(propertyReflector);
							String timeFormat = getTimeFormat(propertyReflector);
							value = propertyReflector.filter(value, getMapperType());
							E deserializedValue = deserializer.deserialize(value, timeZone, timeFormat, elementClass);
							if(deserializedValue != null)
							{
								map.put(key, deserializedValue);
							}
						}
					}
					else
					{
						readUnmapped(reader, fieldOpenTag);
					}
				}
			}
			else if(tag instanceof CloseTag)
			{
				break;
			}
			readUntil(reader, LEFT_ANGLE_BRACKET);
		}
		return (T) map;
	}
	
	@SuppressWarnings("unchecked")
	protected <T, E> T readArray(Reader reader, Type type, PropertyReflector propertyReflector) throws Exception
	{
		Type elementType = ReflectionUtil.getElementType(type);
		Class<E> elementClass = ReflectionUtil.getTypeClass(elementType);
		List<E> collection = readCollection(reader, type, propertyReflector);
		E[] array = (E[]) Array.newInstance(elementClass, collection.size());
		int i = 0;
		for(E element : collection)
		{
			array[i++] = element;
		}
		return (T) array;
	}
	
	@SuppressWarnings("unchecked")
	protected <T, E> T readCollection(Reader reader, Type type, PropertyReflector propertyReflector) throws Exception
	{
		Collection<E> collection = null;
		Class<T> klass = ReflectionUtil.getTypeClass(type);
		Type elementType = ReflectionUtil.getElementType(type);
		Class<E> elementClass = ReflectionUtil.getTypeClass(elementType);
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
		readUntil(reader, LEFT_ANGLE_BRACKET);
		Tag tag = null;
		while((tag = readTag(reader)) != null)
		{
			if(tag instanceof OpenTag)
			{
				OpenTag fieldOpenTag = (OpenTag) tag;
				if(getMapperReflector().isEntity(elementType))
				{
					E value = null;
					if(XmlValue.class.isAssignableFrom(elementClass))
					{
						value = readXmlValue(reader, fieldOpenTag, elementType);
					}
					else if(XmlCdata.class.isAssignableFrom(elementClass))
					{
						value = readXmlCdata(reader, fieldOpenTag, elementType);
					}
					else
					{
						value = readEntity(reader, fieldOpenTag, elementType);
					}
					if(value != null)
					{
						collection.add(value);
					}
				}
				else if(ReflectionUtil.isCollection(elementType))
				{
					E value = readCollection(reader, elementType, propertyReflector);
					if(value != null)
					{
						collection.add(value);
					}
				}
				else if(ReflectionUtil.isArray(elementType))
				{
					E value = readArray(reader, elementType, propertyReflector);
					if(value != null)
					{
						collection.add(value);
					}
				}
				else if(ReflectionUtil.isMap(elementType))
				{
					E value = readMap(reader, elementType, propertyReflector);
					if(value != null)
					{
						collection.add(value);
					}
				}
				else
				{
					Deserializer<E> deserializer = (Deserializer<E>) getDeserializer(elementClass, propertyReflector);
					if(deserializer != null)
					{
						String value = readEscaped(reader, LEFT_ANGLE_BRACKET);
						readTag(reader);
						TimeZone timeZone = getTimeZone(propertyReflector);
						String timeFormat = getTimeFormat(propertyReflector);
						value = propertyReflector.filter(value, getMapperType());
						E deserializedValue = deserializer.deserialize(value, timeZone, timeFormat, elementClass);
						if(deserializedValue != null)
						{
							collection.add(deserializedValue);
						}
					}
					else
					{
						readUnmapped(reader, fieldOpenTag);
					}
				}
			}
			else if(tag instanceof EmptyTag)
			{
				EmptyTag fieldEmptyTag = (EmptyTag) tag;
				if(getMapperReflector().isEntity(elementType))
				{
					E value = readEntity(reader, fieldEmptyTag, elementType);
					if(value != null)
					{
						collection.add(value);
					}
				}
			}
			else if(tag instanceof CloseTag)
			{
				break;
			}
			readUntil(reader, LEFT_ANGLE_BRACKET);
		}
		return (T) collection;
	}
	
	@SuppressWarnings("unchecked")
	protected <T, E> E readCollectionElement(Reader reader, OpenTag tag, Type type, PropertyReflector propertyReflector) throws Exception
	{
		E element = null;
		Type elementType = ReflectionUtil.getElementType(type);
		Class<E> elementClass = ReflectionUtil.getTypeClass(elementType);
		if(getMapperReflector().isEntity(elementType))
		{
			if(XmlValue.class.isAssignableFrom(elementClass))
			{
				element = readXmlValue(reader, tag, elementType);
			}
			else if(XmlCdata.class.isAssignableFrom(elementClass))
			{
				element = readXmlCdata(reader, tag, elementType);
			}
			else
			{
				element = readEntity(reader, tag, elementType);
			}
		}
		else if(ReflectionUtil.isCollection(elementType))
		{
			element = readCollection(reader, elementType, propertyReflector);
		}
		else if(ReflectionUtil.isArray(elementType))
		{
			element = readArray(reader, elementType, propertyReflector);
		}
		else if(ReflectionUtil.isMap(elementType))
		{
			element = readMap(reader, elementType, propertyReflector);
		}
		else
		{
			Deserializer<E> deserializer = (Deserializer<E>) getDeserializer(elementClass, propertyReflector);
			if(deserializer != null)
			{
				String value = readEscaped(reader, LEFT_ANGLE_BRACKET);
				readTag(reader);
				TimeZone timeZone = getTimeZone(propertyReflector);
				String timeFormat = getTimeFormat(propertyReflector);
				value = propertyReflector.filter(value, getMapperType());
				element = deserializer.deserialize(value, timeZone, timeFormat, elementClass);
			}
			else
			{
				readUnmapped(reader, tag);
			}
		}
		return element;
	}
	
	protected Tag readTag(Reader reader) throws IOException
	{
		Tag tag = null;
		StringBuilder builder = null;
		int b;
		char c;
		char p = 0;
		read: while((b = reader.read()) > -1)
		{
			c = (char) b;
			if(builder == null)
			{
				switch(c)
				{
					case QUESTION:
					{
						String value = readUntil(reader, "?>");
						tag = new DeclarationTag(value.replaceAll("\\?>$", "").trim());
						break read;
					}
					case EXCLAMATION:
					{
						if(matches(reader, "[CDATA["))
						{
							String value = readUntil(reader, "]]>");
							tag = new CdataTag(value.replaceAll("]]>$", "").trim());
						}
						else
						{
							String value = readUntil(reader, "-->");
							tag = new CommentTag(value.replaceAll("^--", "").replaceAll("-->$", "").trim());
						}
						break read;
					}
					case SLASH:
					{
						String value = readUntil(reader, RIGHT_ANGLE_BRACKET);
						tag = new CloseTag(value.trim());
						break read;
					}
				}
				builder = new StringBuilder();
			}
			switch(c)
			{
				case TAB:
				case NEW_LINE:
				case CARRIAGE_RETURN:
				case FORM_FEED:
				case BACKSPACE:
				{
					break;
				}
				case SPACE:
				{
					String name = builder.toString();
					if(tag == null && !name.isEmpty())
					{
						tag = new OpenTag(name);
						builder.setLength(0);
					}
					break;
				}
				case EQUAL:
				{
					String name = builder.toString();
					if(!name.isEmpty())
					{
						String value = null;
						c = (char) reader.read();
						switch(c)
						{
							case QUOTE:
							{
								value = readEscaped(reader, QUOTE);
								break;
							}
							case SINGLE_QUOTE:
							{
								value = readEscaped(reader, SINGLE_QUOTE);
								break;
							}
						}
						if(tag != null && value != null && tag instanceof OpenTag)
						{
							((OpenTag) tag).addAttribute(name, value);
						}
						builder.setLength(0);
					}
					break;
				}
				case RIGHT_ANGLE_BRACKET:
				{
					if(tag == null)
					{
						String name = builder.toString();
						if(!name.isEmpty())
						{
							tag = new OpenTag(name);
							builder.setLength(0);
						}
					}
					if(p == SLASH && tag instanceof OpenTag)
					{
						tag = new EmptyTag((OpenTag) tag);
					}
					break read;
				}
				default:
				{
					builder.append(c);
					break;
				}
			}
			p = c;
		}
		return tag;
	}
	
	protected String readEscaped(Reader reader, final char until) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		int b;
		char c;
		read: while((b = reader.read()) > -1)
		{
			c = (char) b;
			switch(c)
			{
				case AMPERSAND:
				{
					c = (char) reader.read();
					switch(c)
					{
						case HASH:
						{
							String code = readUntil(reader, SEMI_COLON);
							try
							{
								builder.append((char) Integer.parseInt(code, 16));
							}
							catch(Exception e)
							{
								
							}
							break;
						}
						default:
						{
							String code = c + readUntil(reader, SEMI_COLON);
							switch(code)
							{
								case ESCAPED_QUOTE:
								{
									builder.append(QUOTE);
									break;
								}
								case ESCAPED_SINGLE_QUOTE:
								{
									builder.append(SINGLE_QUOTE);
									break;
								}
								case ESCAPED_LEFT_ANGLE_BRACKET:
								{
									builder.append(LEFT_ANGLE_BRACKET);
									break;
								}
								case ESCAPED_RIGHT_ANGLE_BRACKET:
								{
									builder.append(RIGHT_ANGLE_BRACKET);
									break;
								}
								case ESCAPED_AMPERSAND:
								{
									builder.append(AMPERSAND);
									break;
								}
							}
							break;
						}
					}
					break;
				}
				default:
				{
					if(c == until)
					{
						break read;
					}
					else
					{
						builder.append(c);
						break;
					}
				}
			}
		}
		return builder.toString();
	}
	
	
	protected String readCdata(Reader reader) throws IOException
	{
		readUntil(reader, "<![CDATA[");
		String value = readUntil(reader, "]]>").replaceAll("]]>$", "").trim();
		readUntil(reader, LEFT_ANGLE_BRACKET);
		return value;
	}
	
	protected void readUnmapped(Reader reader, OpenTag openTag) throws IOException
	{
		int nested = 0;
		readUntil(reader, LEFT_ANGLE_BRACKET);
		Tag tag = null;
		while((tag = readTag(reader)) != null)
		{
			if(tag instanceof OpenTag)
			{
				nested++;
			}
			else if(tag instanceof CloseTag)
			{
				if(nested <= 0)
				{
					break;
				}
				nested--;
			}
			readUntil(reader, LEFT_ANGLE_BRACKET);
		}
	}
	
	@Override
	public String prettyPrint(Reader reader)
	{
		StringBuilder builder = new StringBuilder();
		try
		{
			int tabs = 0;
			readUntil(reader, LEFT_ANGLE_BRACKET);
			Tag tag = null;
			Tag lastTag = null;
			while((tag = readTag(reader)) != null)
			{
				if(lastTag == null)
				{
					builder.append(tag);
				}
				else if(tag instanceof OpenTag)
				{
					if(lastTag instanceof OpenTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(++tabs));
					}
					else if(lastTag instanceof EmptyTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof CloseTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof DeclarationTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof CommentTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					builder.append(tag);
				}
				else if(tag instanceof EmptyTag)
				{
					if(lastTag instanceof OpenTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(++tabs));
					}
					else if(lastTag instanceof EmptyTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof CloseTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof DeclarationTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof CommentTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					builder.append(tag);
				}
				else if(tag instanceof CloseTag)
				{
					if(lastTag instanceof OpenTag)
					{
						
					}
					else if(lastTag instanceof EmptyTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(--tabs));
					}
					else if(lastTag instanceof CloseTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(--tabs));
					}
					else if(lastTag instanceof DeclarationTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					else if(lastTag instanceof CommentTag)
					{
						builder.append(NEW_LINE);
						builder.append(tabs(tabs));
					}
					builder.append(tag);
				}
				else if(tag instanceof DeclarationTag)
				{
					builder.append(NEW_LINE);
					builder.append(tabs(tabs));
					builder.append(tag);
				}
				else if(tag instanceof CommentTag)
				{
					builder.append(NEW_LINE);
					builder.append(tabs(tabs));
					builder.append(tag);
				}
				String value = readUntil(reader, LEFT_ANGLE_BRACKET);
				if(value != null)
				{
					builder.append(value.trim());
				}
				lastTag = tag;
			}
		}
		catch(IOException e)
		{
			
		}
		builder.append(NEW_LINE);
		return builder.toString();
	}
	
}
