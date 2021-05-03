package roth.lib.java.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Hashtable;
import roth.lib.java.lang.List;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import roth.lib.java.util.IoUtil;

public class Ssh implements AutoCloseable
{
	protected static String EXEC = "exec";
	
	protected SshConfig config;
	protected JSch jsch;
	protected Session session;
	protected ChannelExec channelExec;
	
	public Ssh(String host)
	{
		this(new SshConfig(host));
	}
	
	public Ssh(SshConfig config)
	{
		this.config = config;
		jsch = new JSch();
		try
		{
			if(!config.hasPassword())
			{
				jsch.addIdentity(config.getKey());
			}
			openChannelExec();
		}
		catch(JSchException e)
		{
			throw new SshException(e);
		}
	}
	
	public Ssh connect()
	{
		if(session == null || !session.isConnected())
		{
			try
			{
				session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
				if(config.hasPassword())
				{
					session.setPassword(config.getPassword());
				}
				session.setConfig(new Hashtable<String, String>(config.getConfig()));
				if(this.config != null && this.config.getTimeout() > 0)
				{
					session.setTimeout(this.config.getTimeout());
				}
				session.connect();
			}
			catch(JSchException e)
			{
				throw new SshException(e);
			}
		}
		return this;
	}
	
	public JSch getJSch()
	{
		return jsch;
	}
	
	public Session getSession()
	{
		return session;
	}
	
	public ChannelExec getChannelExec()
	{
		return channelExec;
	}
	
	public Sftp sftp()
	{
		return new Sftp(this);
	}
	
	public ChannelExec openChannelExec()
	{
		connect();
		if(channelExec == null || channelExec.isClosed())
		{
			try
			{
				channelExec = (ChannelExec) session.openChannel(EXEC);
			}
			catch(JSchException e)
			{
				throw new SshException(e);
			}
		}
		return channelExec;
	}
	
	public List<String> exec(String command)
	{
		List<String> lines = new List<String>();
		try
		{
			openChannelExec();
			channelExec.setCommand(command);
			channelExec.setPty(true);
			InputStream input = channelExec.getInputStream();
			channelExec.connect();
			String line = null;
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(input));)
			{
				while((line = reader.readLine()) != null)
				{
					lines.add(line);
				}
			}
			int code = channelExec.getExitStatus();
			if(code > 0)
			{
				throw new SshExecException(code, command, lines);
			}
		}
		catch(JSchException | IOException e)
		{
			throw new SshException(e);
		}
		finally
		{
			closeChannelExec();
		}
		return lines;
	}
	
	public void scp(File file, String dest)
	{
		scp(file, file.getName(), dest);
	}
	
	public void scp(File file, String fileName, String dest)
	{
		try
		{
			openChannelExec();
			String command = "scp -t " + dest;
			channelExec.setCommand(command);
			try(OutputStream output = channelExec.getOutputStream(); FileInputStream fileInput = new FileInputStream(file);)
			{
				channelExec.connect();
				String command2 = "C0644 " + file.length() + " " + fileName + "\n";
				output.write(command2.getBytes());
				output.flush();
				IoUtil.copy(fileInput, output);
				output.write((byte) 0);
				output.flush();
			}
		}
		catch(JSchException | IOException e)
		{
			throw new SshException(e);
		}
		finally
		{
			closeChannelExec();
		}
	}
	
	public void closeChannelExec()
	{
		if(channelExec != null)
		{
			channelExec.disconnect();
		}
	}
	
	public void closeSession()
	{
		if(session != null)
		{
			session.disconnect();
		}
	}
	
	@Override
	public void close()
	{
		closeChannelExec();
		closeSession();
	}
	
}
