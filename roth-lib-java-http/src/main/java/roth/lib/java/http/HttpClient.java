package roth.lib.java.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import roth.lib.java.inputter.Inputter;
import roth.lib.java.util.UrlUtil;

public class HttpClient
{
	private static final String INVALID_FTP = "ftp://";
	private static final String INVALID_DICT = "dict://";
	private static final String INVALID_FILE = "file://";
	private static final String INVALID_GOPHER = "gopher://";
	
	public HttpClient()
	{
		super();
	}
	
	public HttpConnection connection(HttpUrl url, boolean debug) throws IOException
	{
		if(invalidUrl(url.toString()))
		{
			throw new IOException("Unsafe URL");
		}
		URLConnection connection = new URL(UrlUtil.sanitizeUrl(url.toString())).openConnection();
		if(connection instanceof HttpURLConnection)
		{
			if(connection instanceof HttpsURLConnection)
			{
				setSslSocketFactory((HttpsURLConnection) connection);
			}
			Integer connectionTimeout = getConnectionTimeout();
			if(connectionTimeout != null)
			{
				connection.setConnectTimeout(connectionTimeout);
			}
			Integer readTimeout = getReadTimeout();
			if(readTimeout != null)
			{
				connection.setReadTimeout(readTimeout);
			}
			return new HttpConnection(new HttpUrl(UrlUtil.sanitizeUrl(url.toString())), (HttpURLConnection) connection, debug);
		}
		else
		{
			throw new HttpException("unable to cast connection to HttpURLConnection");
		}
	}
	
	private boolean invalidUrl(String url) {
		if(url == null)
		{
			return true;
		}
		if(url.startsWith(INVALID_FTP) || url.startsWith(INVALID_DICT)  || url.startsWith(INVALID_FILE)  || url.startsWith(INVALID_GOPHER) )
		{
			return true;
		}
		return false;
	}

	protected void setSslSocketFactory(HttpsURLConnection connection)
	{
		
	}
	
	public <T> HttpResponse<T> connect(HttpRequest<?> request, Inputter<T> inputter, boolean debug) throws IOException
	{
		return connection(request.getUrl(), debug).connect(request, inputter);
	}
	
	public HttpResponse<?> connect(HttpRequest<?> request, boolean debug) throws IOException
	{
		return connection(request.getUrl(), debug).connect(request);
	}
	
	public Integer getConnectionTimeout()
	{
		return null;
	}
	
	public Integer getReadTimeout()
	{
		return null;
	}
	
}
