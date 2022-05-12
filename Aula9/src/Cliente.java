import java.io.*;
import java.net.*;

class Client {
    public static void main(String[] args){
        try{
            if(args.length<3)
                System.exit(1);
            
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            Socket s = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream());
            String nome = args[2];
            new Thread(() -> {
                try {
                    while(true){
                        String res = System.console().readLine();
                        out.println(nome + '>' +' '+ res);
                        out.flush();
                    }

                } catch (Exception e) {}  
            }).start();

            new Thread(() -> {
                try {
                    while(true){
                        String res = in.readLine();
                        out.println(res);
                        System.out.println(res);                       
                    }

                } catch (Exception e) {}  
            }).start();
        }catch (Exception e){}
    }
}