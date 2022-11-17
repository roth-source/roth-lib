package roth.lib.java.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil
{
	protected static int BUFFER_SIZE = 1024 * 4;
	
	protected ZipUtil()
	{
		
	}
	
	public static File extract(File file) throws Exception
	{
		return extract(file, file.getParentFile());
	}
	
	public static File extract(File file, File parentDir) throws Exception
	{
		return extract(new File(getFileSystemPath(file.getPath())), new File(getFileSystemPath(parentDir.getPath())), getFileSystemPath(file.getName()).replaceFirst("\\.[A-Za-z]+?$", "") + "/");
	}
	
	private static File extract(File file, File parentDir, String dirname)
	{
		File dir = new File(parentDir, dirname);
		try(ZipFile zipFile = new ZipFile(file))
		{
			delete(dir);
			dir.mkdirs();
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements())
			{
				ZipEntry zipEntry = zipEntries.nextElement();
				File entryFile = new File(dir, zipEntry.getName());
				if(zipEntry.isDirectory())
				{
					entryFile.mkdirs();
				}
				else
				{
					entryFile.getParentFile().mkdirs();
					int count = 0;
					byte[] buffer = new byte[BUFFER_SIZE];
					try(InputStream input = zipFile.getInputStream(zipEntry); FileOutputStream output = new FileOutputStream(entryFile);)
					{
						while((count = input.read(buffer, 0, BUFFER_SIZE)) != -1)
						{
							output.write(buffer, 0, count);
						}
						output.flush();
					}
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return dir;
	}
	
	public static String getFileSystemPath(String urlPath) throws Exception {

		urlPath = urlPath.replace("%2e", ".");
		urlPath = urlPath.replace("%2f", "/");
		urlPath = urlPath.replace("%5c", "/");

		Path normalizedPath = Paths.get(urlPath).normalize();
		return normalizedPath.toString();
	}
	
	protected static void delete(File dir)
	{
		if(dir.exists() && dir.isDirectory())
		{
			for(File file : dir.listFiles())
			{
				if(file.isDirectory())
				{
					delete(file);
				}
				file.delete();
			}
		}
	}
	
	public static void main(String[]  args)
	{
		try
		{
			extract(new File("war/test-war-1.war"), new File("/Users/User/Downloads/"));
			extract(new File("war/test-war-2.war"), new File("/Users/User/Downloads/"));
		}
		catch(Exception ex)
		{
			
		}
	}
	
}
