package roth.lib.java.ssh;

import roth.lib.java.lang.Map;

public class SshConfig
{
	protected static String KEY = System.getProperty("user.home") + "/.ssh/id_rsa";
	protected static Integer PORT = 22;
	protected static String USERNAME = "root";
	
	protected String host;
	protected Integer port;
	protected String username;
	protected String password;
	protected String key;
	protected int timeout;
	protected Map<String, String> config = new Map<String, String>();
	
	public SshConfig(String host)
	{
		this(host, false);
	}
	
	public SshConfig(String host, boolean strictHostKeyChecking)
	{
		this.host = host;
		if(!strictHostKeyChecking)
		{
			config.put("StrictHostKeyChecking", "no");
		}
	}
	
	public String getHost()
	{
		return host;
	}
	
	public Integer getPort()
	{
		return port != null ? port : PORT;
	}
	
	public String getUsername()
	{
		return username != null ? username : USERNAME;
	}
	
	public boolean hasPassword()
	{
		return password != null;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String getKey()
	{
		return key != null ? key : KEY;
	}
	
	public Map<String, String> getConfig()
	{
		return config;
	}
	
	public int getTimeout()
	{
		return timeout;
	}
	
	public SshConfig setHost(String host)
	{
		this.host = host;
		return this;
	}
	
	public SshConfig setPort(Integer port)
	{
		this.port = port;
		return this;
	}
	
	public SshConfig setUsername(String username)
	{
		this.username = username;
		return this;
	}
	
	public SshConfig setPassword(String password)
	{
		this.password = password;
		return this;
	}
	
	public SshConfig setKey(String key)
	{
		this.key = key;
		return this;
	}
	
	public SshConfig setConfig(Map<String, String> config)
	{
		this.config = config;
		return this;
	}
	
	public SshConfig setTimeout(int timeout)
	{
		this.timeout = timeout;
		return this;
	}
	
}
