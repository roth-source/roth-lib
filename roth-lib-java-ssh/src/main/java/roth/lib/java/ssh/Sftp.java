package roth.lib.java.ssh;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import roth.lib.java.lang.List;
import roth.lib.java.util.IoUtil;

public class Sftp implements AutoCloseable
{
	protected static String SFTP = "sftp";
	
	protected Ssh ssh;
	protected ChannelSftp channelSftp;
	
	public Sftp(Ssh ssh)
	{
		this.ssh = ssh;
	}
	
	public ChannelSftp openChannelSftp()
	{
		try
		{
			ssh.connect();
			if(channelSftp == null || channelSftp.isClosed())
			{
				channelSftp = (ChannelSftp) ssh.session.openChannel(SFTP);
				channelSftp.connect();
			}
		}
		catch(JSchException e)
		{
			throw new SshException(e);
		}
		return channelSftp;
	}
	
	public List<String> ls()
	{
		return ls(".");
	}
	
	public List<String> ls(String path)
	{
		List<String> lines = new List<String>();
		try
		{
			openChannelSftp();
			Vector<?> vector = channelSftp.ls(path);
			for(Object object : vector)
			{
				if(object instanceof LsEntry)
				{
					lines.add(((LsEntry) object).getFilename());
				}
			}
		}
		catch(SftpException e)
		{
			throw new SshException(e);
		}
		return lines;
	}
	
	public List<SftpFile> lsWithType()
	{
		return lsWithType(".");
	}
	
	public List<SftpFile> lsWithType(String path)
	{
		List<SftpFile> lines = new List<SftpFile>();
		try
		{
			openChannelSftp();
			Vector<?> vector = channelSftp.ls(path);
			for(Object object : vector)
			{
				if(object instanceof LsEntry)
				{
					LsEntry lsE = (LsEntry) object;
					SftpFile sfFile = new SftpFile(lsE.getFilename(), lsE.getAttrs() == null ? false : lsE.getAttrs().isDir());
					lines.add(sfFile);
				}
			}
		}
		catch(SftpException e)
		{
			throw new SshException(e);
		}
		return lines;
	}
	
	public void cd(String path)
	{
		try
		{
			openChannelSftp();
			channelSftp.cd(path);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void put(File source, String dest)
	{
		put(source, dest, null);
	}
	
	public void put(File source, String dest, String filename)
	{
		try(FileInputStream input = new FileInputStream(source);)
		{
			put(input, dest + "/" + (filename != null ? filename : source.getName()));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void put(byte[] bytes, String dest)
	{
		put(new ByteArrayInputStream(bytes), dest);
	}
	
	public void put(InputStream input, String dest)
	{
		try
		{
			openChannelSftp();
			channelSftp.put(input, dest);
		}
		catch(SftpException e)
		{
			throw new SshException(e);
		}
	}
	
	public InputStream get(String src)
	{
		try
		{
			openChannelSftp();
			return channelSftp.get(src);
		}
		catch(SftpException e)
		{
			throw new SshException(e);
		}
	}
	
	public byte[] getBytes(String src)
	{
		try
		{
			return IoUtil.toBytes(get(src));
		}
		catch(IOException e)
		{
			throw new SshException(e);
		}
	}
	
	public String getString(String src)
	{
		try
		{
			return IoUtil.toString(get(src));
		}
		catch(IOException e)
		{
			throw new SshException(e);
		}
	}
	
	public void get(String src, OutputStream output)
	{
		try
		{
			IoUtil.copy(get(src), output);
		}
		catch(IOException e)
		{
			throw new SshException(e);
		}
	}
	
	public void get(String src, File file)
	{
		try(FileOutputStream output = new FileOutputStream(file))
		{
			get(src, output);
		}
		catch(IOException e)
		{
			throw new SshException(e);
		}
	}
	
	public void mv(String src, String dest)
	{
		try
		{
			openChannelSftp();
			channelSftp.rename(src, dest);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void rm(String path)
	{
		try
		{
			openChannelSftp();
			channelSftp.rm(path);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void rmdir(String path)
	{
		try
		{
			openChannelSftp();
			channelSftp.rmdir(path);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void closeChannelSftp()
	{
		if(channelSftp != null)
		{
			channelSftp.disconnect();
		}
	}
	
	@Override
	public void close()
	{
		closeChannelSftp();
	}
	
}
