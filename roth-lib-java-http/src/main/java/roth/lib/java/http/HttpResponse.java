package roth.lib.java.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import roth.lib.java.Characters;

public class HttpResponse<T> implements Characters
{
	protected static String VERSION 		= "version";
	protected static String CODE			= "code";
	protected static String REASON			= "reason";
	protected static Pattern STATUS_PATTERN = Pattern.compile("(?<" + VERSION + ">HTTP/[0-9.]+)\\s+(?<" + CODE + ">[0-9]{3})\\s+(?<" + REASON + ">.+)?$");
	
	protected HttpVersion version = HttpVersion.HTTP_1_1;
	protected HttpStatus status;
	protected HttpHeaders headers;
	protected String body;
	protected T entity;
	protected InputStream input;
	protected HttpURLConnection connection;
	
	public HttpResponse()
	{
		
	}
	
	public HttpVersion getVersion()
	{
		return version;
	}
	
	public HttpStatus getStatus()
	{
		return status;
	}
	
	public HttpHeaders getHeaders()
	{
		return headers;
	}
	
	public String getBody()
	{
		return body;
	}
	
	public T getEntity()
	{
		return entity;
	}
	
	public InputStream getInput()
	{
		return input;
	}
	
	public HttpResponse<T> setVersion(HttpVersion version)
	{
		this.version = version;
		return this;
	}
	
	public HttpResponse<T> setStatus(HttpStatus status)
	{
		this.status = status;
		return this;
	}
	
	public HttpResponse<T> setHeaders(HttpHeaders headers)
	{
		this.headers = headers;
		return this;
	}
	
	public HttpResponse<T> setBody(String body)
	{
		this.body = body;
		return this;
	}
	
	public HttpResponse<T> setEntity(T entity)
	{
		this.entity = entity;
		return this;
	}
	
	public HttpResponse<T> setInput(InputStream input)
	{
		this.input = input;
		return this;
	}
	
	public HttpResponse<T> parseStatus(String statusLine)
	{
		if(statusLine != null)
		{
			Matcher matcher = STATUS_PATTERN.matcher(statusLine);
			if(matcher.find())
			{
				version = HttpVersion.get(matcher.group(VERSION));
				status = new HttpStatus();
				String code = matcher.group(CODE);
				status.parseCode(code);
				status.setReason(matcher.group(REASON));
			}
		}
		return this;
	}
	
	public HttpResponse<T> parseStatus(HttpURLConnection connection)
	{
		status = new HttpStatus();
		this.connection = connection;
		try
		{
			status.parseCode(connection.getResponseCode());
			status.setReason(connection.getResponseMessage());
		}
		catch(Exception e)
		{
			throw new HttpException(e);
		}
		return this;
	}
	
	public boolean isInformational()
	{
		return status != null && status.getStatusCode() != null && status.getStatusCode().isInformational();
	}
	
	public boolean isSuccess()
	{
		return status != null && status.getStatusCode() != null && status.getStatusCode().isSuccess();
	}
	
	public boolean isRedirection()
	{
		return status != null && status.getStatusCode() != null && status.getStatusCode().isRedirection();
	}
	
	public boolean isClientError()
	{
		return status != null && status.getStatusCode() != null && status.getStatusCode().isClientError();
	}
	
	public boolean isServerError()
	{
		return status != null && status.getStatusCode() != null && status.getStatusCode().isServerError();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(NEW_LINE);
		builder.append("RESPONSE");
		builder.append(NEW_LINE);
		builder.append("----------------------------------");
		builder.append(NEW_LINE);
		if(version != null)
		{
			builder.append(version.getValue());
			builder.append(SPACE);
		}
		if(status != null)
		{
			builder.append(status);
			builder.append(SPACE);
		}	
		builder.append(CARRIAGE_RETURN);
		builder.append(NEW_LINE);
		if(headers != null)
		{
			builder.append(headers);
			builder.append(CARRIAGE_RETURN);
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	public void close()
	{
		try
		{
			if(this.input != null)
			{
				this.input.close();
			}
			if (this.connection != null)
			{
				this.connection.disconnect();
			}
		}
		catch(IOException ex)
		{
			System.out.println(ex.getMessage());
			throw new HttpException(ex);
		}
	}
	
}
