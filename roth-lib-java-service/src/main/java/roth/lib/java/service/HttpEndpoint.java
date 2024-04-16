package roth.lib.java.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.net.InternetDomainName;

import roth.lib.java.Characters;
import roth.lib.java.form.FormMapper;
import roth.lib.java.form.MultipartFormMapper;
import roth.lib.java.http.HttpMethod;
import roth.lib.java.http.HttpUrl;
import roth.lib.java.lang.List;
import roth.lib.java.lang.Map;
import roth.lib.java.mapper.Mapper;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.mapper.MapperType;
import roth.lib.java.reflector.EntityReflector;
import roth.lib.java.reflector.MapperReflector;
import roth.lib.java.reflector.PropertyReflector;
import roth.lib.java.service.annotation.Service;
import roth.lib.java.service.annotation.ServiceMethod;
import roth.lib.java.service.reflector.MethodReflector;
import roth.lib.java.time.Time;
import roth.lib.java.type.MimeType;
import roth.lib.java.util.EnumUtil;
import roth.lib.java.util.IoUtil;
import roth.lib.java.util.ReflectionUtil;
import roth.lib.java.util.UrlUtil;
import roth.lib.java.validate.Validator;
import roth.lib.java.validate.ValidatorException;

@SuppressWarnings("serial")
public abstract class HttpEndpoint extends HttpServlet implements Characters
{
	protected static String ORIGIN 								= "Origin";
	protected static String ANY 									= "*";
	protected static List<String> EXPOSED_HEADERS				= new List<>(HttpService.X_SESSION, HttpService.X_CSRF_TOKEN);
	protected static String ACCESS_CONTROL_ALLOW_ORIGIN 			= "Access-Control-Allow-Origin";
	protected static String ACCESS_CONTROL_ALLOW_CREDENTIALS 		= "Access-Control-Allow-Credentials";
	protected static String ACCESS_CONTROL_ALLOW_METHODS 			= "Access-Control-Allow-Methods";
	protected static String CONTENT_SECURITY_POLICY 				= "Content-Security-Policy";
	protected static String ACCESS_CONTROL_EXPOSE_HEADERS 		    = "Access-Control-Expose-Headers";
	protected static String STRICT_TRANSPORT_SECURITY			    = "Strict-Transport-Security";
	protected static String DEFAULT_SOURCE_SELF                     ="default-src 'self' ";
	protected static String CONTENT_TYPE_PARAM	 					= "contentType";
	protected static String CONTENT_TYPE_HEADER 					= "Content-Type";
	protected static String ACCEPT_PARAM		 					= "accept";
	protected static String ACCEPT_HEADER		 					= "Accept";
	protected static String ALLOWED_METHODS 						= "GET, POST";
	protected static List<HttpMethod> SUPPORTED_METHODS			= List.fromArray(HttpMethod.GET, HttpMethod.POST);
	protected static List<String> LOCALHOSTS 					= List.fromArray("localhost", "127.0.0.1");
	protected static String ENDPOINT 							= "_endpoint";
	protected static String SERVICE 								= "service";
	protected static String METHOD 								= "method";
	protected static Pattern SERVICE_METHOD_PATTERN 				= Pattern.compile("(?:^|/)(?<" + SERVICE + ">[\\w\\-]+)/(?<" + METHOD + ">[\\w]+)(?:/|$)");
	protected static Pattern BOUNDARY_PATTERN					= Pattern.compile("boundary\\=(?:\")?(.+?)(?:\"|;|$)");
	protected static String MAX_LENGTH_ERROR 					= "%d exceeds max length of %d characters";
	
	protected static Map<HttpServiceMethod, MethodReflector> methodReflectorMap = new Map<HttpServiceMethod, MethodReflector>();
	
	protected MapperReflector mapperReflector = MapperReflector.get();
	protected MapperConfig mapperConfig = MapperConfig.get();
	
