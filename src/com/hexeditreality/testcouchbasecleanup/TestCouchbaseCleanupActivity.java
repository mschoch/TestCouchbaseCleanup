package com.hexeditreality.testcouchbasecleanup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.android.CouchbaseMobile;
import com.couchbase.android.ICouchbaseDelegate;

public class TestCouchbaseCleanupActivity extends Activity implements OnClickListener {
    
	private Button startButton;
	private Button killCouchbaseButton;
	private Button stopButton;
	private TextView openFileCount;
	private UpdateStatsAsyncTask updateStatsTask;
	
	private ServiceConnection couchbaseServiceConnection;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startButton = (Button)findViewById(R.id.start_couchbase_button);
        startButton.setOnClickListener(this);
        killCouchbaseButton = (Button)findViewById(R.id.kill_couchbase_button);
        killCouchbaseButton.setOnClickListener(this);
        stopButton = (Button)findViewById(R.id.stop_couchbase_button);
        stopButton.setOnClickListener(this);
        
        openFileCount = (TextView)findViewById(R.id.open_file_count);
        updateOpenFileCount();
        
        updateStatsTask = new UpdateStatsAsyncTask(this);
        updateStatsTask.execute((Integer)null);

    }
    
    @Override
    public void onClick(View v) {
    	if(v == startButton) {
    		CouchbaseMobile couchbaseMobile = new CouchbaseMobile(getBaseContext(), mDelegate);
    		couchbaseServiceConnection = couchbaseMobile.startCouchbase();
    	}
    	else if(v == killCouchbaseButton) {
    		int couch_pid = findPidOfBeam();
    		if(couch_pid != -1) {
    			android.os.Process.killProcess(couch_pid);
    		}
    	}
    	else if(v == stopButton) {
    		if(couchbaseServiceConnection != null) {
	    		unbindService(couchbaseServiceConnection);
	    		couchbaseServiceConnection = null;
    		}
    	}
    }
    
    private int findPidOfBeam() {
    	InputStream is = null;
    	InputStream es = null;
    	OutputStream os = null;
    	Process p = null;
		try {
			String[] args = new String[] { "ps", "beam" };
			p = Runtime.getRuntime().exec(args);
			is = p.getInputStream();
			os = p.getOutputStream();
			es = p.getErrorStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line;
			while((line = br.readLine()) != null) {
				if(line.contains("beam")) {
					String[] pieces = line.split("\\s+");
					if(pieces.length > 2) {
						try {
							return Integer.parseInt(pieces[1]);
						}
						catch(NumberFormatException nfe) {
							//ignore and return default value for no match
						}
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				is.close();
				os.close();
				es.close();
				p.destroy();
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		return -1;
    }
    
    public void updateOpenFileCount() {
    	int count = 0;
    	InputStream is = null;
    	InputStream es = null;
    	OutputStream os = null;
    	Process p = null;
		try {
			int pid = android.os.Process.myPid();
			String[] args = new String[] { "ls", "/proc/" + pid + "/fd" };
			p = Runtime.getRuntime().exec(args);
			is = p.getInputStream();
			os = p.getOutputStream();
			es = p.getErrorStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while(br.readLine() != null) {
				count++;
			}
			
			openFileCount.setText("" + count);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				is.close();
				os.close();
				es.close();
				p.destroy();
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}

    }
    
    private ICouchbaseDelegate mDelegate = new ICouchbaseDelegate() {
		
		@Override
		public void installing(int completed, int total) {

		}
		
		@Override
		public void exit(String error) {
    		unbindService(couchbaseServiceConnection);
    		couchbaseServiceConnection = null;			
			Toast t = Toast.makeText(TestCouchbaseCleanupActivity.this, error, 5);
			t.show();
		}
		
		@Override
		public void couchbaseStarted(String host, int port) {
			Toast t = Toast.makeText(TestCouchbaseCleanupActivity.this, "Couchbase started on http://" + host + ":" + port, 5);
			t.show();
			
		}
	};
}