

var roth = roth || {};
roth.lib = roth.lib || {};
roth.lib.js = roth.lib.js || {};
roth.lib.js.env = roth.lib.js.env || {};


roth.lib.js.env.hosts = roth.lib.js.env.hosts || { local : ["localhost", "127.0.0.1"] };
roth.lib.js.env.environment = roth.lib.js.env.environment || null;
roth.lib.js.env.mobile = roth.lib.js.env.mobile || null;
roth.lib.js.env.debug = roth.lib.js.env.debug || null;
roth.lib.js.env.print = roth.lib.js.env.print || null;
roth.lib.js.env.compiled = roth.lib.js.env.compiled || null;
roth.lib.js.env.cssCompiled = roth.lib.js.env.cssCompiled || false;
roth.lib.js.env.context = roth.lib.js.env.context || null;
roth.lib.js.env.mockDemo = roth.lib.js.env.mockDemo || false;
roth.lib.js.env.dependencies = roth.lib.js.env.dependencies || [];


var Protocol = Protocol ||
{
	FILE		: "file:",
	DATA		: "data:",
	HTTP		: "http:",
	HTTPS		: "https:"
};


var Environment = Environment ||
{
	MOCK		: "mock",
	LOCAL		: "local",
	TEST		: "test",
	DEMO		: "demo",
	PROD		: "prod"
};


var setHosts = setHosts || function(environment, hosts)
{
	roth.lib.js.env.hosts[environment] = hosts;
};


var setEnvironment = setEnvironment || function(environment)
{
	roth.lib.js.env.environment = environment;
};


var setMockDemo = setMockDemo || function()
{
	roth.lib.js.env.mockDemo = true;
};


var setMock = setMock || function()
{
	setEnvironment(Environment.MOCK);
};


var setLocal = setLocal || function()
{
	setEnvironment(Environment.LOCAL);
};


var setTest = setTest || function(context)
{
	setEnvironment(Environment.TEST);
	setContext(context);
};


var setDemo = setDemo || function(context)
{
	setEnvironment(Environment.DEMO);
	setContext(context);
};


var setProd = setProd || function(context)
{
	setEnvironment(Environment.PROD);
	setContext(context);
};


var setMobile = setMobile || function(mobile)
{
	roth.lib.js.env.mobile = mobile !== false ? true : false;
};


var setDebug = setDebug || function(debug)
{
	roth.lib.js.env.debug = debug !== false ? true : false;
};


var setPrint = setPrint || function(print)
{
	roth.lib.js.env.print = print !== false ? true : false;
};


var setCompiled = setCompiled || function(compiled)
{
	roth.lib.js.env.compiled = compiled !== false ? true : false;
};


var setCssCompiled = setCssCompiled || function(cssCompiled)
{
	roth.lib.js.env.cssCompiled = cssCompiled !== false ? true : false;
};


var setContext = setContext || function(context)
{
	if(context !== undefined)
	{
		roth.lib.js.env.context = context;
	}
};


var setDependencies = setDependencies || function(dependencies)
{
	roth.lib.js.env.dependencies = dependencies;
};


var isFileProtocol = isFileProtocol || function()
{
	return Protocol.FILE == window.location.protocol;
};


var isDataProtocol = isDataProtocol || function()
{
	return Protocol.DATA == window.location.protocol;
};


var isHttpProtocol = isHttpProtocol || function()
{
	return Protocol.HTTP == window.location.protocol;
};


var isHttpsProtocol = isHttpsProtocol || function()
{
	return Protocol.HTTPS == window.location.protocol;
};


var isHyperTextProtocol = isHyperTextProtocol || function()
{
	return isHttpProtocol() || isHttpsProtocol();
};


var hasHost = hasHost || function()
{
	var host = false;
	var hostname = window.location.hostname.toLowerCase();
	for(var environment in roth.lib.js.env.hosts)
	{
		if(Array.isArray(roth.lib.js.env.hosts[environment]))
		{
			if(roth.lib.js.env.hosts[environment].indexOf(hostname) != -1)
			{
				host = true;
				break;
			}
		}
	}
	return host;
};


var getEnvironment = getEnvironment || function()
{
	if(roth.lib.js.env.environment == null)
	{
		if(isHyperTextProtocol())
		{
			var host = window.location.hostname.toLowerCase();
			for(var environment in roth.lib.js.env.hosts)
			{
				if(Array.isArray(roth.lib.js.env.hosts[environment]))
				{
					if(roth.lib.js.env.hosts[environment].indexOf(host) != -1)
					{
						roth.lib.js.env.environment = environment;
						break;
					}
				}
			}
			if(roth.lib.js.env.environment == null)
			{
				roth.lib.js.env.environment = Environment.PROD;
			}
		}
		else
		{
			roth.lib.js.env.environment = Environment.MOCK;
		}
	}
	return roth.lib.js.env.environment;
};


var isEnvironment = isEnvironment || function(environment)
{
	return getEnvironment() == environment;
};


