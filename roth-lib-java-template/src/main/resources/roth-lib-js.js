

var roth = roth || {};
roth.lib = roth.lib || {};
roth.lib.js = roth.lib.js || {};


var isUndefined = isUndefined || function(value)
{
	return value === undefined;
};


var isDefined = isDefined || function(value)
{
	return !isUndefined(value);
};


var isNull = isNull || function(value)
{
	return value === null;
};


var isSet = isSet || function(value)
{
	return !isUndefined(value) && !isNull(value);
};


var isValid = isValid || function(value)
{
	return isSet(value) && value !== "";
};


var isValidString = isValidString || function(value)
{
	return isValid(value) && isString(value);
};


var isInvalid = isInvalid || function(value)
{
	return !isValid(value);
};


var isEmpty = isEmpty || function(value)
{
	var empty = !isSet(value);
	if(!empty)
	{
		if(isString(value))
		{
			empty = !isValidString(value);
		}
		else if(isArray(value))
		{
			empty = value.length == 0;
		}
		else if(isObject(value))
		{
			empty = Object.keys(value).length == 0;
		}
	}
	return empty;
};


var isTrue = isTrue || function(value)
{
	return value === true || value === "true";
};


var isFalse = isFalse || function(value)
{
	return value === false || value === "false";;
};


var isBoolean = isBoolean || function(value)
{
	return typeof value === "boolean";
};


var isNumber = isNumber || function(value)
{
	return !isNaN(value);
};


var isString = isString || function(value)
{
	return typeof value === "string";
};


var isArray = isArray || function(value)
{
	return Array.isArray(value);
};


var isFunction = isFunction || function(value)
{
	return typeof value === "function";
};


var isDate = isDate || function(value)
{
	return value instanceof Date;
};


var isError = isError || function(value)
{
	return value instanceof Error;
};


var isRegExp = isRegExp || function(value)
{
	return value instanceof RegExp;
};


var isObject = isObject || function(value)
{
	return isSet(value) && typeof value === "object";
};


var inArray = inArray || function(value, array)
{
	var contains = false;
	if(isSet(value) && isArray(array))
	{
		contains = array.indexOf(value) > -1;
	}
	return contains;
};


var inMap = inMap || function(value, map)
{
	var array = [];
	for(var key in map)
	{
		array.push(map[key]);
	}
	return inArray(value, array);
};


var forEach = forEach || function(object, callback, thisArg)
{
	if(isFunction(callback))
	{
		if(!isSet(thisArg))
		{
			thisArg = this;
		}
		if(isArray(object))
		{
			for(var i = 0; i < object.length; i++)
			{
				var loop =
				{
					index 	: i,
					length 	: object.length,
					first	: i == 0,
					last	: i == object.length - 1
				};
				if(isFalse(callback.call(thisArg, object[i], i, loop)))
				{
					break;
				}
			}
		}
		else if(isObject(object))
		{
			var keys = Object.keys(object);
			for(var i = 0; i < keys.length; i++)
			{
				var key = keys[i];
				var loop =
				{
					index 	: i,
					length 	: keys.length,
					first	: i == 0,
					last	: i == keys.length - 1
				};
				if(isFalse(callback.call(thisArg, object[key], key, loop)))
				{
					break;
				}
			}
		}
	}
};


var mixin = mixin || function(dest, source)
{
	if(isFunction(dest) && isFunction(source))
	{
		forEach(source.prototype, function(value, name)
		{
			if(!isSet(dest.prototype[name]))
			{
				dest.prototype[name] = value;
			}
		});
	}
};