	public static void register(Class<? extends HttpService> serviceClass)
	{
		Service service = serviceClass.getDeclaredAnnotation(Service.class);
		if(service != null)
		{
			String serviceName = service.name();
			if(serviceName != null && !serviceName.isEmpty())
			{
				for(Method method : serviceClass.getDeclaredMethods())
				{
					ServiceMethod serviceMethod = method.getDeclaredAnnotation(ServiceMethod.class);
					if(serviceMethod != null)
					{
						MethodReflector methodReflector = new MethodReflector(serviceClass, method, serviceMethod);
						String methodName = methodReflector.getMethodName();
						HttpServiceMethod httpServiceMethod = new HttpServiceMethod(serviceName, methodName);
						methodReflectorMap.put(httpServiceMethod, methodReflector);
					}
				}
			}
		}
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
	{
		Time startTime = new Time();
		HttpService service = null;
		Object methodRequest = null;
		Mapper requestMapper = null;
		Object methodResponse = null;
		String debugRequest = "";
		HttpServiceMethod serviceMethod = null;
		MethodReflector methodReflector = null;
		ByteArrayInputStream rawInput = null;
		List<HttpError> errors = new List<HttpError>();
		MimeType requestContentType = getRequestContentType(request, response);
		MimeType responseContentType = getResponseContentType(request, response);
		Mapper responseMapper = getResponseMapper(request, response, responseContentType);
		response.setHeader(CONTENT_TYPE_HEADER, responseContentType.toString());
		boolean dev = isDev(request, response);
		try
		{
			if(dev || isOriginAllowed(request, response))
			{
				String origin = request.getHeader(ORIGIN);
				if(origin != null)
				{
					response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, UrlUtil.sanitizeUrl(origin));
					if(restrictSecurityOrigin())
					{
						response.setHeader(CONTENT_SECURITY_POLICY, dev ? UrlUtil.sanitizeUrl(origin) : getContentSecurityOrigins(UrlUtil.sanitizeUrl(origin)));
					}
					response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
				}
				else
				{
					response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, dev ? ANY : getSecurityHost(request));
				}
				response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
				response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS.toString());
				response.setHeader(STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");
				HttpMethod httpMethod = HttpMethod.fromString(request.getMethod());
				if(httpMethod != null)
				{
					if(SUPPORTED_METHODS.contains(httpMethod))
					{
						serviceMethod = getServiceMethod(request, response);
						if(serviceMethod != null)
						{
							logRequest(serviceMethod.getServiceName(), serviceMethod.getMethodName(), request);
							if(!ENDPOINT.equalsIgnoreCase(serviceMethod.getServiceName()))
							{
								methodReflector = getMethodReflector(request, response, serviceMethod);
								if(methodReflector != null)
								{
									service = methodReflector.getService();
									if(service != null)
									{
										service.setServletContext(request.getServletContext()).setHttpServletRequest(request).setHttpServletResponse(response);
										service.setService(serviceMethod.getServiceName()).setMethod(serviceMethod.getMethodName());
										responseMapper.setContext(methodReflector.getContext());
										responseMapper.setPrettyPrint(methodReflector.isPrettyPrint());
										if(methodReflector.isHttpMethodImplemented(httpMethod))
										{
											boolean hasAjax = methodReflector.isAjax();
											boolean hasApi = methodReflector.isApi();
											boolean ajaxAuthenticated = hasAjax && (!methodReflector.isAuthenticated() || service.isAjaxAuthenticated(methodReflector));
											boolean apiAuthenticated = hasApi && (!methodReflector.isAuthenticated() || service.isApiAuthenticated(methodReflector));
											if(ajaxAuthenticated || apiAuthenticated)
											{
												Parameter parameter = methodReflector.getParameter();
												if(parameter != null)
												{
													if(!HttpMethod.GET.equals(httpMethod))
													{
														InputStream input = methodReflector.isGzippedInput() ? new GZIPInputStream(request.getInputStream()) : request.getInputStream();
														if(service.isDebug() && methodReflector.isRawRequest())
														{
															rawInput = new ByteArrayInputStream(IoUtil.toBytes(input));
															input = rawInput;
														}
														service.setRequestContentType(requestContentType);
														requestMapper = getRequestMapper(request, response, requestContentType);
														requestMapper.setContext(methodReflector.getContext()).setContentType(requestContentType);
														if(requestMapper instanceof MultipartFormMapper)
														{
															Matcher matcher = BOUNDARY_PATTERN.matcher(request.getHeader(CONTENT_TYPE_HEADER));
															if(matcher.find())
															{
																((MultipartFormMapper) requestMapper).setBoundary(matcher.group(1));
															}
														}
														service.setRequestMapper(requestMapper);
														methodRequest = requestMapper.deserialize(input, parameter.getParameterizedType());
													}
													else
													{
														requestMapper = getMapperReflector().getMapper(MapperType.FORM, getMapperConfig());
														requestMapper.setContext(methodReflector.getContext()).setContentType(requestContentType);
														if(requestMapper instanceof FormMapper)
														{
															methodRequest = ((FormMapper) requestMapper).deserialize(HttpUrl.parseParamMap(request.getQueryString()), parameter.getParameterizedType());
														}
													}
												}
												boolean validCsrf = !(ajaxAuthenticated && methodReflector.isAuthenticated() && !service.isValidCsrfToken());
												if(validCsrf)
												{
													boolean authorized = !methodReflector.isAuthenticated() || service.isAuthorized(methodReflector, methodRequest);
													errors.addAll(service.getErrors());
													service.clearErrors();
													if(authorized)
													{
														if(methodRequest != null && requestMapper != null)
														{
															errors.addAll(validate(request, response, methodRequest, requestMapper.getMapperType()));
														}
														if(errors.isEmpty())
														{
															service.setResponseContentType(responseContentType);
															service.setResponseMapper(responseMapper);
															try
															{
																methodResponse = methodReflector.invoke(service, methodRequest);
															}
															catch(InvocationTargetException e)
															{
																if(e.getCause() != null)
																{
																	Throwable cause = e.getCause();
																	HttpErrorType errorType = HttpErrorType.SERVICE_EXCEPTION;
																	if(cause instanceof SQLNonTransientConnectionException || cause instanceof SQLTransientConnectionException)
																	{
																		errorType = HttpErrorType.DATABASE_CONNECTION_EXCEPTION;
																	}
																	errors.add(service.exception(errorType.error(cause), cause));
																}
															}
															errors.addAll(service.getErrors());
															service.clearErrors();
														}
													}
													else
													{
														errors.add(HttpErrorType.SERVICE_NOT_AUTHORIZED.error());
													}
												}
												else
												{
													errors.add(HttpErrorType.SERVICE_CSRF_TOKEN_INVALID.error());
												}
											}
											else
											{
												if(hasAjax && !ajaxAuthenticated)
												{
													errors.add(HttpErrorType.SERVICE_AJAX_NOT_AUTHENTICATED.error());
												}
												else if(hasApi && !apiAuthenticated)
												{
													errors.add(HttpErrorType.SERVICE_API_NOT_AUTHENTICATED.error());
												}
												else
												{
													errors.add(HttpErrorType.SERVICE_CHANNEL_NOT_IMPLEMENTED.error());
												}
											}
										}
										else
										{
											errors.add(HttpErrorType.HTTP_METHOD_NOT_IMPLEMENTED.error());
										}
									}
									else
									{
										errors.add(HttpErrorType.SERVICE_NOT_IMPLEMENTED.error());
									}
								}
								else
								{
									errors.add(HttpErrorType.SERVICE_METHOD_MISSING.error());
								}
							}
							else
							{
								methodResponse = endpoint(request, response, serviceMethod.getMethodName());
							}
						}
						else
						{
							errors.add(HttpErrorType.SERVICE_METHOD_NAME_MISSING.error());
						}
					}
					else
					{
						errors.add(HttpErrorType.HTTP_METHOD_NOT_SUPPORTED.error());
					}
				}	
				else
				{
					errors.add(HttpErrorType.HTTP_METHOD_MISSING.error());
				}
			}
			else
			{
				errors.add(HttpErrorType.HTTP_ORIGIN_UNSUPPORTED.error());
			}
		}
		catch(Throwable e)
		{
			if(dev)
			{
				e.printStackTrace();
			}
			try
			{
				exception(request, response, e);
			}
			catch(Throwable e2)
			{
				e2.printStackTrace();
			}
		}
		if(!errors.isEmpty())
		{
			if(methodResponse == null)
			{
				methodResponse = errorResponse(errors);
			}
			else if(methodResponse instanceof HttpServiceResponse)
			{
				((HttpServiceResponse) methodResponse).setErrors(errors);
			}
		}
		if(methodResponse != null)
		{
			int size = 0;
			try(DataOutputStream output = new DataOutputStream(response.getOutputStream()))
			{
				responseMapper.serialize(methodResponse, output);
				size = output.size();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			if(errors.isEmpty())
			{
				Time endTime = new Time();
				int duration = (int) (endTime.toTimestamp() - startTime.toTimestamp());
				logResponse(serviceMethod.getServiceName(), serviceMethod.getMethodName(), methodReflector.isSkipRequest(), methodRequest, requestMapper, request, response, size, duration, startTime, endTime);
			}
		}
		if(dev || (service != null && service.isDebug()))
		{
			String debugResponse = null;
			if(methodReflector != null)
			{
				if(methodReflector.isDebugRequest())
				{
					debugRequest = getDebugRequest(request, methodRequest, requestMapper);
				}
				if(methodReflector.isDebugResponse())
				{
					debugResponse = getDebugResponse(response, methodResponse, responseMapper);
				}
			}
			if(dev)
			{
				System.out.println((debugRequest != null ? debugRequest : "") + (debugResponse != null ? debugResponse : ""));
			}
			if(service != null && service.isDebug() && serviceMethod != null)
			{
				String rawRequest = null;
				if(rawInput != null)
				{
					try
					{
						rawInput.reset();
						rawRequest = IoUtil.toString(rawInput);
					}
					catch(Exception e)
					{
						
					}
				}
				service.debug(serviceMethod.getServiceName(), serviceMethod.getMethodName(), debugRequest, debugResponse, rawRequest);
			}
		}
	}
	
