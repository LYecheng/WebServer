import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by Yecheng Li & Mingyang YU on 2/28/15.
 */

public class SecureServer implements Runnable{
    Socket originSocket = null;
    int portNumber;
    Response secureResponse;

    SecureServer(int portNumber) {
        this.portNumber = portNumber;
    }

    // override
    public void run(){
        this.start(this.portNumber);
    }

    public void start(int port) {
        int portNumber = port;

        char[] password = "password".toCharArray();
        SSLContext sslContext;
        try{
            sslContext = SSLContext.getInstance("TLS");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream keyFile = new FileInputStream("server.jks");
            keyStore.load(keyFile, password);
            KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
            keyManager.init(keyStore, password);
            sslContext.init(keyManager.getKeyManagers(), null, null);
            SSLServerSocketFactory ssfac = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) ssfac.createServerSocket(portNumber);

            while (true) {
                this.originSocket =serverSocket.accept();
                try{
                    this.originSocket.setSoTimeout(1500);
                }catch(SocketException e)
                {
                    e.printStackTrace();
                }
                DataOutputStream out= null;
                DataInputStream in= null;
                boolean connection=true;
                int index=0;
                while(true){
                    try{
                        index++;
                        Socket clientSocket = originSocket;
//                        System.out.println("1:  "+index);
                        if(out==null&&in==null){
//                            System.out.println("12:  "+index);
                            out =new DataOutputStream(clientSocket.getOutputStream());
                            in = new DataInputStream(clientSocket.getInputStream());
                        }
//                        System.out.println("2:  "+index);
                        String request = in.readLine();
                        //System.out.println("request Message: " + request);
                        if (request == null) {
                            continue;
                        }
//                        System.out.println("7:  " + index);
                        secureResponse.printLog(clientSocket, request);
                        while(true){
                            String otherRequestInfo = in.readLine();
                            //System.out.println("request Message: "+otherRequestInfo);
                            if (otherRequestInfo == null || otherRequestInfo.length() == 0) {
                                break;
                            }
                            else if (otherRequestInfo.startsWith("Connection")){
//                                System.out.println("3:  "+index);
                                String [] connectionState = otherRequestInfo.split(" ");
                                if (connectionState[1].equals("close")||connectionState[1].equals("Close")){
                                    connection = false;
                                }
                                else if(connectionState[1].equals("Keep-Alive")&& index==2){
                                    connection = false;
                                }
                            }
                        }
//                        System.out.println("4:  "+index);
                        clientSocket.setKeepAlive(connection);
                        if (secureResponse.respondToClient(clientSocket,request,out)<0)
                        {
                            break;
                        }
                        if(!originSocket.getKeepAlive()){
//                            System.out.println("5:  "+index);
                            originSocket.close();
                            break;
                        }
//                        System.out.println("6:  "+index);
                    } catch (SocketException e){
                        //System.out.println(e.getMessage());
                        //e.printStackTrace();
                        break;
                    }catch (IOException e) {
                        break;
                    }
                }
            }
        }
        catch(NoSuchAlgorithmException ex){
            System.out.println(ex.getMessage());
        }
        catch(KeyStoreException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.out.println(ex.getMessage());
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        catch(CertificateException ex){
            System.out.println(ex.getMessage());
        }
        catch(UnrecoverableKeyException ex){
            System.out.println(ex.getMessage());
        }
        catch(KeyManagementException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}
