package jackpal.androidterm;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import jackpal.androidterm.emulatorview.TermSession;
import jackpal.androidterm.emulatorview.TermSession.FinishCallback;
import jackpal.androidterm.util.TermSettings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.xl.runC.ofToApk1.R;

//XL 控制台Fragment
public class TermFragment extends Fragment implements FinishCallback{
	private TermSession mTermSession = null;
	private String mInitCmd="cd /sdcard/\n\n";
	private TermView mEmulatorView = null;
	@SuppressLint("SdCardPath") 
  
	private Handler mHandler = null;
	private String mChangePS1Cmd = new File("/system/bin/basename").canExecute() ? "export PS1='$USER:`basename \"$PWD\"`\\$';" : "";
	private ViewGroup mControlLayout = null;
	public TermFragment() {
	}

	public TermFragment(String cmd,String home){
		mInitCmd=cmd;
		
	}

	
	public TermFragment(String cmd,boolean runAsSu){
		Log.e("cms",cmd);
		mInitCmd=cmd;
		
	}
	
	private int [][] mButonMap = {
			//id	isdown	char	key
			{R.id.termButton_1,0,0,KeyEvent.KEYCODE_ESCAPE},
			{R.id.termButton_2,0,0,KeyEvent.KEYCODE_TAB},
			{R.id.termButton_3,0,0,KeyEvent.KEYCODE_DPAD_UP},
			{R.id.termButton_4,0,'$',0},
			{R.id.termButton_5,0,'`',0},
			
			{R.id.termButton_6,0,0,KeyEvent.KEYCODE_CTRL_LEFT},
			{R.id.termButton_7,0,0,KeyEvent.KEYCODE_DPAD_LEFT},
			{R.id.termButton_8,0,0,KeyEvent.KEYCODE_DPAD_DOWN},
			{R.id.termButton_9,0,0,KeyEvent.KEYCODE_DPAD_RIGHT},
			{R.id.termButton_10,0,'\'',0},
	};
	private int mFristKeyDelay = 200;
	private int mKeyDelay = 60;
	