	protected boolean restrictSecurityOrigin()
	{
		return true;
	}
	
	protected String getContentSecurityOrigins(String origin) 
	{
		try
		{
			InternetDomainName idn = InternetDomainName.from(removeProtocol(origin)).topPrivateDomain();
			if(idn != null)
			{
				return DEFAULT_SOURCE_SELF + idn.toString() + " *." + idn.toString() + getDefaultOrigins();
			}
		}
		catch (Exception ex)
		{
			
		}
		return DEFAULT_SOURCE_SELF + removeProtocol(origin) + getDefaultOrigins();
	}
	
	private String removeProtocol(String origin)
	{
		if(origin == null)
		{
			return null;
		}
		return origin.replace("https://", "").replaceAll("[\\r\\n]", "");
	}
	
	public String getDefaultOrigins()
	{
		return "";
	}
	
	public abstract String getSecurityHost(HttpServletRequest request);

	public abstract void exception(HttpServletRequest request, HttpServletResponse response, Throwable e);
	
	protected Object endpoint(HttpServletRequest request, HttpServletResponse response, String methodName)
	{
		return null;
	}
	
	protected HttpServiceResponse errorResponse(List<HttpError> errors)
	{
		return new HttpServiceResponse().setErrors(errors);
	}
	
	protected boolean isDev(HttpServletRequest request, HttpServletResponse response)
	{
		return getLocalHosts(request, response).contains(request.getServerName());
	}
	
