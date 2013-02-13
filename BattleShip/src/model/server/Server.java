package model.server;

import model.util.EGameMenu;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import model.util.Board;
import model.util.NavalForce;

public class Server {

    private ServerSocket serverSocket;
    private Socket socket;
    private EGameMenu eGameMenu = EGameMenu.NewGame;
    private int nPlayer = 0;
    private String[] playerName = new String[10];
    private PlayerHandlingThread playerHandlingThread[] = new PlayerHandlingThread[2];
    private Board[] boards = new Board[2];
    public NavalForce[] navalForces = new NavalForce[2];
    private DataOutputStream toClient;
    private DataInputStream fromClient;

    public Server() {

        //initialize the two Boards and two NavalForces
        for (int i = 0; i < 2; i++) {
            boards[i] = new Board();
            navalForces[i] = new NavalForce();
        }

        //the server wait for connection and send
        try {
            this.serverSocket = new ServerSocket(4000);
            System.out.println("BattleShip Server is running ...");

            while (true) {
                this.socket = serverSocket.accept();
                nPlayer++;
                fromClient = new DataInputStream(this.socket.getInputStream());
                toClient = new DataOutputStream(socket.getOutputStream());

                //get username from client side
                try {
                    playerName[nPlayer - 1] = fromClient.readUTF();
                    System.out.println(playerName[nPlayer - 1] + " accepted");
                    toClient.writeInt(nPlayer);
                } catch (IOException ex) {
                } catch (Exception x) {
                    System.out.println("Server is full.\nNo more clients can connect to the server.");
                }


                if (nPlayer <= 2) {

                    setupGame();
                } else if (nPlayer > 2 && nPlayer < 10) {
                    this.eGameMenu = EGameMenu.ViewGame;
                    toClient.writeUTF(this.eGameMenu.name());
                    ViewerHandlingThread vht = new ViewerHandlingThread(socket, playerName[nPlayer - 1]);
                    System.out.println(playerName[nPlayer - 1] + " is a viewer.");
                } else {
                    this.eGameMenu = EGameMenu.ViewGameFull;
                    toClient.writeUTF(this.eGameMenu.name());
                    System.out.println("Server is busy");
                }
            }
        } catch (IOException ioEx) {
            System.err.println(ioEx);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public String[] getPlayerName() {
        return playerName;
    }

    private void setupGame() {
        try {
            playerHandlingThread[nPlayer - 1] = new PlayerHandlingThread(this, this.socket, nPlayer - 1);
            System.out.println("PlayerHandlingThread " + nPlayer + " created");

            if (nPlayer == 1) {
                this.eGameMenu = EGameMenu.NewGame;
                toClient.writeUTF(eGameMenu.name());
                //toClient.flush();
            }
            if (nPlayer == 2) {
                this.eGameMenu = EGameMenu.SetupGame;
                for (int i = 0; i < 2; i++) {
                    DataOutputStream dos = new DataOutputStream(playerHandlingThread[i].getSocketOutputStream());
                    dos.writeUTF(this.eGameMenu.name());
                    dos.flush();
                    playerHandlingThread[i].start();
                }
            }

        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //

    private void playing() {
        //the game is full, interested being viewer???
    }

    private void newGame() {
        //if both of them created, then eGamePlay change into Playing
    }

    private void resumeGame() {
        //validate username and resume the gameplay
    }

    public Board[] getBoards() {
        return boards;
    }
}
