package roth.lib.java.template;

import java.util.Map;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import roth.lib.java.json.JsonMapper;
import roth.lib.java.mapper.MapperConfig;
import roth.lib.java.util.ResourceUtil;

public class Template
{
	protected static final String ENGINE			= "nashorn";
	protected static final String ROTH_JS			= "roth-lib-js.js";
	protected static final String ROTH_JS_TEMPLATE	= "roth-lib-js-template.js";
	protected static final String VERSION			= "$_version";
	protected static final String CONFIG			= "$_config";
	protected static final String TEMPLATE			= "$_template";
	protected static final String JSON				= "JSON";
	protected static final String PARSE				= "parse";
	protected static final String EVAL				= "eval";
	protected static final String RENDER			= "render";
	protected static final String SET_VERSION		= String.format("var %s = roth.lib.js.template.version;", VERSION);
	protected static final String SET_TEMPLATE		= String.format("var %s = new roth.lib.js.template.Template(" + CONFIG + ");", TEMPLATE);

	protected ScriptEngine engine;
	protected NashornScriptEngineFactory nashornScriptEngineFactory;
	protected String version;
	protected Object template;
	protected Object json;
	protected JsonMapper jsonMapper;

	public Template()
	{
		this(new TemplateConfig());
	}

	public Template(TemplateConfig config)
	{
		init(config);
	}

	protected void init(TemplateConfig config)
	{
		try
		{
			nashornScriptEngineFactory = new NashornScriptEngineFactory();
			engine = nashornScriptEngineFactory.getScriptEngine(ENGINE);
			engine.eval(ResourceUtil.toString(ROTH_JS));
			engine.eval(ResourceUtil.toString(ROTH_JS_TEMPLATE));
			engine.put(CONFIG, config);
			engine.eval(SET_VERSION + SET_TEMPLATE);
			version = (String) engine.get(VERSION);
			template = engine.get(TEMPLATE);
			json = engine.get(JSON);
			jsonMapper = new JsonMapper(new MapperConfig().setSerializeNulls(true));
		}
		catch(ScriptException e)
		{
			throw new TemplateException(e);
		}
	}

	public Template setJsonMapper(JsonMapper jsonMapper)
	{
		this.jsonMapper = jsonMapper;
		return this;
	}

	public void dependency(String source)
	{
		try
		{
			engine.eval(source);
		}
		catch(ScriptException e)
		{
			throw new TemplateException(e);
		}
	}

	public String parse(String source)
	{
		try
		{
			Object parsedSource = ((Invocable) engine).invokeMethod(template, PARSE, source);
			return parsedSource != null ? parsedSource.toString() : "";
		}
		catch(NoSuchMethodException | ScriptException e)
		{
			throw new TemplateException(e);
		}
	}

	protected Object parseJson(String value)
	{
		try
		{
			return ((Invocable) engine).invokeMethod(json, PARSE, value);
		}
		catch(NoSuchMethodException | ScriptException e)
		{
			throw new TemplateException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected String serializeJson(Object scope)
	{
		if(scope instanceof Map)
		{
			return jsonMapper.serialize((Map<String, ?>) scope);
		}
		else
		{
			return jsonMapper.serialize(scope);
		}
	}

	protected Object convertJson(Object scope)
	{
		return parseJson(serializeJson(scope));
	}

	public String eval(String parsedSource, Object scope)
	{
		try
		{
			Object rendered = ((Invocable) engine).invokeMethod(template, EVAL, parsedSource, convertJson(scope));
			return rendered != null ? rendered.toString() : "";
		}
		catch(NoSuchMethodException | ScriptException e)
		{
			throw new TemplateException(e);
		}
	}

	public String render(String source, Object scope)
	{
		try
		{
			Object rendered = ((Invocable) engine).invokeMethod(template, RENDER, source, convertJson(scope));
			return rendered != null ? rendered.toString() : "";
		}
		catch(NoSuchMethodException | ScriptException e)
		{
			throw new TemplateException(e);
		}
	}

	public String getVersion()
	{
		return version;
	}

}
