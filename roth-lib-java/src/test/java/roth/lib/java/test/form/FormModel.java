package roth.lib.java.test.form;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import roth.lib.java.annotation.Entity;
import roth.lib.java.annotation.Property;
import roth.lib.java.form.FormMapper;
import roth.lib.java.lang.List;
import roth.lib.java.mapper.MapperConfig;

@Entity(name = "model")
public class FormModel
{
	
	@Property(name = "null_primitive_boolean")
	private boolean nullPrimitiveBoolean;
	
	@Property(name = "null_primitive_byte")
	private byte nullPrimitiveByte;
	
	@Property(name = "null_primitive_short")
	private short nullPrimitiveShort;
	
	@Property(name = "null_primitive_integer")
	private int nullPrimitiveInteger;
	
	@Property(name = "null_primitive_long")
	private long nullPrimitiveLong;
	
	@Property(name = "null_primitive_float")
	private float nullPrimitiveFloat;
	
	@Property(name = "null_primitive_double")
	private double nullPrimitiveDouble;
	
	@Property(name = "null_primitive_character")
	private char nullPrimitiveCharacter;
	
	@Property(name = "null_wrapper_boolean")
	private Boolean nullWrapperBoolean = null;
	
	@Property(name = "null_wrapper_byte")
	private Byte nullWrapperByte;
	
	@Property(name = "null_wrapper_short")
	private Short nullWrapperShort;
	
	@Property(name = "null_wrapper_integer")
	private Integer nullWrapperInteger;
	
	@Property(name = "null_wrapper_long")
	private Long nullWrapperLong;
	
	@Property(name = "null_wrapper_float")
	private Float nullWrapperFloat;
	
	@Property(name = "null_wrapper_double")
	private Double nullWrapperDouble;
	
	@Property(name = "null_wrapper_character")
	private Character nullWrapperCharacter;
	
	@Property(name = "null_string")
	private String nullString;
	
	@Property(name = "null_date")
	private Date nullDate;
	
	@Property(name = "null_calendar")
	private Calendar nullCalendar;
	
	@Property(name = "test_boolean")
	private Boolean testBoolean = true;
	
	@Property(name = "test_byte")
	private Byte testByte = 123;
	
	@Property(name = "test_short")
	private Short testShort = 12345;
	
	@Property(name = "test_integer")
	private Integer testInteger = 12345678;
	
	@Property(name = "test_long")
	private Long testLong = 123456789L;
	
	@Property(name = "test_float")
	private Float testFloat = 1.23456789f;
	
	@Property(name = "test_double")
	private Double testDouble = 1.23456789d;
	
	@Property(name = "test_character")
	private Character testCharacter = 'a';
	
	@Property(name = "test_string")
	private String testString = "test";

	@Property(name = "test_string_trim", trimLength=10)
	private String testStringTrim = "testtrim10EXCESS";
	
	@Property(name = "test_date")
	private Date testDate = new Date();
	
	@Property(name = "test_calendar", exclude = "test")
	private Calendar testCalendar = new GregorianCalendar();
	
	@Property(name = "test_list")
	private List<String> testList = new List<>("one", "two", "three");
	
	@Property(name = "test_sub_list")
	private List<FormSubModel> testSubList = new List<>();
	
	protected FormModel()
	{
		testSubList.add(new FormSubModel("one_1", "one_2", "one_3"));
		testSubList.add(new FormSubModel("two_1", "two_2", "two_3"));
	}
	
	@Override
	public String toString()
	{
		MapperConfig mapperConfig = new MapperConfig().setSerializeNulls(true);
		return new FormMapper(mapperConfig).setPrettyPrint(true).serialize(this);
	}
	
}
