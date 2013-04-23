MthreadServer
=============

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
