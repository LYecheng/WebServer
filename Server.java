import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.RunnableFuture;

/**
 * Created by Yecheng Li & Mingyang Yu on 2/28/15.
 */
public class Server implements Runnable{
    Socket originSocket;
    int portNumber;
    Response response;

    Server(int portNumber) {
        this.portNumber = portNumber;
    }

    public void run(){
        this.start(this.portNumber);
    }

    public void start(int port) {
        int portNumber = port;
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(portNumber);
            while (true){
                this.originSocket = serverSocket.accept();
                try{
                    this.originSocket.setSoTimeout(1500);
                }catch(SocketException e){
                    e.printStackTrace();
                }
                DataOutputStream out=null;
                DataInputStream in=null;
                boolean connection=true;
                while(true){
                    try{
                        Socket clientSocket = originSocket;
                        if(out==null&&in==null){
                            out = new DataOutputStream(clientSocket.getOutputStream());
                            in = new DataInputStream(clientSocket.getInputStream());
                        }
                        String request = in.readLine();
                        if (request == null) {
                            continue;
                        }
                        response.printLog(clientSocket, request);

                        //Skip other lines of the request
                        while (true) {
                            String otherRequestInfo = in.readLine();
                            System.out.println("here: "+otherRequestInfo);
                            if (otherRequestInfo == null || otherRequestInfo.length() == 0) {
                                break;
                            }
                            else if (otherRequestInfo.startsWith("Connection")){
                                String [] connectionState = otherRequestInfo.split(" ");
                                if (connectionState[1].equals("close")||connectionState[1].equals("Close")){
                                    connection = false;
                                }

                            }
                                                    }
                        clientSocket.setKeepAlive(connection);

                        if (response.respondToClient(clientSocket,request,out)<0)
                        {
                            break;
                        }
                        
                        if(!originSocket.getKeepAlive()){
                            originSocket.close();
                            break;
                        }
                    }catch (SocketException e){
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        break;
                    }catch (IOException e) {
                        break;
                    }
                }

            }
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
