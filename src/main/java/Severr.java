import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Severr {
    DatagramSocket socket;
    boolean player1, player2;
    InetAddress player1IP,player2IP;
    int player1PORT,player2PORT,quit;
    String p1nom,p2nom;

    public void init(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public void runServer() throws IOException {
        setplayer();
        do {
            jugar();
        }while (true);
    }
    private byte[] processData(byte[] data, int length) throws IOException {
        //procés diferent per cada aplicació
        String s = new String(data,0,length);
        if (s.equals("99")){
            quit++;
            if (quit==2) {
                player1 = false;
                player2 = false;
                runServer();
            }
        }
        return s.getBytes();
    }

    private void setplayer() throws IOException {
        byte [] receivingData = new byte[1024];
        byte [] sendingData;
        DatagramPacket packet = null;

        while(!player1 || !player2) {
            packet = new DatagramPacket(receivingData,1024);
            StringBuilder w = new StringBuilder("Welcome ");
            socket.receive(packet);
            if (!player1) {
                player1IP = packet.getAddress();
                player1PORT = packet.getPort();
                p1nom=new String(packet.getData(),0,packet.getData().length);
                player1 = true;
                System.out.println(packet.getAddress()+ " " + packet.getPort());
                System.out.println("Player1 " + p1nom);
                w.append(p1nom);
                sendingData = w.toString().getBytes();
                packet = new DatagramPacket(sendingData, sendingData.length, player1IP, player1PORT);
                socket.send(packet);
            } else if (!player2){
                player2IP = packet.getAddress();
                player2PORT = packet.getPort();
                p2nom=new String(packet.getData(),0,packet.getData().length);
                player2 = true;
                System.out.println(packet.getAddress()+ " " + packet.getPort());
                System.out.println("Player2 " + p2nom);
                w.append(p2nom);
                sendingData = w.toString().getBytes();
                packet = new DatagramPacket(sendingData, sendingData.length, player2IP, player2PORT);
                socket.send(packet);
            }
        }
        sendingData = "play".getBytes();
        packet = new DatagramPacket(sendingData, sendingData.length, player1IP, player1PORT);
        socket.send(packet);
        packet = new DatagramPacket(sendingData, sendingData.length, player2IP, player2PORT);
        socket.send(packet);
    }
    public void jugar() throws IOException {
        byte [] receivingData = new byte[1024];
        byte [] sendingData;
        InetAddress clientIP;
        int clientPort;

        DatagramPacket packet = new DatagramPacket(receivingData,1024);
        socket.receive(packet);
        sendingData = processData(packet.getData(),packet.getLength());
        if (player1 && player2){
            //Llegim el port i l'adreça del client per on se li ha d'enviar la resposta
            clientIP = packet.getAddress();
            clientPort = packet.getPort();
            if (clientIP.equals(player1IP) && clientPort == player1PORT){
                clientPort =player2PORT;
                clientIP =player2IP;
                System.out.println("RECEIVED " + p1nom+ " " + packet.getPort());
            }else{
                clientPort = player1PORT;
                clientIP = player1IP;
                System.out.println("RECEIVED " + p2nom+ " " + packet.getPort());
            }
            packet = new DatagramPacket(sendingData,sendingData.length,clientIP,clientPort);
            socket.send(packet);
            System.out.println(new String(sendingData,0, sendingData.length));
        }
    }

    public static void main(String[] args) {
        Severr severr = new Severr();
        try {
            severr.init(8880);
            severr.runServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