	protected List<String> getLocalHosts(HttpServletRequest request, HttpServletResponse response)
	{
		return LOCALHOSTS;
	}
	
	protected boolean isOriginAllowed(HttpServletRequest request, HttpServletResponse response)
	{
		return true;
	}
	
	protected HttpServiceMethod getServiceMethod(HttpServletRequest request, HttpServletResponse response)
	{
		HttpServiceMethod serviceMethod = null;
		Matcher matcher = SERVICE_METHOD_PATTERN.matcher(request.getPathInfo());
		if(matcher.find())
		{
			serviceMethod = new HttpServiceMethod(matcher.group(SERVICE), matcher.group(METHOD));
		}
		return serviceMethod;
	}
	
	protected MethodReflector getMethodReflector(HttpServletRequest request, HttpServletResponse response, HttpServiceMethod serviceMethod)
	{
		return methodReflectorMap.get(serviceMethod);
	}
	
	protected MapperReflector getMapperReflector()
	{
		return mapperReflector;
	}
	
	protected MapperConfig getMapperConfig()
	{
		return mapperConfig;
	}
	
	protected MimeType getRequestContentType(HttpServletRequest request, HttpServletResponse response)
	{
		MimeType contentType = MimeType.fromString(HttpUrl.parseParamMap(request.getQueryString()).get(CONTENT_TYPE_PARAM));
		if(contentType == null)
		{
			String contentTypeHeader = request.getHeader(CONTENT_TYPE_HEADER);
			if(contentTypeHeader != null)
			{
				int index = contentTypeHeader.indexOf(";");
				if(index > -1)
				{
					contentTypeHeader = contentTypeHeader.substring(0, index);
				}
				contentType = MimeType.fromString(contentTypeHeader);
				if(contentType != null)
				{
					switch(contentType)
					{
						case APPLICATION_JSON:
						case APPLICATION_XML:
						case APPLICATION_X_WWW_FORM_URLENCODED:
						case MULTIPART_FORM_DATA:
						{
							break;
						}
						case TEXT_PLAIN:
						default:
						{
							contentType = null;
							break;
						}
					}
				}
			}
		}
		return contentType != null ? contentType : MimeType.APPLICATION_JSON;
	}
	
