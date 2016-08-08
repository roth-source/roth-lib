package roth.lib.java.web.translate;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import roth.lib.java.annotation.Entity;
import roth.lib.java.annotation.Property;

@Entity
@SuppressWarnings("serial")
public class Text implements Serializable, Cloneable
{
	
	@Property(name = "type")
	protected TreeMap<String, TreeMap<String, Object>> type;
	
	@Property(name = "layout")
	protected TreeMap<String, TreeMap<String, Object>> layout;
	
	@Property(name = "page")
	protected TreeMap<String, TreeMap<String, Object>> page;
	
	@Property(name = "component")
	protected TreeMap<String, TreeMap<String, Object>> component;
	
	protected File file;
	
	public Text()
	{
		
	}
	
	public TreeMap<String, TreeMap<String, Object>> getType()
	{
		return type;
	}
	
	public TreeMap<String, TreeMap<String, Object>> getLayout()
	{
		return layout;
	}
	
	public TreeMap<String, TreeMap<String, Object>> getPage()
	{
		return page;
	}
	
	public TreeMap<String, TreeMap<String, Object>> getComponent()
	{
		return component;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public Text setFile(File file)
	{
		this.file = file;
		return this;
	}
	
}