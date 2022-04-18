package roth.lib.java.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.ConnectException;

import roth.lib.java.Characters;
import roth.lib.java.Generic;
import roth.lib.java.http.HttpClient;
import roth.lib.java.http.HttpHeader;
import roth.lib.java.http.HttpHeaders;
import roth.lib.java.http.HttpMethod;
import roth.lib.java.http.HttpRequest;
import roth.lib.java.http.HttpResponse;
import roth.lib.java.http.HttpStatus;
import roth.lib.java.http.HttpStatusCode;
import roth.lib.java.http.HttpUrl;
import roth.lib.java.inputter.Inputter;
import roth.lib.java.inputter.MapperInputter;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.mapper.MapperType;
import roth.lib.java.outputter.MapperOutputter;
import roth.lib.java.outputter.Outputter;
import roth.lib.java.reflector.MapperReflector;
import roth.lib.java.serializer.Serializer;
import roth.lib.java.type.MimeType;

public abstract class ApiClient<ApiRequest, ApiResponse> extends HttpClient implements Characters
{
	protected static String RAW_BODY		= "RAW BODY";
	protected static String MAPPED_ENTITY	= "MAPPED ENTITY";
	
	protected MapperType requestMapperType;
	protected MapperType responseMapperType;
	protected MapperReflector mapperReflector;
	protected MapperConfig mapperConfig;
	protected boolean debug;
	protected PrintWriter logWriter;
	
	public ApiClient(boolean debug, MapperType requestMapperType, MapperType responseMapperType)
	{
		this.debug = debug;
		this.requestMapperType = requestMapperType;
		this.responseMapperType = responseMapperType;
		mapperReflector = MapperReflector.get();
		mapperConfig = MapperConfig.get();
	}
	
	public MapperType getRequestMapperType()
	{
		return requestMapperType;
	}
	
	public MapperType getResponseMapperType()
	{
		return responseMapperType;
	}
	
	public MapperReflector getMapperReflector()
	{
		return mapperReflector;
	}
	
	public MapperConfig getMapperConfig()
	{
		return mapperConfig;
	}
	
	public ApiClient<ApiRequest, ApiResponse> setMapperReflector(MapperReflector mapperReflector)
	{
		this.mapperReflector = mapperReflector;
		return this;
	}
	
	public ApiClient<ApiRequest, ApiResponse> setMapperConfig(MapperConfig mapperConfig)
	{
		this.mapperConfig = mapperConfig;
		return this;
	}
	
	public boolean isDebug()
	{
		return debug;
	}
	
	public ApiClient<ApiRequest, ApiResponse> setDebug(boolean debug)
	{
		this.debug = debug;
		return this;
	}
	
	protected MimeType getRequestContentType()
	{
		MimeType contentType = null;
		switch(getRequestMapperType())
		{
			case JSON:
			{
				contentType = MimeType.APPLICATION_JSON;
				break;
			}
			case XML:
			{
				contentType = MimeType.APPLICATION_XML;
				break;
			}
			case FORM:
			{
				contentType = MimeType.APPLICATION_X_WWW_FORM_URLENCODED;
				break;
			}
			case MULTIPART_FORM:
			{
				contentType = MimeType.MULTIPART_FORM_DATA;
				break;
			}
			case TABLE:
			case FIXED_WIDTH_TABLE:
			case MYSQL:
			{
				break;
			}
		}
		return contentType;
	}
	
	protected MimeType getResponseContentType()
	{
		MimeType contentType = null;
		switch(getResponseMapperType())
		{
			case JSON:
			{
				contentType = MimeType.APPLICATION_JSON;
				break;
			}
			case XML:
			{
				contentType = MimeType.APPLICATION_XML;
				break;
			}
			case FORM:
			{
				contentType = MimeType.APPLICATION_X_WWW_FORM_URLENCODED;
				break;
			}
			case MULTIPART_FORM:
			{
				contentType = MimeType.MULTIPART_FORM_DATA;
				break;
			}
			case TABLE:
			case FIXED_WIDTH_TABLE:
			case MYSQL:
			{
				break;
			}
		}
		return contentType;
	}
	
	public PrintWriter getLogWriter()
	{
		if(logWriter == null)
		{
			logWriter = new PrintWriter(System.out, true);
		}
		return logWriter;
	}
	
	public void setLogWriter(PrintWriter logWriter)
	{
		this.logWriter = logWriter;
	}
	
	protected abstract HttpUrl url();
	
	protected Outputter getOutputter(ApiRequest apiRequest)
	{
		return new MapperOutputter<ApiRequest>(getRequestMapperType(), getMapperReflector(), getMapperConfig(), apiRequest);
	}
	
	protected <T extends ApiResponse> Inputter<T> getInputter(Type responseType)
	{
		return new MapperInputter<T>(getResponseMapperType(), getMapperReflector(), getMapperConfig(), responseType);
	}
	
