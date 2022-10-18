import java.io.Serializable;
import java.util.ArrayList;

class ClientDetails implements Serializable{
    String address,name;
    public static final long serialVersionUID=4444444444444L;
    ClientDetails(String name,String address){
        this.address=address;
        this.name=name;
    }
    String getName(){
        return name;
    }
    String getAddress(){
        return address;
    }
    @Override
    public boolean equals(Object obj) {
        boolean check=false;
        if(obj instanceof ClientDetails){
            ClientDetails a=(ClientDetails) obj;
            if( !(a.getName()==null || a.getName().isEmpty()) && (a.getAddress().equals(getAddress()) || a.getName().equals(getName())) ){
                check=true;
            }
        }
        return check;
    }
    @Override
    public String toString() {
        return "["+getName()+" : "+getAddress()+"]";
    }
}

class Data implements Serializable{
    private static final long serialVersionUID=4444444444444L;
    int status;
    //0 msg, 1 new, 2 remove
    String message;
    ClientDetails from,to;
    ArrayList<ClientDetails> clientList;
    int removeLoc;

    void setStatus(int stat){
        status=stat;
    }
    void setMessage(String msg){
        message=msg;
    }
    void setFrom(ClientDetails f){
        from=f;
    }
    void setTo(ClientDetails t){
        to=t;
    }
    int getStatus(){
        return status;
    }
    String getMessage(){
        return message;
    }
    ClientDetails getFrom(){
        return from;
    }
    ClientDetails getTo(){
        return to;
    }
    void setRemoveLocation(int loc){
        removeLoc=loc;
    }
    int getRemoveLocation(){
        return removeLoc;
    }
    void setClientList(ArrayList<ClientDetails> clientList){
        this.clientList=clientList;
    }
    public ArrayList<ClientDetails> getClientList() {
        return clientList;
    }
}