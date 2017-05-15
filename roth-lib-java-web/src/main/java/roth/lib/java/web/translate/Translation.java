package roth.lib.java.web.translate;

import java.io.File;
import java.io.Serializable;

import roth.lib.java.annotation.Entity;
import roth.lib.java.annotation.Property;
import roth.lib.java.lang.Map;

@Entity
@SuppressWarnings("serial")
public class Translation implements Serializable, Cloneable
{
	@Property(name = "type")
	protected Map<String, Map<String, Object>> typeFieldMapMap = new Map<>();
	
	@Property(name = "layout")
	protected Map<String, Map<String, Object>> layoutFieldMapMap = new Map<>();
	
	@Property(name = "page")
	protected Map<String, Map<String, Object>> pageFieldMapMap = new Map<>();
	
	@Property(name = "component")
	protected Map<String, Map<String, Object>> componentFieldMapMap = new Map<>();
	
	protected File file;
	
	public Translation()
	{
		
	}
	
	public Map<String, Map<String, Object>> getTypeFieldMapMap()
	{
		return typeFieldMapMap;
	}
	
	public Map<String, Map<String, Object>> getLayoutFieldMapMap()
	{
		return layoutFieldMapMap;
	}
	
	public Map<String, Map<String, Object>> getPageFieldMapMap()
	{
		return pageFieldMapMap;
	}
	
	public Map<String, Map<String, Object>> getComponentFieldMapMap()
	{
		return componentFieldMapMap;
	}
	
	public void setTypeFieldMapMap(Map<String, Map<String, Object>> typeFieldMapMap)
	{
		this.typeFieldMapMap = typeFieldMapMap;
	}
	
	public void setLayoutFieldMapMap(Map<String, Map<String, Object>> layoutFieldMapMap)
	{
		this.layoutFieldMapMap = layoutFieldMapMap;
	}
	
	public void setPageFieldMapMap(Map<String, Map<String, Object>> pageFieldMapMap)
	{
		this.pageFieldMapMap = pageFieldMapMap;
	}
	
	public void setComponentFieldMapMap(Map<String, Map<String, Object>> componentFieldMapMap)
	{
		this.componentFieldMapMap = componentFieldMapMap;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public Translation setFile(File file)
	{
		this.file = file;
		return this;
	}
	
}
