
import java.io.* ;
import java.net.* ;
import java.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.* ;


public final class Server {

    public static void main(String argv[]) throws Exception {
        //Set port number.
        int port = 8140;
        ServerSocket ServerS = new ServerSocket(port);

        System.out.println("Server started.\nStart listening on port: " + port);
        System.out.println();

        // An infinite loop for servicing request messages indefinitely until user stop the server.
        while (true) {

            // Listen for a TCP connection request.
            Socket socket = ServerS.accept();
            System.out.println("Connection opened.");

            // Construct an object to run Request.
            Request ServerRequest = new Request(socket);

            // Create a new thread to process the request.
            Thread thread = new Thread(ServerRequest);

            // Start thread.
            thread.start();
        }
    }
}

final class Request implements Runnable
{
    //Create an socket object.
    Socket socket;

    //Set a static string that is \r\n.
    final static String newline = "\r\n";

    // Constructor of Request
    public Request(Socket socket) throws Exception
    {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Run Handle().
            Handle();
        } catch (Exception e) {
            // Check if Handle() have exceptions.
            System.out.println("E exception: "+e);
        }
    }

    private void Handle() throws Exception
    {
        //Create format for current time and date.
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        // Save down the current time
        LocalDateTime nowTime = LocalDateTime.now();

        SimpleDateFormat dateformat = new SimpleDateFormat ("E, dd MMM yyyy HH:mm:ss 'GMT'",Locale.ENGLISH);
        // Get a reference to the socket's input and output streams.
        DataOutputStream  output = new DataOutputStream(socket.getOutputStream());
        // Set up input stream filters.
        BufferedReader input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        // Create log file for recording statistics.
        FileOutputStream createlog =new FileOutputStream("Log.log", true);
        // read the first line of request message
        String requestMessage=input.readLine();

        System.out.println();
        System.out.println(requestMessage);

        String headerLine=null;
        String Ifmodified= null;
        Date IFmodified=null;
        String fileName = null;
        String responseType;
        // read the remaining header lines
        while ((headerLine = input.readLine()).length() != 0) {
            StringTokenizer IfModifiedSince = new StringTokenizer(headerLine);
            // check if there is If-Modifies-Since in the request messagge.
            if (IfModifiedSince.nextToken().equals("If-Modified-Since:")){
                // save the date if If-Modifies-Since exists.
                Ifmodified=headerLine;
                IFmodified= dateformat.parse(Ifmodified.substring(19));
            }
            // print out the request message.
            System.out.println(headerLine);
        }
        System.out.println();

        // ust .nextToken() to get the methods of request message.
        StringTokenizer token = new StringTokenizer(requestMessage);
        String requestMethod=token.nextToken();

        // To filter when the method is not Get and Head
        if (requestMethod.equals("GET") || requestMethod.equals("HEAD")){
            // ust .nextToken() to get the filename from request message.
            fileName= token.nextToken().toLowerCase();
            fileName= fileName.substring(1);
            File file;
            String contentType=null;
            int fileLength;

            // catch if there is FileNotFoundException
            try {
                // Create file object
                file = new File(fileName);
                fileLength = (int) file.length();
                // create object byte and call the function readFileData.
                byte[] fileInBytes =  readFileData(file, fileLength);
                // get the content type of the file.
                contentType = getContentType(fileName);
                // get the last modified date of the file.
                Date LastDate=new Date(file.lastModified());

                // if there is no If-Modified-Since, 200 OK and last-modified date will be sent to client.
                if (Ifmodified==null){
                    // save the responseType for log.
                    responseType="200 OK";
                    // Send the status line.
                    output.writeBytes("HTTP/1.1 200 OK"+newline);
                    // Send the header lines.
                    output.writeBytes("Connection close"+newline);
                    output.writeBytes("Date: "+new Date()+newline);
                    output.writeBytes("Server: Chloe's server 1.0.0"+newline);
                    output.writeBytes("Last-Modified: "+dateformat.format(LastDate)+newline);
                    output.writeBytes("Content-length: "+ fileLength+newline);
                    output.writeBytes("Content-type: "+contentType+newline);
                    output.writeBytes(newline);
                    // Send the content body if it is not HEAD method.
                    if (!requestMethod.equals("HEAD")) {
                        output.write(fileInBytes, 0, fileLength);
                    }
                }
                else if (!dateformat.format(LastDate).equals(dateformat.format(IFmodified))) {
                    // save the responseType for log.
                    responseType="200 OK";
                    // Send the status line.
                    output.writeBytes("HTTP/1.1 200 OK"+newline);
                    // Send the header lines.
                    output.writeBytes("Connection close"+newline);
                    output.writeBytes("Date: "+new Date()+newline);
                    output.writeBytes("Server: Chloe's server 1.0.0"+newline);
                    output.writeBytes("Last-Modified: "+dateformat.format(LastDate)+newline);
                    output.writeBytes("Content-length: "+ fileLength+newline);
                    output.writeBytes("Content-type: "+contentType+newline);
                    output.writeBytes(newline);
                    // Send the content body if it is not HEAD method.
                    if (!requestMethod.equals("HEAD")) {
                        output.write(fileInBytes, 0, fileLength);
                    }
                }
                else {
                    // save the responseType for log.
                    responseType="304 Not Modified";
                    // Send the status line.
                    output.writeBytes("HTTP/1.1 304 Not Modified"+newline);
                    // Send the header lines.
                    output.writeBytes("Connection close"+newline);
                    output.writeBytes("Date: "+new Date()+newline);
                    output.writeBytes("Server: Chloe's server 1.0.0"+newline);
                    output.writeBytes("Last-Modified: "+dateformat.format(LastDate)+newline);
                    output.writeBytes("Content-length: "+ fileLength+newline);
                    output.writeBytes("Content-type: "+contentType+newline);
                    output.writeBytes(newline);

                }

                output.flush();
            }
            catch (FileNotFoundException fileNotFoundException){
                // save the responseType for log.
                responseType="404 File Not Found";
                // Send the status line.
                output.writeBytes("HTTP/1.1 404 File Not Found"+newline);
                // Send the header lines.
                output.writeBytes("Date: "+new Date()+newline);
                output.writeBytes("Server: Chloe's server 1.0.0"+newline);
                output.writeBytes("Content-type: "+contentType+newline);
                output.writeBytes(newline);
                output.flush();
            }
        }
        else {
            // save the responseType for log.
            responseType="400 Bad Request";
            // Send the status line.
            output.writeBytes("HTTP/1.1 400 Bad Request"+newline);
            // Send the header lines.
            output.writeBytes("Connection close"+newline);
            output.writeBytes("Date: "+new Date()+newline);
            output.writeBytes("Server: Chloe's server 1.0.0"+newline);
            output.writeBytes(newline);
            output.flush();
        }
        // Append the log file.
        CreateLog(createlog,socket.getInetAddress().getHostName(),fileName,responseType,timeFormatter.format(nowTime));

        // Close streams and socket.
        output.close();
        input.close();
        socket.close();

        System.out.println("Connection closed.\n");

    }
    // return the fileDate (Content body)
    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileName) {
        if(fileName.endsWith(".html")) {
            return "text/html";
        }
        if(fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if(fileName.endsWith(".jpeg")||fileName.endsWith(".jpg")) {
            return "image/jpeg";
        }
        if(fileName.endsWith(".png")) {
            return "image/png";
        }
        if(fileName.endsWith(".txt")) {
            return "text/txt";
        }
        return "application/octet-stream";
    }

    // Append the log file by adding statistic to that file created above.
    private void CreateLog(FileOutputStream file,String hostname,String ReqFile,String responseTime, String accessTime){
        try{
            String nextline=newline;
            String space=" ";
            byte[] a =hostname.getBytes();
            byte[] b =ReqFile.getBytes();
            byte[] d =responseTime.getBytes();
            byte[] e =accessTime.getBytes();
            byte[] c =nextline.getBytes();
            byte[] ss =space.getBytes();
            file.write(a);           //writes bytes into file
            file.write(ss);
            file.write(e);
            file.write(ss);
            file.write(b);
            file.write(ss);
            file.write(d);
            file.write(ss);
            file.write(c);
            file.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
        }
    }
}

