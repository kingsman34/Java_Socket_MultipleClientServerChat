import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>() ;
    public Socket socket ;
    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    public String clientUserName ;

    public ClientHandler(Socket socket){
        try{
            this.socket = socket ;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) ;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
            this.clientUserName = bufferedReader.readLine() ;
            clientHandlers.add(this) ;
            broadcastMessage("Server : " + clientUserName + " has entered the chat ") ;

        }catch(IOException e){
            closeEveryting(socket,bufferedReader,bufferedWriter) ;

        }
    }
    @Override
    public void run() {
        String messageFromClient ;
        while(socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine() ;
                if(messageFromClient.equals("logout")){
                    closeEveryting(socket,bufferedReader,bufferedWriter);
                    break ;
                }
                if(messageFromClient.startsWith("P")){
                    String[] messagePartition = messageFromClient.split("&") ;
                    String sendTo = messagePartition[1] ;
                    StringBuilder privateMessage = new StringBuilder(" Priavte Message from "+clientUserName+": ");
                    for(int i=2 ; i<messagePartition.length ; i++){
                        privateMessage.append(messagePartition[i]).append(" ") ;
                    }
                    privateMessage.append("\n") ;
                    secretMessage(sendTo,privateMessage.toString());
                }else{
                    String message = clientUserName+" : "+messageFromClient ;
                    broadcastMessage(message);
                }

            }catch (IOException e){
                closeEveryting(socket,bufferedReader,bufferedWriter) ;
                break ;
            }
        }
    }
    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler: clientHandlers){
            try {
                if(!clientHandler.clientUserName.equals(clientUserName)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    System.out.println(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEveryting(socket,bufferedReader,bufferedWriter) ;

            }
        }
    }
    public void removeClientHandler(){
        clientHandlers.remove(this) ;
        broadcastMessage("Server " +clientUserName+" has Left the chat ");

    }
    public void closeEveryting(Socket socket,BufferedReader bufferedReader ,BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();

        }
    }
    public void secretMessage(String sendto,String message) {
        for(ClientHandler clientHandler:clientHandlers){
            if(clientHandler.clientUserName.equals(sendto)){
                try{
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.flush();

                }catch(IOException e){
                    e.printStackTrace();
                    closeEveryting(socket,bufferedReader,bufferedWriter);
                }
            }
        }

    }
}
