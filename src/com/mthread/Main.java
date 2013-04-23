/*******************************************************************
 *   Multi-threaded HTTP Server using a custom Thread Pool
 *   1. Works on local IP address (127.0.0.1) 
 *   2. Uses HTTP GET requests as client input
 *   3. Output :
 *      3.1 If valid file name in current folder, returns the file
 *      3.2 Else returns folder contents
 *   4. Number of threads : > 0 AND < 20
 *      Port number       : > 1024 AND < 40000 
 *   
 *   Author : Pradeep Yenneti
 * 
 ********************************************************************/


package com.mthread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

class Main {
	public static void main (String[] args) {
		int port = 7081;
		int no_threads = 5;
		int input_port = port;
		int input_no_threads = no_threads;
		boolean set_threadpool = false;
		ThreadPool t = null;
		
		while (true) {
			if (set_threadpool == false)
			    System.out.println("Enter number of threads and port number separated by a space");
			else
				System.out.println("Type EXIT to exit the program");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String input = br.readLine();
				if (input.contains("EXIT"))
					break;
				if (!set_threadpool) {
				    int index = input.indexOf(' ');
				    input_no_threads = Integer.parseInt(input.substring(0, index));
				    index = input.lastIndexOf(' ');
				    input_port = Integer.parseInt(input.substring(index+1));
				    if (input_port < 1024 || input_port > 40000 || input_no_threads < 1 || input_no_threads > 20) {
				    	continue;
				    }	
					if (!set_threadpool) {
						if (!checkPort(input_port))
							continue;
						t = new ThreadPool(input_no_threads, input_port);
					    t.start();
					    set_threadpool = true;
					    System.out.println("Server started on port "+input_port+" with "+input_no_threads+" threads");
					}
				}
			} catch (Exception e) {
				System.out.println("Try again...");
			}
			
		}
		
		if (t != null)
			t.terminateThread();
		System.out.println("Server closed. Exiting ... ");
	}
	
	private static boolean checkPort (int port) {
		boolean result = true;
		ServerSocket s = null;
		try {
		    s = new ServerSocket (port);
		} catch (Exception e) {
			System.out.println("Unable to use "+port);
			result = false;
		}
		if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return result;
	}
}


