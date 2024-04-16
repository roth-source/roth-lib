package roth.lib.java.http;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import roth.lib.java.Characters;
import roth.lib.java.inputter.Inputter;
import roth.lib.java.lang.List;
import roth.lib.java.util.UrlUtil;

public class HttpConnection implements Characters
{
	protected HttpUrl url;
	protected HttpURLConnection connection;
	protected boolean debug;
	
	public HttpConnection(HttpUrl url, HttpURLConnection connection, boolean debug)
	{
		this.url = url;
		this.connection = connection;
		this.debug = debug;
	}
	
	public HttpUrl getUrl()
	{
		return url;
	}
	
	public HttpURLConnection getConnection()
	{
		return connection;
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		return connection.getOutputStream();
	}
	
	public Writer getWriter() throws IOException
	{
		return new OutputStreamWriter(getOutputStream(), UTF_8);
	}
	
	public BufferedWriter getBufferedWriter() throws IOException
	{
		return new BufferedWriter(getWriter());
	}
	
	public InputStream getInputStream() throws IOException
	{
		return connection.getInputStream();
	}
	
	public InputStream getErrorStream() throws IOException
	{
		return connection.getErrorStream();
	}
	
	public InputStream getAnyStream()
	{
		InputStream input = null;
		try
		{
			input = getInputStream();
		}
		catch(Exception e)
		{
			
		}
		if(input == null)
		{
			try
			{
				input = getErrorStream();
			}
			catch(Exception e)
			{
				
			}
		}
		return input;
	}
	
	public GZIPInputStream getGzipInputStream() throws IOException
	{
		return new GZIPInputStream(getInputStream());
	}
	
	public Reader getReader() throws IOException
	{
		return new InputStreamReader(getInputStream(), UTF_8);
	}
	
	public Reader getGzipReader() throws IOException
	{
		return new InputStreamReader(getGzipInputStream(), UTF_8);
	}
	
	public <T> HttpResponse<T> connect(HttpRequest<?> request, Inputter<T> inputter) throws IOException
	{
		connection.setDoInput(true);
		connection.setRequestMethod(request.getMethod().toString());
		setRequestHeaders(request.getHeaders());
		if(!HttpMethod.GET.equals(request.getMethod()) && request.hasOutputter())
		{
			connection.setDoOutput(true);
			try(OutputStream output = getOutputStream())
			{
				request.getOutputter().wrap(output);
			}
		}
		connection.connect();
		HttpResponse<T> response = new HttpResponse<T>();
		response.parseStatus(connection);
		response.setHeaders(getResponseHeaders());
		if(inputter != null)
		{
			try(InputStream input = getAnyStream())
			{
				if(debug || !response.isSuccess())
				{
					ByteArrayOutputStream output = readAll(input);
					response.setInput(new ByteArrayInputStream(output.toByteArray()));
					response.setBody(new String(output.toByteArray(), UTF_8));
					response.setEntity(inputter.input(response.getInput()));
				}
				else
				{
					response.setEntity(inputter.input(input));
				}
			}
		}
		else
		{
			response.setInput(getAnyStream());
		}
		return response;
	}
	
	public HttpResponse<?> connect(HttpRequest<?> request) throws IOException
	{
		connection.setDoInput(true);
		connection.setRequestMethod(request.getMethod().toString());
		setRequestHeaders(request.getHeaders());
		if(request.hasOutputter())
		{
			connection.setDoOutput(true);
			request.getOutputter().wrap(getOutputStream());
		}
		connection.connect();
		HttpResponse<?> response = new HttpResponse<>();
		response.parseStatus(connection);
		response.setHeaders(getResponseHeaders());
		response.setInput(getAnyStream());
		return response;
	}
	
	protected void setRequestHeaders(HttpHeaders headers)
	{
		for(Entry<String, List<String>> headersEntry : headers.getHeadersMap().entrySet())
		{
			String name = headersEntry.getKey();
			for(String value : headersEntry.getValue())
			{
				if(value != null && !value.isEmpty())
				{
					connection.addRequestProperty(name, value);
				}
			}
		}
	}
	
	protected HttpHeaders getResponseHeaders()
	{
		HttpHeaders headers = new HttpHeaders();
		for(Entry<String, java.util.List<String>> headerEntry : connection.getHeaderFields().entrySet())
		{
			String name = headerEntry.getKey();
			if(name != null)
			{
				try
				{
					headers.setHeader(URLEncoder.encode(UrlUtil.sanitizeHeader(name), java.nio.charset.StandardCharsets.UTF_8.toString()), headerEntry.getValue() == null ? null : sanitizeHeaders(headerEntry.getValue()));
				}
				catch(IOException ex)
				{
					
				}
				
			}
		}
		return headers;
	}
	
	private java.util.List<String> sanitizeHeaders(java.util.List<String> headers) throws IOException
	{
		java.util.List<String> newList = new ArrayList<String>();
		for(String header : headers)
		{
			newList.add(URLEncoder.encode(UrlUtil.sanitizeHeader(header), java.nio.charset.StandardCharsets.UTF_8.toString()));
		}
		return newList;
	}
	
	protected String readLine(InputStream input) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int b;
		read: while((b = input.read()) > -1)
		{
			switch(b)
			{
				case CARRIAGE_RETURN:
				{
					break;
				}
				case NEW_LINE:
				{
					break read;
				}
				default:
				{
					output.write(b);
					break;
				}
			}
		}
		return new String(output.toByteArray(), UTF_8);
	}
	
	protected ByteArrayOutputStream readAll(InputStream input) throws IOException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if(input != null)
		{
			byte[] buffer = new byte[1024];
			int n = 0;
			while((n = input.read(buffer)) != -1)
			{
				output.write(buffer, 0, n);
			}
			output.flush();
		}
		return output;
	}
	
}
