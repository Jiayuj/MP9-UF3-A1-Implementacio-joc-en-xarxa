import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import java.lang.*;
import java.net.*;
import java.util.Scanner;

class ChessDemo extends Panel implements ActionListener {

    InetAddress serverIP;
    int serverPort;
    DatagramSocket socket;
    boolean player2, turn;

    char whoTurn = 'O';  //人—O,计算机--X
    Button b[] = new Button[9];  //9个按钮
    StringBuffer chess = new StringBuffer("KKKKKKKKK");
    //将九宫格用一位数组来表示,用K字符表示空位置

    public ChessDemo(String host, int port) throws IOException {
        serverIP = InetAddress.getByName(host);
        serverPort = port;
        socket = new DatagramSocket();

        setLayout(new GridLayout(3, 3, 3, 3));
        for (int i = 0; i < 9; i++) {
            b[i] = new Button("");
            add(b[i]);
            b[i].setActionCommand(String.valueOf(i));
            b[i].addActionListener(this);
        }
        System.out.println("You name");
        setPlayer(new Scanner(System.in).nextLine());
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (player2 && turn) {
                Button me = (Button) (e.getSource());//自己
                if (!me.getLabel().equals("")) //不允许在已有棋子位置下棋
                    return;
                me.setLabel("" + whoTurn); //标记下棋
                int row = Integer.parseInt(me.getActionCommand()); //求位置
                chess.setCharAt(row, whoTurn); //记录下棋
                send(row);
                gameOver();  //判游戏是否结束
                whoTurn = (whoTurn == 'O') ? 'X' : 'O'; //轮换玩家
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void gameOver() {
        if (isWin(whoTurn)) { //判是否取胜
            JOptionPane.showMessageDialog(null, whoTurn + " win!");
            quit();
        } else if (isFull()) { //判是否下满格子
            JOptionPane.showMessageDialog(null, "game is over!");
            quit();
        }
    }
    public boolean isWin(char who) {
        String s3 = "" + who + who + who;
        String sum;
        String sum1;
        String sum2;//用来拼接一个方向的棋子标识
        for (int k = 0; k < 3; k++) {
            sum1 = "" + chess.charAt(k) + chess.charAt(k + 3) + chess.charAt(k + 6);//垂直方向
            sum2 = "" + chess.charAt(k * 3 + 0) + chess.charAt(k * 3 + 1) + chess.charAt(k * 3 + 2);//水平方向
            if (sum1.equals(s3) || sum2.equals(s3)) {
                return true;
            } else {
                sum1 = "";
                continue;
            }
        }
        sum = "" + chess.charAt(0) + chess.charAt(4) + chess.charAt(8); //正对角线
        if (sum.equals(s3)) return true;
        sum = "" + chess.charAt(2) + chess.charAt(4) + chess.charAt(6); //反对角线
        if (sum.equals(s3)) return true;
        return false;
    }

    public boolean isFull() {  //判是否棋盘下满了
        return chess.toString().indexOf("K") == -1;
    }
    public void quit(){
        try {
            send(99);
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        Frame f = new Frame();
        ChessDemo chessDemo = new ChessDemo("127.0.0.1",8880);
        f.add(chessDemo);
        f.setSize(300, 300);
        f.setVisible(true);

        RecvDataChes rd = new RecvDataChes();
        rd.recvD(chessDemo);
        Thread recvThread = new Thread(rd);
        recvThread.start();
    }

    public void send(int row) throws IOException {
        byte [] sendingData;
        String w = String.valueOf(row);
        //a l'inici
        sendingData = w.getBytes();
        //el servidor atén el port indefinidament
        DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, serverIP, serverPort);
        //enviament de la resposta
        socket.send(packet);
        turn=false;
        System.out.println("wait player set");
    }

    private void setPlayer(String nom) throws IOException {
        byte [] sendingData;
        byte [] receivedData = new byte[1024];
        //a l'inici
        sendingData = nom.getBytes();
        //el servidor atén el port indefinidament
        DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, serverIP, serverPort);
        //enviament de la resposta
        socket.send(packet);
        //creació del paquet per rebre les dades
        packet = new DatagramPacket(receivedData, 1024);
        //espera de les dades
        socket.receive(packet);
        System.out.println(new String(packet.getData(),0, packet.getData().length));
        //processament de les dades rebudes i obtenció de la resposta

        System.out.println("Wait .... player");
        System.out.println(getWaitPlayeStatus());
    }
    private String getWaitPlayeStatus() throws IOException {
        byte [] receivedData = new byte[1024];
        DatagramPacket packet = new DatagramPacket(receivedData, 1024);
        //espera de les dades
        socket.receive(packet);
        player2=true;
        turn=true;
        return  (new String(receivedData,0, receivedData.length));
    }
}

class RecvDataChes implements Runnable {
    private ChessDemo chessDemo = null;

    public void recvD(ChessDemo argDs) {
        this.chessDemo = argDs;
    }

    public void run() {
        try {
            while (true) {
                byte [] receivedData = new byte[1024];
                DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length, chessDemo.serverIP, chessDemo.serverPort);
                //espera de les dades
                chessDemo.socket.receive(packet);

                String w = new String(packet.getData(),0,packet.getData().length);
                System.out.println();
                int x = Integer.parseInt(w.trim());
                chessDemo.chess.setCharAt(x, chessDemo.whoTurn);
                chessDemo.b[x].setLabel(String.valueOf(chessDemo.whoTurn));
                chessDemo.gameOver();
                chessDemo.whoTurn = (chessDemo.whoTurn == 'O') ? 'X' : 'O';
                chessDemo.turn =true;
                System.out.println("You Turn");

            }
        } catch (Exception e) {
        }
    }
}
