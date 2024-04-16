package roth.lib.java.util;

import java.net.URLDecoder;
import java.net.URLEncoder;

import roth.lib.java.Characters;

public class UrlUtil implements Characters
{
	
	protected UrlUtil()
	{
		
	}
	
	public static String encode(String value)
	{
		try
		{
			return URLEncoder.encode(value, UTF_8.toString());
		}
		catch(Exception e)
		{
			return value;
		}
	}
	
	public static String decode(String value)
	{
		try
		{
			return URLDecoder.decode(value, UTF_8.toString());
		}
		catch(Exception e)
		{
			return value;
		}
	}
	
	public static String escapePath(String path)
	{
		return path.replaceAll(" ", "%20");
	}
	
	public static String sanitizeUrl(String url)
	{
		if(url == null)
		{
			return null;
		}
		return url.replaceAll("[\\r\\n]", "").replace(";", "").replace("\\/", "");
	}
	
	public static String sanitizeHeader(String headerValue)
	{
		if(headerValue == null)
		{
			return null;
		}
		return headerValue.replaceAll("[\\r\\n]", "").replace(";", "").replace("\\/", "");
		
	}

}
