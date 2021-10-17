This is a multi-threaded web server developed using Java (jdk-15.0.2). The java program name is Server.java.

Before compiling the program, you may add files that you want to show in client. 
Files, for example, text file,html file, jpg file, etc.
They must be in the same directory with the Server.java

To compile and run the program, there are two ways to run.
First way:
	Using integrated development environment to run the Java program.
Second way:
	1. Create a batch file “Server.bat”in the same directory with the Java program. And the content as below:
	   javac Server.java
	   pause
	   java Server
	
	2. Double-click the “Server.bat” to compile and execute webserver.
	   Hit Enter when you see “請按任意鍵繼續 . . .” or “Press any key to continue”

If the Server.java is running successfully, two follwoing sentences will be shown.
"Server started. "
"Start listening on port: 8140"

Last, start a web browser or client and key in http://127.0.0.1:8140/________.___ (e.g Hello.html/hello.jpg <-- The last part is the file name added before)
After hitting Enter. The content of that file will display.

After displaying the content of file on client , "Connection closed." will be shown.

Type the key in http://127.0.0.1:8140/________.___ (e.g Hello.html/hello.jpg) again if you want to display another files.

Finally, stop running the integrated development environment or pressing ^C in batch file to terminate the Web server.

Note:
Http reqeust message should start with GET or HEAD request methods, otherwise 400 BAD Request will be sent.
"200 OK" will be sent to client when it is successful request.
"404 Not Found" will be sent to client when the fike is not found in the same directory with the Server.java.

MIME types, besides .html, .gif, .jpeg/.jpg, .png, .txt, will be downloaded if client is a browser.