	protected String debugRequest(ApiRequest apiRequest)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		getMapperReflector().getMapper(getRequestMapperType(), getMapperConfig()).setPrettyPrint(true).serialize(apiRequest, output);
		return output.toString();
	}
	
	protected String debugResponse(ApiResponse apiResponse)
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		getMapperReflector().getMapper(getResponseMapperType(), getMapperConfig()).setPrettyPrint(true).serialize(apiResponse, output);
		return output.toString();
	}
	
	protected String debugBody(String body)
	{
		return getMapperReflector().getMapper(getResponseMapperType(), getMapperConfig()).prettyPrint(body);
	}
	
	protected void setHeaders(HttpHeaders headers)
	{
		
	}
	
	protected <T extends ApiResponse> T head(HttpUrl url, HttpHeader... headers)
	{
		return head(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T head(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return head(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T head(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, null, responseType, HttpMethod.HEAD, false, headers);
	}
	
	protected <T extends ApiResponse> T get(HttpUrl url, HttpHeader... headers)
	{
		return get(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T get(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return get(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T get(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, null, responseType, HttpMethod.GET, false, headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, HttpHeader... headers)
	{
		return post(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return post(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return post(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return post(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return post(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connect(url, apiRequest, null, responseType, HttpMethod.POST, false, headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return post(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return post(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T post(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, outputter, responseType, HttpMethod.POST, false, headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, HttpHeader... headers)
	{
		return put(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return put(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return put(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return put(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return put(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connect(url, apiRequest, null, responseType, HttpMethod.PUT, false, headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return put(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return put(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T put(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, outputter, responseType, HttpMethod.PUT, false, headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, HttpHeader... headers)
	{
		return delete(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return delete(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return delete(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return delete(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return delete(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connect(url, apiRequest, null, responseType, HttpMethod.DELETE, false, headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return delete(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return delete(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T delete(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, outputter, responseType, HttpMethod.DELETE, false, headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, HttpHeader... headers)
	{
		return postGzip(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postGzip(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return postGzip(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return postGzip(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postGzip(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connect(url, apiRequest, null, responseType, HttpMethod.POST, true, headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return postGzip(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postGzip(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T postGzip(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, outputter, responseType, HttpMethod.POST, true, headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, HttpHeader... headers)
	{
		return putGzip(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putGzip(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return putGzip(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return putGzip(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putGzip(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connect(url, apiRequest, null, responseType, HttpMethod.PUT, true, headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return putGzip(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putGzip(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T putGzip(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, outputter, responseType, HttpMethod.PUT, true, headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, HttpHeader... headers)
	{
		return deleteGzip(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteGzip(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return deleteGzip(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return deleteGzip(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteGzip(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connect(url, apiRequest, null, responseType, HttpMethod.DELETE, true, headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return deleteGzip(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url,  Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteGzip(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> T deleteGzip(HttpUrl url,  Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connect(url, null, outputter, responseType, HttpMethod.DELETE, true, headers);
	}
	
	protected <T extends ApiResponse> T connect(HttpUrl url, ApiRequest apiRequest, Outputter outputter, Type responseType, HttpMethod method, boolean gzip, HttpHeader... headers)
	{
		HttpResponse<T> response = connectResponse(url, apiRequest, outputter, responseType, method, gzip, headers);
		return response.getEntity();
	}
	
	protected <T extends ApiResponse> HttpResponse<T> headResponse(HttpUrl url, HttpHeader... headers)
	{
		return headResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> headResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return headResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> headResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, null, responseType, HttpMethod.HEAD, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> getResponse(HttpUrl url, HttpHeader... headers)
	{
		return getResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> getResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return getResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> getResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, null, responseType, HttpMethod.GET, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, HttpHeader... headers)
	{
		return postResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return postResponse(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return postResponse(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postResponse(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, apiRequest, null, responseType, HttpMethod.POST, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return postResponse(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postResponse(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postResponse(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, outputter, responseType, HttpMethod.POST, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, HttpHeader... headers)
	{
		return putResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return putResponse(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return putResponse(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putResponse(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, apiRequest, null, responseType, HttpMethod.PUT, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return putResponse(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putResponse(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putResponse(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, outputter, responseType, HttpMethod.PUT, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, HttpHeader... headers)
	{
		return deleteResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return deleteResponse(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return deleteResponse(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteResponse(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, apiRequest, null, responseType, HttpMethod.DELETE, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return deleteResponse(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteResponse(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteResponse(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, outputter, responseType, HttpMethod.DELETE, false, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, HttpHeader... headers)
	{
		return postGzipResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postGzipResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return postGzipResponse(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return postGzipResponse(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postGzipResponse(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, apiRequest, null, responseType, HttpMethod.POST, true, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return postGzipResponse(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return postGzipResponse(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> postGzipResponse(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, outputter, responseType, HttpMethod.POST, true, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, HttpHeader... headers)
	{
		return putGzipResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putGzipResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return putGzipResponse(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return putGzipResponse(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putGzipResponse(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, apiRequest, null, responseType, HttpMethod.PUT, true, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return putGzipResponse(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return putGzipResponse(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> putGzipResponse(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, outputter, responseType, HttpMethod.PUT, true, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, HttpHeader... headers)
	{
		return deleteGzipResponse(url, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteGzipResponse(url, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, Type responseType, HttpHeader... headers)
	{
		return deleteGzipResponse(url, (ApiRequest) null, responseType, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, ApiRequest apiRequest, HttpHeader... headers)
	{
		return deleteGzipResponse(url, apiRequest, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, ApiRequest apiRequest, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteGzipResponse(url, apiRequest, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, ApiRequest apiRequest, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, apiRequest, null, responseType, HttpMethod.DELETE, true, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, Outputter outputter, HttpHeader... headers)
	{
		return deleteGzipResponse(url, outputter, (Type) null, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, Outputter outputter, Generic<T> responseGeneric, HttpHeader... headers)
	{
		return deleteGzipResponse(url, outputter, responseGeneric.getType(), headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> deleteGzipResponse(HttpUrl url, Outputter outputter, Type responseType, HttpHeader... headers)
	{
		return connectResponse(url, null, outputter, responseType, HttpMethod.DELETE, true, headers);
	}
	
	protected <T extends ApiResponse> HttpResponse<T> connectResponse(HttpUrl url, ApiRequest apiRequest, Outputter outputter, Type responseType, HttpMethod method, boolean gzip, HttpHeader...headers)
	{
		try
		{
			HttpRequest<ApiRequest> request = new HttpRequest<ApiRequest>();
			request.setMethod(method);
			request.setUrl(url);
			HttpHeaders httpHeaders = url.getHeaders();
			MimeType requestContentType = getRequestContentType();
			if(requestContentType != null)
			{
				if(MapperType.MULTIPART_FORM.equals(getRequestMapperType()))
				{
					httpHeaders.setContentType(requestContentType, getMapperConfig().getBoundary());
				}
				else
				{
					httpHeaders.setContentType(requestContentType);
				}
			}
			MimeType responseContentType = getResponseContentType();
			if(requestContentType != null)
			{
				httpHeaders.setAccept(responseContentType);
			}
			httpHeaders.setHeaders(headers);
			setHeaders(httpHeaders);
			request.setHeaders(httpHeaders);
			if(apiRequest != null)
			{
				if(apiRequest instanceof Outputter)
				{
					request.setOutputter(((Outputter) apiRequest).setGzip(gzip));
				}
				else
				{
					request.setOutputter(getOutputter(apiRequest).setGzip(gzip));
				}
			}
			else if(outputter != null)
			{
				request.setOutputter(outputter.setGzip(gzip));
			}
			if(isDebug())
			{
				StringBuilder builder = new StringBuilder();
				builder.append(request);
				if(apiRequest != null && !(apiRequest instanceof Serializer))
				{
					builder.append(debugRequest(apiRequest));
				}
				logRequest(builder.toString());
			}
			Inputter<T> inputter = null;
			if(responseType != null)
			{
				inputter = getInputter(responseType);
			}
			HttpResponse<T> response = connect(request, inputter, isDebug());
			T apiResponse = response.getEntity();
			if(isDebug())
			{
				StringBuilder builder = new StringBuilder();
				builder.append(response);
				String body = response.getBody();
				if(body != null)
				{
					builder.append(RAW_BODY);
					builder.append(NEW_LINE);
					builder.append(debugBody(body));
				}
				if(apiResponse != null)
				{
					builder.append(MAPPED_ENTITY);
					builder.append(NEW_LINE);
					builder.append(debugResponse(apiResponse));
				}
				logResponse(builder.toString());
			}
			if(!response.isSuccess())
			{
				checkError(response);
			}
			checkResponse(response);
			return response;
		}
		catch(ConnectException e)
		{
			HttpResponse<T> httpResponse = new HttpResponse<T>();
			HttpStatus status = new HttpStatus().setStatusCode(HttpStatusCode._503);
			httpResponse.setStatus(status);
			return httpResponse;
		}
		catch(IOException e)
		{
			if(e.getMessage() != null && e.getMessage().length() > 2)
			{
				int loc = e.getMessage().indexOf(' ');
				if(loc > 0)
				{
					String msg = e.getMessage().substring(0, loc);
					if(msg != null && msg.length() > 1)
					{
						HttpStatusCode code = HttpStatusCode.fromString(msg);
						if(code != null)
						{
							HttpResponse<T> httpResponse = new HttpResponse<T>();
							HttpStatus status = new HttpStatus().setStatusCode(code);
							httpResponse.setStatus(status);
							return httpResponse;
						}
						
					}
				}
			}
			
			logException(e);
			throw new ApiException(e);
		}
		catch(Exception ex)
		{
			logException(ex);
			throw ex;
		}
	}
	
	protected void logRequest(String request)
	{
		getLogWriter().println(request);
	}
	
	protected void logResponse(String response)
	{
		getLogWriter().println(response);
	}
	
	protected void logException(Exception ex)
	{
		getLogWriter().println(ex.getCause() != null ? ex.getCause().toString() : ex.getMessage());
	}

	
	protected <T extends ApiResponse> void checkError(HttpResponse<T> response)
	{
		
	}
	
	protected <T extends ApiResponse> void checkResponse(HttpResponse<T> response)
	{
		
	}
	
}
