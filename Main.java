/**
 * Created by Yecheng Li & Mingyang Yu on 2/28/15.
 */
import java.io.*;
import java.util.concurrent.*;
public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Invalid argument, please follow: --port=#### --securePort=#### format");
            System.exit(1);
        }

        String foo = args[0];
        String fooSecure = args[1];
        String[] parts = foo.split("=");
        String[] partsSecure = fooSecure.split("=");

        if ((parts.length == 2) && (parts[0].equals("--port")) &&(partsSecure.length==2) && (partsSecure[0].equals("--securePort"))) {
            System.out.println("port is: " + parts[1]);
            System.out.print("secure Port is: "+partsSecure[1]);
        } else {
            System.out.println("Invalid argument, please follow: --port=#### --securePort=#### format");
            System.exit(0);
        }

        int num = Integer.parseInt(parts[1]);
        int numSecure = Integer.parseInt(partsSecure[1]);
        new Thread(new Server(num)).start();
        new Thread(new SecureServer(numSecure)).start();

    }
}
