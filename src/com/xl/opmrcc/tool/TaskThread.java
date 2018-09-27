package com.xl.opmrcc.tool;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.xl.game.tool.CopyFile;
import com.xl.game.tool.Log;
import com.xl.game.tool.UnzipAssets;
import com.xl.game.tool.ZipUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
/*
任务类
解压zip
设置权限
执行linux命令
删除文件
新建文件
新建文件夹
复制文件
压缩zip

*/
public class TaskThread extends Thread
{
	ArrayList<Task> tasks;
	Context context;
	Handler handler;
	OnTaskListener listener;
	
	public interface OnTaskListener
	{
		public void onTaskEnd(); //任务完成
		public void onTaskError(Exception e); //出错
	}
	
	public TaskThread(Context context)
	{
		this.context = context;
		this.tasks = new ArrayList<Task>();
		this.handler = new Handler(Looper.getMainLooper())
		{
			public void handleMessage(android.os.Message msg) 
			{
				if(msg.what==0)
				{
					if(listener!=null)
					listener.onTaskEnd();
				}
				else if(msg.what==-1)
				{
					if(listener!=null)
					listener.onTaskError((Exception)msg.obj);
				}
			}
		};
	}
	
	public class Task
	{
		String name;
		String value;
		
		public Task(String name,String value)
		{
			this.name=name;
			this.value = value;
		}
		
		
		public String getName()
		{
			return name;
		}
		
		public String getValue()
		{
			return value;
		}
		
		
		
		
		
		
	}
	
	
	
	/*
	添加任务 任务名字 任务内容
	name
	  unzip
		shell
		mkdir
	
	*/
	public void addTask(String name,String value)
	{
		tasks.add(new Task(name,value));
	}
	
	//
	public void setOnTaskListener(OnTaskListener listener)
	{
		this.listener = listener;
	}
	
	private void writeStreamToFile(InputStream stream, File file)
	{
		try
		{
			//
			OutputStream output = null;
			try
			{
				output = new FileOutputStream(file);
			}
			catch (FileNotFoundException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try
			{
				try
				{
					final byte[] buffer = new byte[2048*10];
					int read;

					while ((read = stream.read(buffer)) != -1)
						output.write(buffer, 0, read);

					output.flush();
				}
				finally
				{
					output.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void run()
	{
		while(tasks.size()>0)
		{
			Task task =  tasks.get(0);
			if(task.getName().equals("unzip"))
			{
				String value = task.getValue();
				String items[] = value.split(" ");
				if(items.length>=2)
				{
					if(items[0].startsWith("assets://"))
					{
				  	try
				  	{
							Log.e("unzip",task.getValue());
						if(context!=null)
			  		UnzipAssets.unZip(context, items[0].substring(9), items[1], true);
			  		}
		  			catch (IOException e)
			   		{
						
			  		}
					}
					else
					{
				  	try
			  		{
			  		ZipUtils.unZipFile(items[0], items[1]);
			  		}
			  		catch (IOException e)
			  		{
							
						}
			  	}
					
						
				}
				
					
			}
			else if(task.getName().equals("shell"))
			{
				ShellUtils.execCommand(task.getValue(),false);
				Log.e("shell",task.getValue());
			}
			else if(task.getName().equals("mkdir"))
			{
				File file = new File(task.getValue());
				if(!file.exists())
				{
					file.mkdirs();
				}
			}
			else if(task.getName().equals("rmdir"))
			{
				CopyFile.delFolder(task.getValue());
			}
			else if(task.getName().equals("rm"))
			{
				CopyFile.delFile(task.getValue());
			}
			else if(task.getName().equals("unpack"))
			{
				String value = task.getValue();
				String items[] = value.split(" ");
				try
				{
					writeStreamToFile(context.getAssets().open(items[0]), new File(items[1]));
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			else if(task.getName().equals("download"))//下载 参数：url 文件路径
			{
				
			}
			tasks.remove(0);
		}
		Message  m = new Message();
		m.what=0;
		handler.sendMessage(m);
	}
	
	
	
	
	
}
