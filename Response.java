import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by LiYecheng & Mingyang YU on 03/03/15.
 */
public class Response {

    // Print connection Message and information
    public static int respondToClient(Socket clientSocket, String request, DataOutputStream out){

        if ((!request.startsWith("GET") && !request.startsWith("HEAD")) || !(request.endsWith("HTTP/1.1"))) {
            formErrPage(out, clientSocket, "403", "Invalid Request", "Client request could not be understood");
        }
        else {
            int index=0;
            String[] requests = request.split(" ");
            try {
                //If here is a redirect for that path in the special redirect.defs file, return "301"
                File redirectFile = new File("www/redirect.defs");
                Scanner scanFile = new Scanner(redirectFile);
                while (scanFile.hasNextLine()) {
                    String[] scanRedirects = scanFile.nextLine().split(" ");
                    if (requests[1].equals(scanRedirects[0])) {
                        StringBuilder response = new StringBuilder()
                                .append("HTTP/1.1 301 Move Permanently\r\n" + "Location: "+scanRedirects[1]+"\r\n" +
                                        "Content-Type:text/html\r\n"
                                        + "Content_Length:" + "\r\n\r\n");
                        out.writeBytes(response.toString());

                        index = 1;
                    }
                }
            } catch (IOException e) {
                System.out.println("There is something wrong with opening file redirect.defs.");
            }

            //Proceed only when not redirected
            if (index==0) {
                String path = "www" + requests[1];
                if (path.equals("www/redirect.defs")) {
                    formErrPage(out, clientSocket, "404", "Not Found", "The requested path does not exist.");
                }
                else {
                    File f = new File(path);
                    //Format the path of the directory index.html page
                    if (f.isDirectory()) {
                        if (path.endsWith("/"))
                            path = path + "index.html";
                        else
                            path = path + "/index.html";
                    }
                    try {
                        f = new File(path);
                        //Print header if the file does exist (for both HEAD and GET)
                        byte [] content = printRowFile(f);
                        if(f.exists()){

                            StringBuilder response = new StringBuilder()
                                    .append("HTTP/1.1 200 OK\r\n")
                                    .append("Content-Type: text/html\r\n")
                                    .append("Server: project2\r\n")
                                            //.append("Connection: close\r\n")
                                    .append("Connection: keep-Alive\r\n")
                                    .append(String.format("Content-Length: %d\r\n", content.length));
                            out.writeBytes(response.toString());
//                            System.out.println("Content length: "+content.length);
                            if (requests[0].equals("GET")) {
                                out.writeBytes("\n\r");

                                ByteArrayOutputStream outBS= new ByteArrayOutputStream();
                                outBS.write(content, 0, content.length);
                                outBS.writeTo(out);
                                out.flush();
                            }
                        }
                        else {
                            formErrPage(out, clientSocket, "404", "Not Found", "The requested URL is not found.");
                        }
                    } catch (FileNotFoundException e) {
                        formErrPage(out, clientSocket, "404", "Not Found", "The requested URL is not found.");
                    }catch (IOException ioe){
                        System.out.println(ioe.getMessage());
                    }
                }
            }
        }
        return 0;
    }

    // Print log to server end
    public static void printLog(Socket newSocket, String message)
    {
        System.err.println("["+newSocket.getInetAddress().getHostAddress() + ":"+ newSocket.getPort()+"] "+message);
    }

    // Print message according to response code 403 or 404
    private static void formErrPage(DataOutputStream printOut, Socket newSocket, String respCode, String title, String message)
    {
        try{
            StringBuilder response = new StringBuilder()
                    .append("HTTP/1.1 " + respCode + " " + title + "\r\n\r\n" +
                            "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" +
                            "<TITLE>" + respCode + " " + title + "</TITLE>\r\n" +
                            "</HEAD><BODY>\r\n" +
                            "<H1>" + title + "</H1>\r\n" + message + "<P>\r\n" +
                            "<HR><ADDRESS>FileServer 1.1 at " +
                            newSocket.getLocalAddress().getHostName() +
                            " Port " + newSocket.getLocalPort() + "</ADDRESS>\r\n" +
                            "</BODY></HTML>\r\n\r\n");
            printOut.writeBytes(response.toString());
        }catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }

    }

    //Help to format the proper content type for header
    private static String getFileType(String path)
    {
        if (path.endsWith(".html") || path.endsWith(".htm"))
            return "text/html";
        else if (path.endsWith(".pdf"))
            return "application/pdf";
        else if (path.endsWith(".png"))
            return "image/png";
        else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
            return "image/jpeg";
        else
            return "text/plain";
    }

    //Print file content to output stream
    private static byte[] printRowFile(File inFile) throws IOException
    {
        InputStream in = new BufferedInputStream(new FileInputStream(inFile));
        int fileBytes = (int) inFile.length();
        byte[] inByteBuf = new byte[fileBytes];

        for (int totalBytesRead = 0; totalBytesRead < fileBytes; ) {
            int bytesRead = in.read(inByteBuf, totalBytesRead, fileBytes-totalBytesRead);
            if (bytesRead < 0) {
                System.out.println(String.format("bytesRead = %d", bytesRead));
            } else {
                totalBytesRead += bytesRead;
            }
        }
        return inByteBuf;

    }
}