var CookieUtil = CookieUtil ||
{


	get : function(key)
	{
		return decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*" + encodeURIComponent(key).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=\\s*([^;]*).*$)|^.*$"), "$1")) || null;
	},


	set : function(key, value, end, path, domain, secure)
	{
		if(!key || /^(?:expires|max\-age|path|domain|secure)$/i.test(key)) { return false; }
		var expires = "";
		if(end)
		{
			switch(end.constructor)
			{
				case Number:
					expires = end === Infinity ? "; expires=Fri, 31 Dec 9999 23:59:59 GMT" : "; max-age=" + end;
					break;
				case String:
					expires = "; expires=" + end;
					break;
				case Date:
					expires = "; expires=" + end.toUTCString();
					break;
			}
		}
		document.cookie = encodeURIComponent(key) + "=" + encodeURIComponent(value) + expires + (domain ? "; domain=" + domain : "") + (path ? "; path=" + path : "") + (secure ? "; secure" : "");
		return true;
	},


	remove : function(key, path, domain)
	{
		if(!key || !this.has(key)) { return false; }
		document.cookie = encodeURIComponent(key) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT" + ( domain ? "; domain=" + domain : "") + ( path ? "; path=" + path : "");
		return true;
	},


	has : function(key)
	{
		return (new RegExp("(?:^|;\\s*)" + encodeURIComponent(key).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=")).test(document.cookie);
	}

};






var CurrencyUtil = CurrencyUtil ||
{


	formatInput : function(value)
	{
		return this.format(value, null, null)
	},


	formatText : function(value)
	{
		return this.format(value, "$", ",")
	},


	formatRounded : function(value)
	{
		return this.format(value, "$", ",", true)
	},


	format : function(value, symbol, seperator, round)
	{
		value = parseInt(value);
		if(isNumber(value))
		{
			var decimal = 2;
			var negative = value < 0;
			value = Math.abs(value) / 100;
			if(isTrue(round))
			{
				decimal = 0;
				value = Math.round(value);
			}
			var formattedValue = isValidString(symbol) ? symbol : "";
			if(isValidString(seperator))
			{
				var stringValue = parseFloat(value).toFixed(decimal);
				var formattedValues = [];
				var length = stringValue.length;
				if(decimal > 0)
				{
					for(var i = 1; i <= decimal; i++)
					{
						formattedValues.push(stringValue.charAt(length - i));
					}
					formattedValues.push(".");
					length = length - decimal - 1;
				}
				for(var i = 1; i <= length; i++)
				{
					formattedValues.push(stringValue.charAt(length - i));
					if(i < length && i % 3 == 0)
					{
						formattedValues.push(seperator);
					}
				}
				formattedValue += formattedValues.reverse().join("");
			}
			else
			{
				formattedValue = parseFloat(value).toFixed(decimal);
			}
			if(negative)
			{
				formattedValue = "(" + formattedValue + ")";
			}
			return formattedValue;
		}
		else
		{
			return "";
		}
	},


	parse : function(value)
	{
		var parsedValue = null;
		if(isValidString(value))
		{
			try
			{
				value = parseFloat(value.replace(/[^0-9.]/g, ""));
				if(!isNaN(value))
				{
					parsedValue = Math.round(value * 100);
				}
			}
			catch(e)
			{

			}
		}
		return parsedValue;
	}


};






var DateUtil = DateUtil ||
{


	defaultPattern : "yyyy-MM-dd HH:mm:ss z",


	defaultLang : "en",


	formatRegExp : (function()
	{
		var builder = "";
		builder += "''|'|";
		builder += "yyyy|yy|";
		builder += "MMMM|MMM|MM|M|";
		builder += "dd|d|";
		builder += "EEEE|EEE|u|";
		builder += "HH|H|";
		builder += "kk|k|";
		builder += "KK|K|";
		builder += "hh|h|";
		builder += "mm|m|";
		builder += "ss|s|";
		builder += "SSS|SS|S|";
		builder += "a|";
		builder += "zzz|zz|z|";
		builder += "Z|X|";
		return new RegExp(builder, "g");
	})(),


	label :
	{
		"en" :
		{
			"longMonths" : ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
			"shortMonths" : ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
			"longDays" : ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
			"shortDays" : ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
		},
		"es" :
		{
			"longMonths" : ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"],
			"shortMonths" : ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"],
			"longDays" : ["Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"],
			"shortDays" : ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"]
		}
	},


	get : function(year, month, day, hour, minutes, seconds, milliseconds)
	{
		month = !isNaN(month) ? month - 1 : 0;
		return new Date(year, month, day, hour, minutes, seconds, milliseconds);
	},


	format : function(pattern, date, lang)
	{
		var self = this;
		pattern = isValidString(pattern) ? pattern : this.defaultPattern;
		if(!isNaN(date))
		{
			date = new Date(date);
		}
		else if(!isDate(date))
		{
			date = new Date();
		}
		lang = isSet(this.label[lang]) ? lang : this.defaultLang;
		var escape = false;
		var formattedDate = pattern.replace(this.formatRegExp, function(match, capture)
		{
			switch(match)
			{
				case "''":
				{
					return "'";
				}
				case "'":
				{
					escape = !escape;
					return "";
				}
				default:
				{
					if(escape)
					{
						return match;
					}
				}
			}
			var replacement = "";
			switch(match)
			{
				case "yyyy":
				{
					replacement = new String(date.getFullYear());
					break;
				}
				case "yy":
				{
					replacement = new String(date.getFullYear()).slice(-2);
					break;
				}
				case "MMMM":
				{
					replacement = self.label[lang].longMonths[date.getMonth()];
					break;
				}
				case "MMM":
				{
					replacement = self.label[lang].shortMonths[date.getMonth()];
					break;
				}
				case "MM":
				case "M":
				{
					replacement = StringUtil.padNumber(date.getMonth() + 1, match.length);
					break;
				}
				case "dd":
				case "d":
				{
					replacement = StringUtil.padNumber(date.getDate(), match.length);
					break;
				}
				case "EEEE":
				{
					replacement = self.label[lang].longDays[date.getDay()];
					break;
				}
				case "EEE":
				{
					replacement = self.label[lang].shortDays[date.getDay()];
					break;
				}
				case "u":
				{
					replacement = new String(date.getDay() + 1);
					break;
				}
				case "HH":
				case "H":
				{
					replacement = StringUtil.padNumber(date.getHours(), match.length);
					break;
				}
				case "kk":
				case "k":
				{
					replacement = StringUtil.padNumber(date.getHours() + 1, match.length);
					break;
				}
				case "KK":
				case "K":
				{
					replacement = StringUtil.padNumber(date.getHours() % 12 - 1, match.length);
					break;
				}
				case "hh":
				case "h":
				{
					replacement = StringUtil.padNumber(date.getHours() % 12 || 12, match.length);
					break;
				}
				case "mm":
				case "m":
				{
					replacement = StringUtil.padNumber(date.getMinutes(), match.length);
					break;
				}
				case "ss":
				case "s":
				{
					replacement = StringUtil.padNumber(date.getSeconds(), match.length);
					break;
				}
				case "SSS":
				case "SS":
				case "S":
				{
					replacement = StringUtil.padNumber(date.getMilliseconds(), match.length);
					break;
				}
				case "A":
				{
					replacement = date.getHours() < 12 ? "AM" : "PM";
					break;
				}
				case "a":
				{
					replacement = date.getHours() < 12 ? "am" : "pm";
					break;
				}
				case "zzz":
				case "zz":
				case "z":
				{
					var matcher = /\((\w*)\)/.exec(date.toString());
					if(!isNull(matcher))
					{
						replacement = matcher[1];
					}
					break;
				}
				case "Z":
				case "X":
				{
					var offsetMinutes = date.getTimezoneOffset();
					var sign = offsetMinutes <= 0 ? "+" : "-";
					var offsetHours = Math.abs(offsetMinutes / 60);
					var hours = Math.floor(offsetHours);
					var minutes = Math.round((offsetHours - hours) * 60);
					replacement = sign + StringUtil.padNumber(hours, 2) + StringUtil.padNumber(minutes, 2);
					break;
				}
			}
			return replacement;
		});
		return formattedDate;
	},


	parser : function(pattern, lang)
	{
		var self = this;
		pattern = isValidString(pattern) ? pattern : this.defaultPattern;
		lang = isSet(this.label[lang]) ? lang : this.defaultLang;
		var groups = [];
		var escape = false;
		var builder = pattern.replace(this.formatRegExp, function(match, capture)
		{
			switch(match)
			{
				case "''":
				{
					return "'";
				}
				case "'":
				{
					escape = !escape;
					return "";
				}
				default:
				{
					if(escape)
					{
						return match;
					}
				}
			}
			var replacement = "";
			switch(match)
			{
				case "yyyy":
				{
					replacement = "([0-9]{4})";
					break;
				}
				case "yy":
				{
					replacement = "([0-9]{2})";
					break;
				}
				case "MMMM":
				{
					replacement = "(" + self.label[lang].longMonths.join("|") + ")";
					break;
				}
				case "MMM":
				{
					replacement = "(" + self.label[lang].shortMonths.join("|") + ")";
					break;
				}
				case "MM":
				{
					replacement = "([0][1-9]|[1][0-2])";
					break;
				}
				case "M":
				{
					replacement = "([0][1-9]|[1][0-2]|[1-9])";
					break;
				}
				case "dd":
				{
					replacement = "([0][1-9]|[1-2][0-9]|[3][0-1])";
					break;
				}
				case "d":
				{
					replacement = "([0][1-9]|[1-2][0-9]|[3][0-1]|[1-9])";
					break;
				}
				case "EEEE":
				{
					replacement = "(" + self.label[lang].longDays.join("|") + ")";
					break;
				}
				case "EEE":
				{
					replacement = "(" + self.label[lang].shortDays.join("|") + ")";
					break;
				}
				case "u":
				{
					replacement = "([1-7])";
					break;
				}
				case "HH":
				{
					replacement = "([0-1][0-9]|[2][0-3])";
					break;
				}
				case "H":
				{
					replacement = "([0-1][0-9]|[2][0-3]|[0-9])";
					break;
				}
				case "kk":
				{
					replacement = "([0][1-9]|[1][0-9]|[2][0-4])";
					break;
				}
				case "k":
				{
					replacement = "([0][1-9]|[1][0-9]|[2][0-4]|[0-9])";
					break;
				}
				case "KK":
				{
					replacement = "([0][0-9]|[1][0-1])";
					break;
				}
				case "K":
				{
					replacement = "([0][0-9]|[1][0-1]|[0-9])";
					break;
				}
				case "hh":
				{
					replacement = "([0][1-9]|[0-1][0-2])";
					break;
				}
				case "h":
				{
					replacement = "([0][1-9]|[0-1][0-2]|[1-9])";
					break;
				}
				case "mm":
				{
					replacement = "([0-5][0-9])";
					break;
				}
				case "m":
				{
					replacement = "([0-5][0-9]|[0-9])";
					break;
				}
				case "ss":
				{
					replacement = "([0-5][0-9])";
					break;
				}
				case "s":
				{
					replacement = "([0-5][0-9]|[0-9])";
					break;
				}
				case "SSS":
				{
					replacement = "([0-9]{3})";
					break;
				}
				case "SS":
				{
					replacement = "([0-9]{2,3})";
					break;
				}
				case "S":
				{
					replacement = "([0-9]{1,3})";
					break;
				}
				case "a":
				{
					replacement = "(AM|PM)";
					break;
				}
				default:
				{
					match = null;
					break;
				}
			}
			if(match)
			{
				groups.push(match)
			}
			return replacement;
		});
		return {
			regExp : new RegExp(builder, "i"),
			groups : groups
		}
	},


	isValid : function(pattern, value, lang)
	{
		var parser = this.parser(pattern, lang);
		return parser.regExp.test(value);
	},


	parse : function(pattern, value, lang)
	{
		var self = this;
		lang = isSet(this.label[lang]) ? lang : this.defaultLang;
		var parser = this.parser(pattern, lang);
		var date = null;
		var matcher = parser.regExp.exec(value);
		if(matcher)
		{
			var defaultDate = new Date();
			var year = defaultDate.getYear();
			var month = defaultDate.getMonth();
			var day = 1;
			var hours = 0;
			var minutes = 0;
			var seconds = 0;
			var milliseconds = 0;
			var pm = false;
			for(var i in parser.groups)
			{
				var group = parser.groups[i];
				var capture = matcher[new Number(i) + 1];
				switch(group)
				{
					case "yyyy":
					{
						year = new Number(capture);
						break;
					}
					case "yy":
					{
						year = new Number(capture) + 2000;
						break;
					}
					case "MMMM":
					{
						capture = capture.charAt(0).toUpperCase() + capture.slice(1).toLowerCase();
						var index = self.label[lang].longMonths.indexOf(capture);
						if(index > -1)
						{
							month = index;
						}
						break;
					}
					case "MMM":
					{
						capture = capture.charAt(0).toUpperCase() + capture.slice(1).toLowerCase();
						var index = self.label[lang].shortMonths.indexOf(capture);
						if(index > -1)
						{
							month = index;
						}
						break;
					}
					case "MM":
					case "M":
					{
						month = new Number(capture) - 1;
						break;
					}
					case "dd":
					case "d":
					{
						day = new Number(capture);
						break;
					}
					case "EEEE":
					case "EEE":
					case "u":
					{
						// ignore weekday
						break;
					}
					case "HH":
					case "H":
					{
						hours = new Number(capture);
						break;
					}
					case "kk":
					case "k":
					{
						hours = new Number(capture) - 1;
						break;
					}
					case "KK":
					case "K":
					{
						hours = new Number(capture) + 1;
						break;
					}
					case "hh":
					case "h":
					{
						hours = new Number(capture);
						break;
					}
					case "mm":
					case "m":
					{
						minutes = new Number(capture);
						break;
					}
					case "ss":
					case "s":
					{
						seconds = new Number(capture);
						break;
					}
					case "SSS":
					case "SS":
					case "S":
					{
						milliseconds = new Number(capture);
						break;
					}
					case "a":
					{
						pm = "PM" == capture.toUpperCase();
						break;
					}
				}
			}
			if(pm)
			{
				hours += 12;
				if(hours == 24)
				{
					hours = 0;
				}
			}
			date = new Date(year, month, day, hours, minutes, seconds, milliseconds);
		}
		return date;
	},


	reformat : function(parsePattern, formatPattern, value, lang)
	{
		var date = this.parse(parsePattern, value, lang);
		if(isSet(date))
		{
			value = this.format(formatPattern, date, lang);
		}
		return value;
	},


	equals : function(date1, date2)
	{
		return date1 >= date2 && date1 <= date2;
	},


	day : function(date)
	{
		if(!isDate(date))
		{
			date = new Date();
		}
		return new Date(date.getFullYear(), date.getMonth(), date.getDate());
	},


	month : function(date)
	{
		if(!isDate(date))
		{
			date = new Date();
		}
		return new Date(date.getFullYear(), date.getMonth(), 1);
	},


	year : function(date)
	{
		if(!isDate(date))
		{
			date = new Date();
		}
		return new Date(date.getFullYear(), 0, 1);
	}


};






var IdUtil = IdUtil ||
{


	defaultLength : 10,


	defaultKey : "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",


	generate : function(length, key)
	{
		length = isNumber(length) ? length : this.defaultLength;
		key = isValidString(key) ? key : this.defaultKey;
		var value = "";
		for(var i = 0; i < length; i++)
		{
			value += key.charAt(Math.floor(Math.random() * key.length));
		}
		return value;
	}

};






var NumberUtil = NumberUtil ||
{


	formatInt : function(value)
	{
		var parsedValue = parseInt(value);
		return !isNaN(parsedValue) ? parsedValue.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") : "";
	},


	formatDecimal : function(value)
	{
		var parsedValue = parseFloat(value);
		return !isNaN(parsedValue) ? parsedValue.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",") : "";
	},


	formatPercent : function(value, decimal)
	{
		decimal = isNumber(decimal) ? decimal : 0;
		var parsedValue = parseFloat(value);
		return !isNaN(parsedValue) ? (parsedValue * 100).toFixed(decimal) : "";
	}

};






