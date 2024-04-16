package roth.lib.java.web.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import roth.lib.java.lang.Map;

public class WebServer
{
	protected static String USER_DIR					= "user.dir";
	protected static String WEB_APP					= "src/main/webapp/";
	protected static String WEB_INF					= "WEB-INF/";
	protected static String PORT						= "port";
	protected static String CONTEXT_PATH				= "contextPath";
	protected static String PROJECT_DIR				= "projectDir";
	protected static String WEB_APP_DIR				= "webAppDir";
	protected static String WEB_INF_DIR				= "webInfDir";
	protected static String KEY_STORE_PATH			= "keyStorePath";
	protected static String KEY_STORE_PASSWORD		= "keyStorePassword";
	protected static String KEY_MANAGER_PASSWORD		= "keyManagerPassword";
	protected static String ENV						= "env";
	
	protected File projectDir = new File(System.getProperty(USER_DIR));
	protected File webAppDir;
	protected File webInfDir;
	protected int port = 9443;
	protected String keyStorePath = getClass().getClassLoader().getResource("localhost.jks").toExternalForm();
	protected String keyStorePassword = "localhost";
	protected String keyManagerPassword = "localhost";
	protected Server server;
	protected SslContextFactory.Server sslContextFactory;
	protected HttpConfiguration httpsConfig;
	protected ServerConnector serverConnector;
	protected WebAppContext webAppContext;
	protected String contextPath = "/";
	protected Scanner scanner;
	protected int scanInterval = 2;
	protected Scanner.Listener scanListener;
	
	public WebServer()
	{
		
	}
	
	public File getProjectDir()
	{
		return projectDir;
	}
	
	public File getWebAppDir()
	{
		return webAppDir;
	}
	
