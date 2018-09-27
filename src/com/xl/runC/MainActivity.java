package com.xl.runC;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.view.View.*;
import android.content.res.*;
import java.io.*;
import android.content.*;
import android.net.*;
import com.xl.runC.ofToApk1.R;
import com.xl.opmrcc.tool.TaskThread;
import jackpal.androidterm.TermFragment;

public class MainActivity extends Activity 
{

	


	TermFragment mWeixin;
	Button btn_slzw_install;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
			
			FragmentManager fm = getFragmentManager();
			final FragmentTransaction transaction = fm.beginTransaction();
			
				final String cmd = "dalvikvm -cp \""+getFilesDir().getPath()+File.separatorChar+"classes.dex.zip\" Main\n";
				
			
			
		    TaskThread task = new TaskThread(this);
				//解压可执行文件到files目录
				task.addTask("unpack","app.a "+getFilesDir().getPath()+File.separatorChar+"app.a");
				task.addTask("unpack", "classes.dex.zip "+getFilesDir().getPath()+File.separatorChar+"classes.dex.zip");
				
			task.addTask("shell","chmod 777 "+getFilesDir().getPath()+File.separatorChar+"app.a");
		task.addTask("shell","chmod 777 "+getFilesDir().getPath()+File.separatorChar+"classes.dex.zip");
				task.setOnTaskListener(new TaskThread.OnTaskListener()
				{

					@Override
					public void onTaskEnd()
					{
						String run_cmd = cmd;
						if(Build.VERSION.SDK_INT>=14){
							run_cmd = ""+cmd;
						}
						mWeixin = new TermFragment(run_cmd,"");
						transaction.replace(android.R.id.content, mWeixin);
						transaction.commit();
					}

					@Override
					public void onTaskError(Exception e)
					{
						// TODO: Implement this method
					}
					
					
				});
				task.start();
				//调用term执行
    }

	
	
	
	//从assets读取文本
	public static String getTextFromAssets(Context context, String assetspath) 
	{
		String r0_String;
		String r1_String = "";
		AssetManager assets = context.getResources().getAssets();
		try {
			InputStream input = assets.open(assetspath);
			byte[] buffer = new byte[input.available()];
			input.read(buffer);
			r0_String = new String(buffer, "UTF-8");
			input.close();
			return r0_String;
		} 
		catch (IOException r0_IOException)
		{
			r0_String = r1_String;
		}


		return r0_String;

	}
	
	
	//获取sd卡
	public static String getSDPath()
	{
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
		if(sdCardExist)
		{
			sdDir=Environment.getExternalStorageDirectory();//获取sd卡目录
		}
		else 
		{
			return null;
		}
		return sdDir.getPath();
	}
	
	public void inApk(String assersFileName)
	{
	AssetManager assets = getAssets();
	try
	{
		//获取assets资源目录下的himarket.mp3,实际上是himarket.apk,为了避免被编译压缩，修改后缀名。
		InputStream stream = assets.open(assersFileName );
		if(stream==null)
		{
			//Log.v(TAG,"no file");
			return;
		}

		String folder = getSDPath()+File.separator+"DownLoad";
		File f=new File(folder);
		if(!f.exists())
		{
			f.mkdir();
		}
	
		File file = new File(f,"slzw2.0.3.apk");
		//创建apk文件
		file.createNewFile();
		//将资源中的文件重写到sdcard中
		//<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
		writeStreamToFile(stream, file);
		//安装apk
		//<uses-permission android:name="android.permission.INSTALL_PACKAGES" />			
		installApk(file);
	}
	catch (IOException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}		
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

	//安装apk
	private void installApk(File apkfile)
	{
		//Log.v(TAG,apkPath);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(apkfile),
							  "application/vnd.android.package-archive");
		startActivity(intent);
	}
	
}