var ObjectUtil = ObjectUtil ||
{


	parse : function(value)
	{
		var object = null;
		try
		{
			if(isObject(value))
			{
				object = value;
			}
			else
			{
				eval("object = " + value);
			}
		}
		catch(e)
		{

		}
		return isObject(object) ? object : {};
	},


	find : function(object, path)
	{
		var paths = path.split(".");
		for(var i in paths)
		{
			if(isSet(object[paths[i]]))
			{
				object = object[paths[i]];
			}
			else
			{
				object = null;
				break;
			}
		}
		return object;
	},


	equals : function(object1, object2)
	{
		var equals = false;
		if(!isSet(object1) && !isSet(object2))
		{
			equals = true;
		}
		else if(isArray(object1) && isArray(object2))
		{
			equals = JSON.stringify(object1) == JSON.stringify(object2);
		}
		else if(isObject(object1) && isObject(object2))
		{
			equals = JSON.stringify(object1) == JSON.stringify(object2);
		}
		else
		{
			equals = object1 == object2;
		}
		return equals;
	}


};





var StringUtil = StringUtil ||
{


	padNumber : function(value, length)
	{
		return this.pad(new String(value), length, "0", true);
	},


	padLeft : function(value, length, character)
	{
		return this.pad(value, length, character, true);
	},


	padRight : function(value, length, character)
	{
		return this.pad(value, length, character, false);
	},


	pad : function(value, length, character, left)
	{
		if(!isSet(value))
		{
			value = "";
		}
		if(value.length < length)
		{
			character = isValidString(character) ? character.substring(0, 1) : " ";
			var pad = new Array(length + 1 - value.length).join(character);
			return left ? pad + value : value + pad;
		}
		else
		{
			return value;
		}
	},


	repeat : function(value, length)
	{
		var repeated = "";
		for(var i = 0; i < length; i++)
		{
			repeated += value;
		}
		return repeated;
	},


	equals : function(value1, value2, caseInsensitive)
	{
		caseInsensitive = isSet(caseInsensitive) ? caseInsensitive : true;
		var equals = false;
		if(value1 && value2)
		{
			equals = new String(value1).match(new RegExp("^" + new String(value2) + "$", caseInsensitive ? "i" : "")) !== null;
		}
		return equals;
	},


	capitalize : function(value)
	{
		return isValidString(value) ? value.charAt(0).toUpperCase() + value.slice(1) : "";
	},


	camelCase : function(value)
	{
		value = this.capitalize(value);
		value = value.replace(/[^a-zA-Z0-9]+([a-zA-Z0-9]{1})/g, function(match, capture)
		{
			var replacement = "";
			if(isValidString(capture))
			{
				replacement = capture.charAt(0).toUpperCase();
			}
			return replacement;
		});
		return value;
	},


	stripHtml : function(value)
	{
		return value.replace(/<\/?[^>]+(>|$)/g, "");
	},


	startsWith : function(value, pattern)
	{
		return isSet(value) ? new RegExp("^" + pattern).test(value) : false;
	}

};



roth.lib.js.version = "3.0.1-SNAPSHOT";