	protected MimeType getResponseContentType(HttpServletRequest request, HttpServletResponse response)
	{
		MimeType contentType = MimeType.fromString(HttpUrl.parseParamMap(request.getQueryString()).get(ACCEPT_PARAM));
		if(contentType == null)
		{
			String acceptHeader = request.getHeader(ACCEPT_HEADER);
			if(acceptHeader != null)
			{
				int index = acceptHeader.indexOf(";");
				if(index > -1)
				{
					acceptHeader = acceptHeader.substring(0, index);
				}
				List<String> acceptTypes = List.fromArray(acceptHeader.split(","));
				accepts: for(String acceptType : acceptTypes)
				{
					contentType = MimeType.fromString(acceptType);
					if(contentType != null)
					{
						switch(contentType)
						{
							case APPLICATION_JSON:
							case APPLICATION_XML:
							{
								break accepts;
							}
							default:
							{
								contentType = null;
								break;
							}
						}
					}
				}
			}
		}
		return contentType != null ? contentType : MimeType.APPLICATION_JSON;
	}
	
	protected Mapper getRequestMapper(HttpServletRequest request, HttpServletResponse response, MimeType contentType)
	{
		Mapper mapper = null;
		switch(contentType)
		{
			case APPLICATION_XML:
			{
				mapper = getMapperReflector().getMapper(MapperType.XML, getMapperConfig());
				break;
			}
			case APPLICATION_X_WWW_FORM_URLENCODED:
			{
				mapper = getMapperReflector().getMapper(MapperType.FORM, getMapperConfig());
				break;
			}
			case MULTIPART_FORM_DATA:
			{
				mapper = getMapperReflector().getMapper(MapperType.MULTIPART_FORM, getMapperConfig());
				break;
			}
			default:
			{
				mapper = getMapperReflector().getMapper(MapperType.JSON, getMapperConfig());
				break;
			}
		}
		return mapper;
	}
	
	protected Mapper getResponseMapper(HttpServletRequest request, HttpServletResponse response, MimeType contentType)
	{
		Mapper mapper = null;
		switch(contentType)
		{
			case APPLICATION_XML:
			{
				mapper = getMapperReflector().getMapper(MapperType.XML, getMapperConfig());
				break;
			}
			default:
			{
				mapper = getMapperReflector().getMapper(MapperType.JSON, getMapperConfig());
				break;
			}
		}
		return mapper;
	}
	
	protected List<HttpError> validate(HttpServletRequest request, HttpServletResponse response, Object value, MapperType mapperType)
	{
		return validate(request, response, value, mapperType, BLANK);
	}
	
