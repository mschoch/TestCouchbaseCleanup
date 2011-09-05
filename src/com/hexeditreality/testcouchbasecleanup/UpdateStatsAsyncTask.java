package com.hexeditreality.testcouchbasecleanup;

import android.os.AsyncTask;

public class UpdateStatsAsyncTask extends AsyncTask<Integer, Void, Void> {
	
	private TestCouchbaseCleanupActivity parent;
	private boolean running = false;
	
	public UpdateStatsAsyncTask(TestCouchbaseCleanupActivity parent) {
		this.parent = parent;
		this.running = true;
	}
	
	@Override
	protected Void doInBackground(Integer... params) {
		
		while(running) {
			publishProgress((Void)null);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//ignore
			}
		}
        return null;
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		parent.updateOpenFileCount();
	}
	
	@Override
	protected void onCancelled() {
		running = false;
		super.onCancelled();
	}

}
