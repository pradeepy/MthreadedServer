package com.mthread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ThreadPool extends Thread {
	private Queue<RunnableElt> task_queue;
	private WorkerThread[] threads;
	private boolean terminate;
	private final int port;
	private Lock l;
	
	public ThreadPool () {
		this (5, 7081);
	}
	
	public ThreadPool(int num, int port) {
		threads = new WorkerThread[num];
		task_queue = new ConcurrentLinkedQueue<RunnableElt>();
		this.port = port;
		l = new ReentrantLock();
	}

	public void terminateThread () {
		terminate = true;
	}

	public void run () {
		ServerSocket s = null;
		try {
			s = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 
		
		HTTPThread ht = new HTTPThread(s);
		ht.start();
		
		for (WorkerThread thread : threads) {
			thread = new WorkerThread();
			thread.start();
		}	
		
		while (!terminate) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
				
			}
		}
		
		try {
			ht.terminateHTTPThread();
			Thread.sleep(500);
			s.close();
			for (WorkerThread thread : threads) 
				thread.terminateWorkerThread();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
	}
	
	private class WorkerThread extends Thread {
		private boolean terminate;
		public void terminateWorkerThread () {
			terminate = true;
		}
		
		public void run () {
			while (!terminate) {
				RunnableElt r = null;
				l.lock(); 
				try {
					if (task_queue.isEmpty())
						continue;
					r = task_queue.remove();
				} finally {
					l.unlock();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				r.run();
			}
		}
	}
	
	
	private class RunnableElt implements Runnable {
		private Socket cSocket;
		public RunnableElt (Socket cSocket) {
			this.cSocket = cSocket;
		}
		
        @Override
		public void run() {
        	try {
        		if (cSocket == null)
        			return;
        		InputStream in = cSocket.getInputStream();
        		InputStreamReader isr = new InputStreamReader(in);
        		BufferedReader br = new BufferedReader(isr);
        		String temp = br.readLine();
        		int start = temp.indexOf("GET /") + 5;
        		int end = temp.indexOf(" HTTP/1.1");
        		temp = temp.substring(start, end);
        		writeFile(temp);
        		cSocket.close();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
		}
		
        private void writeFile (String fileName) {
        	File f = null;
        	f = new File (fileName);
        	if (f.exists() && !f.isDirectory()) {
        		writeToSocket(f);
        	} else {
        		listContents();
        	}
        }
        
        
        private void listContents () {
        	try {
				PrintWriter out = new PrintWriter (cSocket.getOutputStream());
				ProcessBuilder pb = new ProcessBuilder("bash","-c","ls -l");
				Process proc = pb.start();
				BufferedReader br_in = new BufferedReader(new 
			             InputStreamReader(proc.getInputStream()));;
			    Thread.sleep(200);         
				String line = "";
				while( (line = br_in.readLine())!=  null) {
					out.println(line);
				}	
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
        }
        
        private void writeToSocket (File f) {
        	byte[] byte_array  = new byte [(int)f.length()];
            FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
			    BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(byte_array,0,byte_array.length);
                OutputStream os = cSocket.getOutputStream();
                os.write(byte_array,0,byte_array.length);
                os.flush();
                os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
	}
	
	
	private class HTTPThread extends Thread {
		private boolean terminateHTTP;
		ServerSocket s;
		public void terminateHTTPThread() {
			terminateHTTP = true;
		}
		public HTTPThread (ServerSocket s) {
			this.s = s;
		}
		
		public void run () {
			while (!terminateHTTP) {
			    try {
				    Socket cSocket = s.accept();
				    Thread.sleep(1000);
				    task_queue.add(new RunnableElt(cSocket));
			    } catch (Exception e) {
			    	//e.printStackTrace();
			    }
			}
		}
	}

	
}