	protected List<HttpError> validate(HttpServletRequest request, HttpServletResponse response, Object value, MapperType mapperType, String path)
	{
		List<HttpError> errors = new List<HttpError>();
		if(value != null)
		{
			EntityReflector entityReflector = getMapperReflector().getEntityReflector(value.getClass());
			if(entityReflector != null)
			{
				for(PropertyReflector propertyReflector : entityReflector.getPropertyReflectors(mapperType))
				{
					try
					{
						Type propertyType = propertyReflector.getFieldType();
						String propertyName = propertyReflector.getPropertyName(mapperType);
						Object propertyValue = propertyReflector.getField().get(value);
						if(getMapperReflector().isEntity(propertyType))
						{
							errors.addAll(validate(request, response, propertyValue, mapperType, path + propertyName + DOT));
						}
						else if(ReflectionUtil.isArray(propertyType) || ReflectionUtil.isCollection(propertyType))
						{
							if(propertyValue != null && !byte[].class.isAssignableFrom(propertyReflector.getFieldClass()))
							{
								int i = 0;
								for(Object element : ReflectionUtil.asCollection(propertyValue))
								{
									errors.addAll(validate(request, response, element, mapperType, path + propertyName + LEFT_BRACE + i + RIGHT_BRACE + DOT));
									i++;
								}
							}
						}
						else if(ReflectionUtil.isMap(propertyType))
						{
							if(propertyValue != null)
							{
								for(Entry<?, ?> elementEntry : ReflectionUtil.asMap(propertyValue).entrySet())
								{
									String elementName = null;
									Object elementKey = elementEntry.getKey();
									if(elementKey != null)
									{
										if(elementKey instanceof Enum)
										{
											elementName = EnumUtil.toString((Enum<?>) elementKey);
										}
										else
										{
											elementName = elementKey.toString();
										}
									}
									if(elementName != null)
									{
										errors.addAll(validate(request, response, elementEntry.getValue(), mapperType, path + propertyName + DOT + elementName + DOT));
									}
								}
							}
						}
						else
						{
							boolean blank = propertyValue == null;
							if(!blank && propertyValue instanceof String)
							{
								blank = ((String) propertyValue).isEmpty();
								if(!blank)
								{
									if(propertyReflector.hasTrimLength())
									{
										int trimLength = propertyReflector.getProperty().trimLength();
										if(((String) propertyValue).length() > trimLength)
										{
											propertyValue = ((String) propertyValue).substring(0, trimLength);
											ReflectionUtil.setFieldValue(propertyReflector.getField(), value, propertyValue);
										}
									}
									else if(propertyReflector.hasMaxLength())
									{
										int length = ((String) propertyValue).length();
										int maxLength = propertyReflector.getProperty().maxLength();
										if(length > maxLength)
										{
											errors.add(HttpErrorType.REQUEST_FIELD_INVALID.error().setContext(path + propertyName).setMessage(String.format(MAX_LENGTH_ERROR, length, maxLength)));
										}
									}
								}
							}
							if(!blank)
							{
								for(Validator validator : propertyReflector.getValidators())
								{
									try
									{
										validator.validate(propertyValue, value, entityReflector);
									}
									catch(ValidatorException e)
									{
										errors.add(HttpErrorType.REQUEST_FIELD_INVALID.error().setContext(path + propertyName).setMessage(e.getMessage()));
										break;
									}
								}
							}
							else if(blank && propertyReflector.isRequired())
							{
								errors.add(HttpErrorType.REQUEST_FIELD_REQUIRED.error().setContext(path + propertyName));
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return errors;
	}
	
	protected String getDebugRequest(HttpServletRequest request, Object methodRequest, Mapper mapper)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(NEW_LINE);
		builder.append(NEW_LINE);
		builder.append("REQUEST");
		builder.append(NEW_LINE);
		builder.append("----------------------------------");
		builder.append(NEW_LINE);
		builder.append(request.getMethod());
		builder.append(SPACE);
		builder.append(request.getRequestURI());
		if(request.getQueryString() != null)
		{
			builder.append(QUESTION);
			builder.append(request.getQueryString());
		}
		
		builder.append(NEW_LINE);
		if(request.getScheme() != null)
		{
			builder.append("PROTOCOL");
			builder.append(": ");
			builder.append(request.getScheme().toUpperCase());
			builder.append(NEW_LINE);
		}
		Enumeration<String> names = request.getHeaderNames();
		while(names.hasMoreElements())
		{
			String name = names.nextElement();
			builder.append(name);
			builder.append(": ");
			builder.append(request.getHeader(name));
			builder.append(NEW_LINE);
		}
		builder.append(NEW_LINE);
		if(methodRequest != null && mapper != null)
		{
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			mapper.setPrettyPrint(true).serialize(methodRequest, output);
			builder.append(output.toString());
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	protected String getDebugResponse(HttpServletResponse response, Object methodResponse, Mapper mapper)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(NEW_LINE);
		builder.append(NEW_LINE);
		builder.append("RESPONSE");
		builder.append(NEW_LINE);
		builder.append("----------------------------------");
		builder.append(NEW_LINE);
		builder.append(response.getStatus());
		builder.append(NEW_LINE);
		for(String name : response.getHeaderNames())
		{
			builder.append(name);
			builder.append(": ");
			builder.append(response.getHeader(name));
			builder.append(NEW_LINE);
		}
		builder.append(NEW_LINE);
		if(methodResponse != null && mapper != null)
		{
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			mapper.setPrettyPrint(true).serialize(methodResponse, output);
			builder.append(output.toString());
			builder.append(NEW_LINE);
			builder.append(NEW_LINE);
		}
		return builder.toString();
	}
	
	public void logRequest(String service, String method, HttpServletRequest request)
	{
		
	}
	
	public void logResponse(String service, String method, boolean skipRequest, Object methodRequest, Mapper requestMapper, HttpServletRequest request, HttpServletResponse response, int size, int duration, Time startTime, Time endTime)
	{
		
	}
	
}