	public File getWebInfDir()
	{
		return webInfDir;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public String getKeyStorePath()
	{
		return keyStorePath;
	}
	
	public String getKeyStorePassword()
	{
		return keyStorePassword;
	}
	
	public String getKeyManagerPassword()
	{
		return keyManagerPassword;
	}
	
	public Server getServer()
	{
		return server;
	}
	
	public ServerConnector getServerConnector()
	{
		return serverConnector;
	}
	
	public WebAppContext getWebAppContext()
	{
		return webAppContext;
	}
	
	public String getContextPath()
	{
		return contextPath;
	}
	
	public Scanner getScanner()
	{
		return scanner;
	}
	
	public int getScanInterval()
	{
		return scanInterval;
	}
	
	public Scanner.Listener getScanListener()
	{
		return scanListener;
	}
	
	public WebServer setProjectDir(File projectDir)
	{
		this.projectDir = projectDir;
		this.webAppDir = new File(projectDir, WEB_APP);
		this.webInfDir = new File(webAppDir, WEB_INF);
		return this;
	}
	
	public WebServer setWebAppDir(File webAppDir)
	{
		this.webAppDir = webAppDir;
		this.webInfDir = new File(webAppDir, WEB_INF);
		return this;
	}
	
	public WebServer setWebInfDir(File webInfDir)
	{
		this.webInfDir = webInfDir;
		return this;
	}
	
	public WebServer setPort(int port)
	{
		this.port = port;
		return this;
	}
	
	public WebServer setKeyStorePath(String keyStorePath)
	{
		this.keyStorePath = keyStorePath;
		return this;
	}
	
	public WebServer setKeyStorePassword(String keyStorePassword)
	{
		this.keyStorePassword = keyStorePassword;
		return this;
	}
	
	public WebServer setKeyManagerPassword(String keyManagerPassword)
	{
		this.keyManagerPassword = keyManagerPassword;
		return this;
	}
	
	public WebServer setServer(Server server)
	{
		this.server = server;
		return this;
	}
	
	public WebServer setServerConnector(ServerConnector serverConnector)
	{
		this.serverConnector = serverConnector;
		return this;
	}
	
	public WebServer setWebAppContext(WebAppContext webAppContext)
	{
		this.webAppContext = webAppContext;
		return this;
	}
	
	public WebServer setContextPath(String contextPath)
	{
		this.contextPath = contextPath;
		return this;
	}
	
	public WebServer setScanner(Scanner scanner)
	{
		this.scanner = scanner;
		return this;
	}
	
	public WebServer setScanInterval(int scanInterval)
	{
		this.scanInterval = scanInterval;
		return this;
	}
	
	public WebServer setScanListener(Scanner.Listener scanListener)
	{
		this.scanListener = scanListener;
		return this;
	}
	
	public void start()
	{
		try
		{
			if(webAppDir == null)
			{
				webAppDir = new File(projectDir, WEB_APP);
			}
			if(webInfDir == null)
			{
				webInfDir = new File(webAppDir, WEB_INF);
			}
			server = new Server();
			sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setKeyStorePath(keyStorePath);
			sslContextFactory.setKeyStorePassword(keyStorePassword);
			sslContextFactory.setKeyManagerPassword(keyManagerPassword);
			httpsConfig = new HttpConfiguration();
			httpsConfig.setSecureScheme("https");
			httpsConfig.addCustomizer(new SecureRequestCustomizer());
			httpsConfig.setSecurePort(port);
			
			serverConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(httpsConfig));
			serverConnector.setPort(port);
			webAppContext = new WebAppContext();
			scanner = new Scanner();
			server.setConnectors(new Connector[]{serverConnector});
			webAppContext.setWar(webAppDir.getAbsolutePath());
			webAppContext.setContextPath(contextPath);
			server.setHandler(webAppContext);
			scanner.setScanInterval(scanInterval);
			java.util.List<File> directories = new ArrayList<File>();
			directories.add(webInfDir);
			scanner.setScanDirs(directories);
			scanner.addListener(scanListener);
			scanner.start();
			server.start();
			server.join();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args)
	{
		WebServer devServer = new WebServer();
		Map<String, String> argMap = parseArgMap(args);
		if(argMap.containsKey(PORT))
		{
			try
			{
				devServer.setPort(Integer.parseInt(argMap.get(PORT)));
			}
			catch(Exception e)
			{
				
			}
		}
		if(argMap.containsKey(CONTEXT_PATH))
		{
			devServer.setContextPath(argMap.get(CONTEXT_PATH));
		}
		if(argMap.containsKey(PROJECT_DIR))
		{
			devServer.setProjectDir(new File(argMap.get(PROJECT_DIR)));
		}
		if(argMap.containsKey(WEB_APP_DIR))
		{
			devServer.setWebAppDir(new File(argMap.get(WEB_APP_DIR)));
		}
		if(argMap.containsKey(WEB_INF_DIR))
		{
			devServer.setWebInfDir(new File(argMap.get(WEB_INF_DIR)));
		}
		if(argMap.containsKey(KEY_STORE_PATH))
		{
			devServer.setKeyStorePath(argMap.get(KEY_STORE_PATH));
		}
		if(argMap.containsKey(KEY_STORE_PASSWORD))
		{
			devServer.setKeyStorePassword(argMap.get(KEY_STORE_PASSWORD));
		}
		if(argMap.containsKey(KEY_MANAGER_PASSWORD))
		{
			devServer.setKeyManagerPassword(argMap.get(KEY_MANAGER_PASSWORD));
		}
		if(argMap.containsKey(ENV))
		{
			System.setProperty(ENV, argMap.get(ENV));
		}
		devServer.start();
	}
	
	protected static Map<String, String> parseArgMap(String[] args)
	{
		Map<String, String> argMap = new Map<String, String>();
		for(String arg : Arrays.asList(args))
		{
			if(arg.startsWith("-"))
			{
				String name = null;
				String value = null;
				int index = arg.indexOf("=");
				if(index > -1)
				{
					name = arg.substring(1, index);
					value = arg.substring(index + 1);
				}
				argMap.put(name, value);
			}
		}
		return argMap;
	}
	
}
