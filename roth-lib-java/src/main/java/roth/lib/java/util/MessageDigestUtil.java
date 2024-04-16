package roth.lib.java.util;

import java.security.MessageDigest;

public class MessageDigestUtil
{
	public static final String MD5 			= "MD5";
	public static final String SHA_1 		= "SHA-1";
	public static final String SHA_256 		= "SHA-256";
	
	protected MessageDigestUtil()
	{
		
	}
	
	@Deprecated
	public static byte[] digestMd5(String value)
	{
		return digest(value.getBytes(), MD5);
	}
	
	@Deprecated
	public static byte[] digestMd5(byte[] bytes)
	{
		return digest(bytes, MD5);
	}
	
	public static byte[] digestSha1(String value)
	{
		return digest(value.getBytes(), SHA_1);
	}
	
	public static byte[] digestSha1(byte[] bytes)
	{
		return digest(bytes, SHA_1);
	}
	
	public static byte[] digestSha256(String value)
	{
		return digest(value.getBytes(), SHA_256);
	}
	
	public static byte[] digestSha256(byte[] bytes)
	{
		return digest(bytes, SHA_256);
	}
	
	public static byte[] digest(byte[] bytes, String algorithm)
	{
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.update(bytes);
			return messageDigest.digest();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static String digestMd5Base16(String value)
	{
		return StringUtil.padLeftLimit(digest(value, MD5, BaseUtil.BASE_16), 32, '0');
	}
	
	public static String digestMd5Base36(String value)
	{
		return digest(value, MD5, BaseUtil.BASE_36);
	}
	
	public static String digestMd5Base62(String value)
	{
		return digest(value, MD5, BaseUtil.BASE_62);
	}
	
	public static String digestMd5Base64(String value)
	{
		return digest(value, MD5, BaseUtil.BASE_64);
	}
	
	public static String digestSha1Base16(String value)
	{
		return digest(value, SHA_1, BaseUtil.BASE_16);
	}
	
	public static String digestSha1Base36(String value)
	{
		return digest(value, SHA_1, BaseUtil.BASE_36);
	}
	
	public static String digestSha1Base62(String value)
	{
		return digest(value, SHA_1, BaseUtil.BASE_62);
	}
	
	public static String digestSha1Base64(String value)
	{
		return digest(value, SHA_1, BaseUtil.BASE_64);
	}
	
	public static String digestSha256Base16(String value)
	{
		return digest(value, SHA_256, BaseUtil.BASE_16);
	}
	
	public static String digestSha256Base36(String value)
	{
		return digest(value, SHA_256, BaseUtil.BASE_36);
	}
	
	public static String digestSha256Base62(String value)
	{
		return digest(value, SHA_256, BaseUtil.BASE_62);
	}
	
	public static String digestSha256Base64(String value)
	{
		return digest(value, SHA_256, BaseUtil.BASE_64);
	}
	
	public static String digest(String value, String algorithm, int base)
	{
		return digest(value.getBytes(), algorithm, base);
	}
	
	public static String digestMd5Base16(byte[] bytes)
	{
		return digest(bytes, MD5, BaseUtil.BASE_16);
	}
	
	public static String digestMd5Base36(byte[] bytes)
	{
		return digest(bytes, MD5, BaseUtil.BASE_36);
	}
	
	public static String digestMd5Base62(byte[] bytes)
	{
		return digest(bytes, MD5, BaseUtil.BASE_62);
	}
	
	public static String digestMd5Base64(byte[] bytes)
	{
		return digest(bytes, MD5, BaseUtil.BASE_64);
	}
	
	public static String digestSha1Base16(byte[] bytes)
	{
		return digest(bytes, SHA_1, BaseUtil.BASE_16);
	}
	
	public static String digestSha1Base36(byte[] bytes)
	{
		return digest(bytes, SHA_1, BaseUtil.BASE_36);
	}
	
	public static String digestSha1Base62(byte[] bytes)
	{
		return digest(bytes, SHA_1, BaseUtil.BASE_62);
	}
	
	public static String digestSha1Base64(byte[] bytes)
	{
		return digest(bytes, SHA_1, BaseUtil.BASE_64);
	}
	
	public static String digestSha256Base16(byte[] bytes)
	{
		return digest(bytes, SHA_256, BaseUtil.BASE_16);
	}
	
	public static String digestSha256Base36(byte[] bytes)
	{
		return digest(bytes, SHA_256, BaseUtil.BASE_36);
	}
	
	public static String digestSha256Base62(byte[] bytes)
	{
		return digest(bytes, SHA_256, BaseUtil.BASE_62);
	}
	
	public static String digestSha256Base64(byte[] bytes)
	{
		return digest(bytes, SHA_256, BaseUtil.BASE_64);
	}
	
	public static String digest(byte[] bytes, String algorithm, int base)
	{
		return BaseUtil.encode(digest(bytes, algorithm), base);
	}
	
}
