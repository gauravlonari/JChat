import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

class Client extends JFrame {
    int port=9000;
    JTextArea msgFrame;
    JButton sendBtn;
    JTextField msg;
    ClientDetails clientDetails;
    String serverAddress;
    JComboBox<String> clients;
    JScrollPane scrollpane;

    Boolean isReceiverRunning=true;
    transient Thread receiving;
    transient ServerSocket rs;
    ArrayList<ClientDetails> clientList;
    boolean selfChat=false;

    public static void main(String[] args) {
        new Client();
    }

    String getLocalAddress(){
        String local="";
        try{
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            local=socket.getLocalAddress().getHostAddress();
            socket.close();
        }catch(IOException e){
            System.out.println("Cannot Determine Local Address");
            try{
                local=InetAddress.getLocalHost().getHostAddress();
            }
            catch(Exception x){
                //
            }
        }
        return local;
    }
    Client() {
        Scanner scan=new Scanner(System.in);
        
        String selfAddress=getLocalAddress();
        
        if(selfAddress.equals("") || selfAddress.startsWith("127")){
            System.out.println("Enter the IPv4 address of this host: ");
            selfAddress=scan.nextLine();
        }
        System.out.println("This Host Address is: "+selfAddress);

        System.out.print("Enter the Client Name: ");
        clientDetails=new ClientDetails(scan.nextLine(),selfAddress);
        
        System.out.print("Enter the Server Address: ");
        serverAddress=scan.nextLine();

        try{
            System.out.println(InetAddress.getByName(serverAddress).getHostAddress()+" is Reachable");
        }
        catch(Exception e){
            System.out.println("Server Address Could not be accessed. Make sure you are connected to Internet.");
            System.out.println(e.getMessage());
            System.exit(0);
        }
        receiving=new Thread(new Runnable(){
            public void run(){
                while(isReceiverRunning){
                    try{
                        Socket current=rs.accept();
                        // System.out.println("Accepted on Client");
                        doOperations(current);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        clientList=new ArrayList<ClientDetails>();
        if(initilizeConnection()){
            initilizeWindow();
            try{
                rs=new ServerSocket(port);
            }
            catch(Exception e){
                System.out.println("Client Receiving cant be initilized.");
                System.out.println(e.getMessage());
                System.exit(0);
            }
            receiving.start();
        }
        scan.close();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                closeClient();
                // System.out.println("Client Close Init");
            }
        }));

    }

    void doOperations(Socket current){
        try{
            ObjectInputStream ois=new ObjectInputStream(current.getInputStream());
            Data data=(Data) ois.readObject();
            ois.close();
            current.close();
            if(data.getStatus()==1){
                clientList=data.getClientList();
                updateClients();
            }
            else if(data.getStatus()==0){
                ClientDetails from=data.getFrom();
                addMsg("From "+from.getName()+": "+data.getMessage());
            }
        }catch(Exception e){
            System.out.println("Error 1: "+e.getMessage());
        }
    }
    void updateClients(){
        clients.removeAllItems();
        for(ClientDetails c:clientList){

            if(c.getName().equals(clientDetails.getName()) && !selfChat)
                continue;
            
                clients.addItem(c.getName());
        }
    }
    boolean initilizeConnection(){
        try{
            Data d=new Data();
            d.setStatus(1);
            d.setFrom(clientDetails);
            Socket s=new Socket(serverAddress,port);
            new ObjectOutputStream(s.getOutputStream()).writeObject(d);
            s.close();
            return true;
        }catch(Exception e){
            System.out.println("Error while initilizing Client: "+e.getMessage());
            return false;
        }
    }
    void initilizeWindow(){
        Font f = new Font("Inter", Font.PLAIN, 15);
        msgFrame = new JTextArea();
        msgFrame.setEditable(false);
        msgFrame.setFont(f);
        msgFrame.setLineWrap(true);
        scrollpane=new JScrollPane(msgFrame);
        
        
        
        msg = new JTextField("Enter your Message here", 30);
        msg.setFont(f);
        
        msg.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (msg.getText().equals("Enter your Message here"))
                    msg.setText("");
            }
            
            public void focusLost(FocusEvent e) {
                if (msg.getText().equals(""))
                    msg.setText("Enter your Message here");
            }
        });

        addWindowListener((new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                msg.requestFocus();
            }
            
            @Override
            public void windowGainedFocus(WindowEvent e) {
                msg.requestFocus();
            }

            // @Override
            // public void windowClosing(WindowEvent e) {
            //     super.windowClosing(e);
            //     closeClient();
            // }
        }));
        
        msg.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    sendBtn.doClick();
                }
            }
            
            public void keyTyped(KeyEvent e) {
                //not required
            }
            
            public void keyReleased(KeyEvent e) {
                //not required
            }
        });
        
        JPanel functionalPanel=new JPanel(new GridLayout(2,1,10,0));
        clients=new JComboBox<String>();
        clients.setFont(f);
        sendBtn = new JButton("Send");
        JPanel buttonsPanel=new JPanel(new GridLayout(1,2,20,0));
        buttonsPanel.add(clients);
        buttonsPanel.add(sendBtn);
        functionalPanel.add(msg);
        functionalPanel.add(buttonsPanel);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.fill=GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.ipady=600;
        add(scrollpane,gbc);
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.gridx=0;
        gbc.gridy=1;
        gbc.ipady=0;
        add(functionalPanel,gbc);
        
        sendBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = msg.getText().trim();
                if (!text.isEmpty() && !text.equals("Enter your Message here") && clientList.size()>0) {
                    msg.setText("");
                    sendMsg(text,clients.getSelectedIndex());
                }
            }
        });
        setSize(500, 750);
        setTitle(clientDetails.getName());
        setResizable(true);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    void sendMsg(String text,int toIndex){
        ClientDetails c;
        if(!selfChat && toIndex>=clientList.indexOf(clientDetails))
            c=clientList.get(toIndex+1);
        else
            c=clientList.get(toIndex);
        try{
            Data d=new Data();
            d.setStatus(0);
            d.setFrom(clientDetails);
            d.setTo(c);
            d.setMessage(text);
            Socket s=new Socket(serverAddress,port);
            new ObjectOutputStream(s.getOutputStream()).writeObject(d);
            addMsg("Sent to "+c.getName()+": "+text);
            s.close();
        }
        catch(Exception e){
            System.out.println("Cannot Send Message: "+e.getMessage());
            addMsg("Error while sending to "+c.getName()+": "+text);
        }
    }
    void addMsg(String text){
        msgFrame.setText(msgFrame.getText()+text+"\n");
    }

    public void closeClient(){
        try{
            int loc=clientList.indexOf(clientDetails);
            if(loc!=-1){
                Data d=new Data();
                d.setRemoveLocation(loc);
                d.setFrom(clientDetails);
                d.setStatus(2);
                Socket s=new Socket(serverAddress,port);
                new ObjectOutputStream(s.getOutputStream()).writeObject(d);
                s.close();
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
        finally{
            isReceiverRunning=false;
            try{
                rs.close();
            }catch(Exception x){
                // System.out.println("Client Closed");
            }
        }
    }
}