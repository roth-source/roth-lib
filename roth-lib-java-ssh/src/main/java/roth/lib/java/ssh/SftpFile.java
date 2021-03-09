package roth.lib.java.ssh;

public class SftpFile 
{
	protected String fileName;
	protected boolean directory;
	
	public SftpFile(String name, boolean dir)
	{
		fileName = name;
		directory = dir;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public void setFileName(String f)
	{
		fileName = f;
	}
	
	public boolean isFile()
	{
		return !directory;
	}
	
	public boolean isDirectory()
	{
		return directory;
	}
}
