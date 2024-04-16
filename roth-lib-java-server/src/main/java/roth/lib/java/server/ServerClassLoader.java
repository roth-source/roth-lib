package roth.lib.java.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;

import roth.lib.java.server.util.ZipUtil;

public class ServerClassLoader extends URLClassLoader
{
	protected static String WEB_INF 	= "WEB-INF/";
	protected static String LIB			= WEB_INF + "lib/";
	protected static String CLASSES		= WEB_INF + "classes/";
	protected static String JAR		 	= ".jar";
	
	public ServerClassLoader(File file)
	{
		this(ClassLoader.getSystemClassLoader(), urls(file));
	}
	
	public ServerClassLoader(Collection<URL> urls)
	{
		this(ClassLoader.getSystemClassLoader(), urls);
	}
	
	public ServerClassLoader(URL...urls)
	{
		this(ClassLoader.getSystemClassLoader(), urls);
	}
	
	public ServerClassLoader(ClassLoader classLoader, File file)
	{
		this(classLoader, urls(file));
	}
	
	public ServerClassLoader(ClassLoader classLoader, Collection<URL> urls)
	{
		super(urls.toArray(new URL[urls.size()]), classLoader);
	}
	
	public ServerClassLoader(ClassLoader classLoader, URL...urls)
	{
		super(urls, classLoader);
	}
	
	protected static LinkedList<URL> urls(File dir)
	{
		LinkedList<URL> urls = new LinkedList<URL>();
		try
		{
			if(dir.isFile())
			{
				dir = ZipUtil.extract(dir);
			}
			urls.addAll(jarUrls(new File(dir, LIB)));
			urls.add(new File(dir, CLASSES).toURI().toURL());
		}
		catch(MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return urls;
	}
	
	protected static LinkedList<URL> jarUrls(File dir) throws MalformedURLException
	{
		LinkedList<URL> urls = new LinkedList<URL>();
		if(dir.isDirectory())
		{
			for(File file : dir.listFiles())
			{
				if(file.getName().endsWith(JAR))
				{
					urls.add(file.toURI().toURL());
				}
			}
		}
		return urls;
	}
	
	@Override
	public Class<?> findClass(String name)
	{
		Class<?> klass = null;
		try
		{
			klass = super.findClass(name);
		}
		catch (ClassNotFoundException e)
		{
			
		}
		return klass;
	}
	
}