var isMockDemo = isMockDemo || function()
{
	return roth.lib.js.env.mockDemo;
};


var isMockFile = isMockFile || function()
{
	return isMock() || isFileProtocol();
};


var isDevFile = isDevFile || function()
{
	return isDev() || isFileProtocol();
};


var isMock = isMock || function()
{
	return isEnvironment(Environment.MOCK);
};


var isLocal = isLocal || function()
{
	return isEnvironment(Environment.LOCAL);
};


var isDev = isDev || function()
{
	return isMock() || isLocal();
};


var isTest = isTest || function()
{
	return isEnvironment(Environment.TEST);
};


var isDemo = isDemo || function()
{
	return isEnvironment(Environment.DEMO);
};


var isProd = isProd || function()
{
	return isEnvironment(Environment.PROD);
};


var isMobile = function()
{
	if(roth.lib.js.env.mobile == null)
	{
		roth.lib.js.env.mobile = isAndroidMobile() || isBlackBerryMobile() || isIosMobile() || isOperaMobile() || isWindowsMobile();
	}
	return roth.lib.js.env.mobile;
};


var isAndroidMobile = function()
{
	return navigator.userAgent.match(/Android/i);
};


var isBlackBerryMobile = function()
{
	return navigator.userAgent.match(/BlackBerry/i);
};


var isIosMobile = function()
{
	return navigator.userAgent.match(/iPhone|iPad|iPod/i);
};


var isOperaMobile = function()
{
	return navigator.userAgent.match(/Opera Mini/i);
};


var isWindowsMobile = function()
{
	return navigator.userAgent.match(/IEMobile/i);
};


var isDebug = isDebug || function()
{
	if(roth.lib.js.env.debug == null)
	{
		if(isMockFile())
		{
			setDebug(true);
		}
		else
		{
			setDebug(window.location.search.search(/[?&]debug/i) != -1);
		}
	}
	return roth.lib.js.env.debug;
};


var isPrint = isPrint || function()
{
	if(roth.lib.js.env.print == null)
	{
		setPrint(window.location.search.search(/[?&]print/i) != -1);
	}
	return roth.lib.js.env.print;
};


var isCompiled = isCompiled || function()
{
	if(roth.lib.js.env.compiled == null)
	{
		roth.lib.js.env.compiled = !isDevFile();
	}
	return roth.lib.js.env.compiled;
};


var getContext = getContext || function()
{
	return roth.lib.js.env.context;
};


var checkSecure = checkSecure || function()
{
	if(!isDev() && isHttpProtocol())
	{
		location.protocol = "https:";
	}
};

var readMobileParams = readMobileParams || function()
{
	if(urlParam("isMobile") === "true")
	{
		var session = urlParam("session");
		var token = urlParam("csrfToken");
		if(isSet(session) && isSet(token))
		{
			localStorage.setItem("session", session);
			localStorage.setItem("csrfToken", token);
		}
	}
};

var urlParam = function(name){
	var url = new URL(window.location.href);
	var results= url.searchParams.get(name);
	if(!isNull(results))
	{
		return results;
	}
	else
	{
		return false;
	}
};

var loadCssCompiledDependencies = loadCssCompiledDependencies || function(app)
{
	loadDependencies(app, roth.lib.js.env.cssCompiled);
};


var loadDependencies = loadDependencies || function(app, cssCompiled)
{
	var styleRegExp = new RegExp("\\.css$", "i");
	var scriptRegExp = new RegExp("\\.js$", "i");
	for(var i in roth.lib.js.env.dependencies)
	{
		var dependency = roth.lib.js.env.dependencies[i];
		if(dependency && (!dependency.app || dependency.app == app))
		{
			if(!((dependency.exclude === true) || (!isDev()  && dependency.dev === true) || (isDev() && dependency.dev === false)))
			{
				if((cssCompiled === undefined && dependency.cssCompiled === undefined) || (cssCompiled === true && dependency.cssCompiled === true) || (cssCompiled === false && dependency.cssCompiled === false))
				{
					if(dependency.print === undefined || (isPrint() && dependency.print === true) || (!isPrint() && dependency.print === false))
					{
						var local = dependency.local;
						var external = dependency.external;
						var path = local;
						if((!local && isDev()) || (external && !isDev()))
						{
							path = external;
						}
						if(path)
						{
							var tag = dependency.tag;
							if((tag && tag.toLowerCase() == "link") || styleRegExp.test(path))
							{
								loadLink(path, dependency.attributeMap);
							}
							else if((tag && tag.toLowerCase() == "script") || scriptRegExp.test(path))
							{
								loadScript(path, dependency.attributeMap);
							}
						}
					}
				}
			}
		}
	}
};


var getDependenciesString = getDependenciesString || function()
{
	return JSON.stringify(roth.lib.js.env.dependencies);
};



roth.lib.js.env.version = "3.0.0-SNAPSHOT";
