import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

class Server {
    int port=9000;
    String selfAddress="";
    Boolean isServerRunning=true;
    Thread running;
    ServerSocket ss;
    ArrayList<ClientDetails> clientList;

    public static void main(String args[]){
        Server server=new Server();
    }

    String getLocalAddress(){
        String local="";
        try{
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            local=socket.getLocalAddress().getHostAddress();
            socket.close();
        }catch(Exception e){
            System.out.println("Cannot Determine LocalAddress");
            try{
                local=InetAddress.getLocalHost().getHostAddress();
            }
            catch(Exception x){}
        }
        return local;
    }

    Server() {
        try {
            ss = new ServerSocket(port);
            // System.out.println("Server Created with port: " + port);
        } catch (IOException e) {
            System.out.println("Stream error" + e);
            System.exit(0);
        }
        running=new Thread(new Runnable(){
            public void run(){
                while(isServerRunning){
                    try{
                        Socket current=ss.accept();
                        // System.out.println("Accepted on Server");
                        doOperations(current);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        Scanner scan=new Scanner(System.in);
        selfAddress=getLocalAddress();
        if(selfAddress.equals("") || selfAddress.startsWith("127")){
            System.out.print("Enter the IPv4 address of this host: ");
            selfAddress=scan.nextLine();
        }
        clientList=new ArrayList<ClientDetails>();
        running.start();
        System.out.println("Server Started on "+selfAddress);
        scan.close();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                closeServer();
                // System.out.println("Server Close Init");
            }
        }));

    }

    void doOperations(Socket current){
        try{
            ObjectInputStream ois=new ObjectInputStream(current.getInputStream());
            Data data=(Data) ois.readObject();
            ois.close();
            current.close();
            // System.out.println("Data received");
            
            ClientDetails cd=data.getFrom();
            switch(data.getStatus()){
                case 0:
                    try{
                        Socket s=new Socket(data.getTo().getAddress(),port);
                        ObjectOutputStream oos= new ObjectOutputStream(s.getOutputStream());
                        oos.writeObject(data);
                        oos.close();
                        s.close();
                    }
                    catch(Exception x){
                        System.out.println("Cannot Forward Message to: "+data.getTo().getName()+" ("+data.getTo().getAddress()+")");
                    }
                break;
                case 1:
                    if(clientList.contains(cd)){
                        try{
                            Socket s=new Socket(cd.getAddress(),port);
                            ObjectOutputStream oos= new ObjectOutputStream(s.getOutputStream());
                            data.setClientList(clientList);
                            oos.writeObject(data);
                            oos.close();
                            s.close();
                        }
                        catch(Exception x){
                            System.out.println("Error Sending all client details to: "+cd.getName()+"("+cd.getAddress()+")");
                            System.out.println(x.getMessage());
                        }
                        System.out.println("Client Already Exists: "+cd.getName()+" ("+cd.getAddress()+")");
                    }
                    else{
                        clientList.add(cd);
                        data.setClientList(clientList);
                        updateClients(data);
                        System.out.println("New Client Added: "+cd.getName()+" ("+cd.getAddress()+")");
                    }
                    break;
                case 2:
                    clientList.remove(data.getRemoveLocation());
                    data.setClientList(clientList);
                    data.setStatus(1);
                    updateClients(data);
                    System.out.println("Client Removed: "+cd.getName()+" ("+cd.getAddress()+")");
                    break;
                default: System.out.println("Status Issue");
                break;
            }
            // System.out.println("after switch");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    void updateClients(Data data){
        for(ClientDetails current:clientList){
            try{
                Socket s=new Socket(current.getAddress(),port);
                ObjectOutputStream oos= new ObjectOutputStream(s.getOutputStream());
                oos.writeObject(data);
                oos.close();
                s.close();
            }
            catch(Exception e){
                System.out.println("Cannot Reach: "+current.getName()+" ("+current.getAddress()+")");
                System.out.println("Error 2: "+e.getMessage());
            }
        }
    }
    public void closeServer(){
            isServerRunning=false;
            try{
                ss.close();
            }catch(Exception x){
                // System.out.println("Server Closed");
            }
    }
}