	private Runnable mKeyLoopRunnable = null;
	private void setAllViewListener(View view){
		mHandler = new Handler();
		if(view instanceof ViewGroup){
			ViewGroup viewGroup = (ViewGroup) view;
			for(int i=0;i<viewGroup.getChildCount();i++){
				setAllViewListener(viewGroup.getChildAt(i));
			}
		}

		if(view instanceof ImageButton){
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mControlLayout.getVisibility() == View.VISIBLE){
						mControlLayout.setVisibility(View.GONE);
					}else{
						mControlLayout.setVisibility(View.VISIBLE);
					}
				}
			});
			view.setOnTouchListener(new View.OnTouchListener() {
				@SuppressWarnings("deprecation")
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch(event.getAction() ){
					case MotionEvent.ACTION_DOWN :
						v.setBackgroundDrawable(new ColorDrawable(Color.rgb(0x80, 0x80, 0xb0)));
						break;
						
					case MotionEvent.ACTION_UP :
					case MotionEvent.ACTION_OUTSIDE :
					case MotionEvent.ACTION_CANCEL :
						v.setBackgroundColor(Color.TRANSPARENT);
						break;
					}
					return false;
				}
			});
		}
		
		if(view instanceof Button){
			view.setOnTouchListener(new View.OnTouchListener() {				
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mButonMap[5][3] = mEmulatorView.getControlKeyCode();
					for(final int[] key:mButonMap){
						if(key[0]==v.getId()){
							switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								key[1] = 1;
								break;
							case MotionEvent.ACTION_UP:
								key[1] = 0;
								break;
							default:
								break;
							}
							
							if(key[2]!=0){
								if(event.getAction()==MotionEvent.ACTION_DOWN){
									mEmulatorView.getTermSession().write(key[2]);
									if(mKeyLoopRunnable!=null){
										mHandler.removeCallbacks(mKeyLoopRunnable);
									}
									mKeyLoopRunnable = new Runnable() {
										@Override
										public void run() {
											if(key[1] == 1){
												mEmulatorView.getTermSession().write(key[2]);
												mHandler.postDelayed(this,mKeyDelay);
											}
										}
									};
									mHandler.postDelayed(mKeyLoopRunnable,mFristKeyDelay);
								}
							}else
							switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:{
									final KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, key[3]);
									mEmulatorView.onKeyDown(keyEvent.getKeyCode(), keyEvent);
									if(mKeyLoopRunnable!=null){
										mHandler.removeCallbacks(mKeyLoopRunnable);
									}
									mKeyLoopRunnable = new Runnable() {
										@Override
										public void run() {
											if(key[1] == 1){
												mEmulatorView.onKeyDown(keyEvent.getKeyCode(), keyEvent);
												mHandler.postDelayed(this,mKeyDelay);
											}
										}
									};
									mHandler.postDelayed(mKeyLoopRunnable,mFristKeyDelay);
								}
								break;

							case MotionEvent.ACTION_UP:{
									final KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, key[3]);
									mEmulatorView.onKeyUp(keyEvent.getKeyCode(), keyEvent);
								}
								break;
							default:
								break;
							}
						}
					}
					
					return v.onTouchEvent(event);
				}
			});
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
	// TODO: Implement this method
	//mEmulatorView.onContextItemSelected(item);
	return super.onContextItemSelected(item);
	}
	
	
	
	public String getInitCmdEx(String cmd)
	{
		if(cmd == null||cmd.length()<0)
			cmd="";
		cmd = "cd;"+mChangePS1Cmd+cmd;
		//XL
		cmd =/* PluginsManager.getCmd(getActivity())+ */cmd;
		String ret = "clear;";//"cd $HOME\n";
		File file = getNextRunnableSh(this.getActivity());
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			try {
				fileOutputStream.write(cmd.getBytes());
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		file.setExecutable(true, false);
		ret += ". "+file.getAbsolutePath();
		return ret;
	}
	
	private static int mShNumber = 0;
	private static File getNextRunnableSh(Context context)
	{
		String runnablePath = context.getFilesDir().getAbsolutePath();
		File []files = new File(runnablePath).listFiles();
		if(files!=null)
		for (File file2 : files) {
			if(file2.getName().endsWith(".tsh"))
				file2.delete();
		}
		File file = null;
		for(int i=0;i<1000;i++)
		{
			file = new File(runnablePath+"/"+mShNumber+".tsh");
			mShNumber++;
			if(!file.isFile())
				break;
		}
		return file;
	}
	
	
	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_term, null);
		RelativeLayout workSpaceLayout = (RelativeLayout) relativeLayout.findViewById(R.id.layout_work_space);
		mTermSession = createTermSession();
		mTermSession.setFinishCallback(this);
		mEmulatorView = createEmulatorView(mTermSession);
		workSpaceLayout.addView(mEmulatorView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mControlLayout = (ViewGroup) relativeLayout.findViewById(R.id.layout_control);
		mControlLayout.setVisibility(View.GONE);
		setAllViewListener(relativeLayout);
		return relativeLayout;
	}

	//XLTERMVIEW
    private TermView createEmulatorView(TermSession session) {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        TermView termView = new TermView(getActivity(), session, metrics);
        termView.updatePrefs(mTermSettings);
        //EmulatorView emulatorView = termView;
        registerForContextMenu(termView);
        return termView;
    }

    private TermSettings mTermSettings = null;
    private TermSession createTermSession() {
    	TermSettings settings = new TermSettings(getResources(), getActivity().getPreferences(0));
    	mTermSettings = settings;
			
			//xl
			/*
    	mTermSettings.setAppendPath(Busybox.getWorkDir(getActivity())+":"
    			+GNUCCompiler.getCcPath(getActivity())+":"
    			+GNUCCompiler.getAbiPath(getActivity())+":"
    			+GNUCCompiler.getGccPath(getActivity())
    			);
					
    	mTermSettings.setHomePath(FileBowserFragment.mDefaultFile.getAbsolutePath());
			*/
    	TermSession session = createTermSession(getActivity(), settings, mInitCmd );
        return session;
    }
    
    protected static TermSession createTermSession(Context context, TermSettings settings, String initialCommand) {
        ShellTermSession session = new ShellTermSession(settings, initialCommand);
        session.initializeEmulator(64, 64);
        session.setProcessExitMessage("do you want exit?");
        return session;
    }
    
    public void destory()
    {
    	if(mTermSession.isRunning())
    		mTermSession.finish();
    }

	@Override
	public void onSessionFinish(TermSession session) {
		if(mFinishCallback!=null)
			mFinishCallback.onSessionFinish(session);
	}
	
	private TermSession.FinishCallback mFinishCallback = null;
	public void setFinishCallback(TermSession.FinishCallback callback)
	{
		mFinishCallback = callback;
	}

}
