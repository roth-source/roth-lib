package roth.lib.java.test.xml;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import roth.lib.java.annotation.Entity;
import roth.lib.java.annotation.Property;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.time.Day;
import roth.lib.java.xml.XmlMapper;
import roth.lib.java.xml.XmlModel;

@Entity(name = "test")
@SuppressWarnings("serial")
public class XmlTestModel extends XmlModel
{
	@Property(name = "test-value")
	protected XmlTestValue testValue;
	
	@Property(name = "attribute", attribute = true)
	protected String attribute = "test";
	
	@Property(name = "day", attribute = true)
	protected Day day;
	
	@Property(name = "null-primitive-boolean")
	protected boolean nullPrimitiveBoolean;
	
	@Property(name = "null-primitive-byte")
	protected byte nullPrimitiveByte;
	
	@Property(name = "null-primitive-short")
	protected short nullPrimitiveShort;
	
	@Property(name = "null-primitive-integer")
	protected int nullPrimitiveInteger;
	
	@Property(name = "null-primitive-long")
	protected long nullPrimitiveLong;
	
	@Property(name = "null-primitive-float")
	protected float nullPrimitiveFloat;
	
	@Property(name = "null-primitive-double")
	protected double nullPrimitiveDouble;
	
	@Property(name = "null-primitive-character")
	protected char nullPrimitiveCharacter;
	
	@Property(name = "null-wrapper-boolean")
	protected Boolean nullWrapperBoolean = null;
	
	@Property(name = "null-wrapper-byte")
	protected Byte nullWrapperByte;
	
	@Property(name = "null-wrapper-short")
	protected Short nullWrapperShort;
	
	@Property(name = "null-wrapper-integer")
	protected Integer nullWrapperInteger;
	
	@Property(name = "null-wrapper-long")
	protected Long nullWrapperLong;
	
	@Property(name = "null-wrapper-float")
	protected Float nullWrapperFloat;
	
	@Property(name = "null-wrapper-double")
	protected Double nullWrapperDouble;
	
	@Property(name = "null-wrapper-character")
	protected Character nullWrapperCharacter;
	
	@Property(name = "null-string")
	protected String nullString;
	
	@Property(name = "null-date")
	protected Date nullDate;
	
	@Property(name = "null-calendar")
	protected Calendar nullCalendar;
	
	@Property(name = "test-boolean")
	//protected Boolean testBoolean = true;
	protected Boolean testBoolean;
	
	@Property(name = "test-byte")
	//protected Byte testByte = 123;
	protected Byte testByte;
	
	@Property(name = "test-short")
	//protected Short testShort = 12345;
	protected Short testShort;
	
	@Property(name = "test-integer")
	//protected Integer testInteger = 12345678;
	protected Integer testInteger;
	
	@Property(name = "test-long")
	//protected Long testLong = 123456789L;
	protected Long testLong;
	
	@Property(name = "test-float")
	//protected Float testFloat = 1.23456789f;
	protected Float testFloat;
	
	@Property(name = "test-double")
	//protected Double testDouble = 1.23456789d;
	protected Double testDouble;
	
	@Property(name = "test-character")
	//protected Character testCharacter = 'a';
	protected Character testCharacter;
	
	@Property(name = "test-string")
	//protected String testString = "<test>He said, \"He's nice & smart\"</test>";
	protected String testString;
	
	@Property(name = "test-string2", trimLength=25)
	//protected String testString = "<test>He said, \"He's nice & smart\"</test>";
	protected String testString2;
	
	@Property(name = "test-string-trim", trimLength=10)
	protected String testStringTrim;

	@Property(name = "test-string-trim2", trimLength=100)
	protected String testStringTrim2;
	
	@Property(name = "test-string-notrim")
	protected String testStringNoTrim;
	
	@Property(name = "test-string-max", maxLength=10)
	protected String testStringmax;
	
	
	@Property(name = "test-date")
	//protected Date testDate = new Date();
	protected Date testDate;
	
	@Property(name = "test-calendar", exclude = "test")
	protected Calendar testCalendar = new GregorianCalendar();
	//protected Calendar testCalendar;
	
	@Property(name = "test-model")
	//protected XmlSubModel testModel = new XmlSubModel();
	protected XmlSubModel testModel;
	
	@Property(name = "null-string-array")
	protected String[] nullStringArray;
	
	@Property(name = "empty-string-array")
	protected String[] emptyStringArray = new String[]{};
	
	@Property(name = "test-string-array", elementsName = "element")
	//protected String[] testStringArray = new String[]{"one", "two", "three"};
	protected String[] testStringArray;
	
	@Property(name = "null-string-list", elementsName = "nullElement")
	protected List<String> nullStringList;
	
	@Property(name = "empty-string-list")
	protected List<String> emptyStringList = new List<String>();
	
	@Property(name = "test-string-list", elementsName = "element")
	//protected List<String> testStringList = new List<String>(Arrays.asList(new String[]{"one", "two", "three"}));
	protected List<String> testStringList;
	
	@Property(name = "null-string-map")
	protected Map<String, String> nullStringMap;
	
	@Property(name = "empty-string-map")
	protected Map<String, String> emptyStringMap = new Map<String, String>();
	
	@Property(name = "test-string-map")
	protected Map<String, String> testStringMap = new Map<String, String>();
	
	@Property(name = "test-model-list", elementsName = "element")
	//protected List<XmlSubModel> testModelList = new List<XmlSubModel>(Arrays.asList(new XmlSubModel[]{new XmlSubModel(), new XmlSubModel()}));
	protected List<XmlSubModel> testModelList;
	
	@Property(name = "test-model-map")
	protected Map<String, XmlSubModel> testModelMap = new Map<String, XmlSubModel>();
	
	public XmlTestModel()
	{
		//testStringMap.put("one", "value one");
		//testStringMap.put("two", "value two");
		//testStringMap.put("three", "value three");
		//testModelMap.put("one", new XmlSubModel());
		//testModelMap.put("two", new XmlSubModel());
		attributeMap.put("mandatory", "true");
		attributeMap.put("validate", "false");
	}
	
	public void init()
	{
		day = new Day();
		testValue = new XmlTestValue().setType("ERROR");
		//testValue.setValue("TEST");
		testBoolean = true;
		testByte = 123;
		testShort = 12345;
		testInteger = 12345678;
		testLong = 123456789L;
		testFloat = 1.23456789f;
		testDouble = 1.23456789d;
		testCharacter = 'a';
		testString = "<test>He said, \"He's nice & smart\"</test>";
		testString2 = "<test>He said, \"nice\"</test>";

		testDate = new Date();
		testCalendar = new GregorianCalendar();
		testModel = new XmlSubModel();
		testStringArray = new String[]{"one", "two", "three"};
		testStringList = List.fromArray("one", "two", "three");
		testModelList = List.fromArray(new XmlSubModel(), new XmlSubModel());
		testStringMap.put("one", "value one");
		testStringMap.put("two", "value two");
		testStringMap.put("three", "value three");
		testModelMap.put("one", new XmlSubModel());
		testModelMap.put("two", new XmlSubModel());
		testStringTrim = "12345678901234567890";
		testStringTrim2 = "12345678901234567890";
		testStringNoTrim = "12345678901234567890";
		testStringmax = "12345678901234567890";
	}
	
	@Override
	public String toString()
	{
		MapperConfig mapperConfig = new MapperConfig().setSerializeNulls(true);
		return new XmlMapper(mapperConfig).setPrettyPrint(true).serialize(this);
	}
	
}