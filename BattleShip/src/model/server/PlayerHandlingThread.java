package model.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.util.BattleShipConstants;
import model.util.Board;
import model.util.EGameMenu;
import model.util.NavalForce;
import model.util.Ship;
import model.util.ShipException;

public class PlayerHandlingThread extends Thread {

    //private fields
    private Server server;
    private Socket socket;
    private String username;
    private Board boards[] = new Board[2];
    private int nPlayer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    //private static fields
    private static Integer myTurn = 0;
    private static Boolean isGameOver = false;
    private static String winner = "";
    private static int NAVAL_FORCE_PLAYERS = 0;
    private static int hit;
    private static int loc;

    //constructor
    public PlayerHandlingThread(Server server, Socket socket, int nPlayer) {
        this.socket = socket;
        this.nPlayer = nPlayer;
        this.boards = server.getBoards();
        this.username = server.getPlayerName()[nPlayer];
        this.server = server;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            try {
                sleep(100);
            } catch (InterruptedException ex1) {
                Logger.getLogger(PlayerHandlingThread.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    public OutputStream getSocketOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    //Methods
    @Override
    public void run() {


        this.initBoard();

        //game starting
        while (!isGameOver) {
            try {
                dataOutputStream.writeInt(myTurn);

                if (myTurn == nPlayer) {
                    System.out.println("It's " + username + "'s turn");
                    loc = dataInputStream.readInt();
                    System.out.println(username + " is bombing at location " + loc);
                    hit = bombing(loc);
                    isWin();
                    if (myTurn == BattleShipConstants.GAMEOVER) {
                        break;
                    }
                    myTurn = (nPlayer + 1) % 2;

                } else {
                    while (true) {
                        if (myTurn != nPlayer) {
                            if (myTurn != BattleShipConstants.GAMEOVER) {
                                try {
                                    this.sleep(100);
                                } catch (InterruptedException ex) {
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                }

                dataOutputStream.writeInt(loc);
                dataOutputStream.writeInt(hit);

            } catch (IOException ex) {
            }

        }

        try {
            dataOutputStream.writeInt(myTurn);
            dataOutputStream.writeUTF(server.getPlayerName()[(nPlayer + 1) % 2]);
            dataOutputStream.flush();
        } catch (IOException ex) {
            try {
                sleep(100);
            } catch (InterruptedException ex1) {
            }
        }

    }

    public void initBoard() {

        for (int i = 0; i < 2; i++) {
            this.server.navalForces[i] = new NavalForce();
        }

        while (this.server.navalForces[nPlayer].size() < 4) {
            //loops until all ships are inside navalForce
            try {
                System.out.println(String.format("%s is placing ship%d", this.username, server.navalForces[nPlayer].size() + 1));

                int size = this.dataInputStream.readInt();
                int orientation = this.dataInputStream.readInt();
                int topLeftPosition = this.dataInputStream.readInt();
                Ship ship = new Ship(size, orientation, topLeftPosition);
                this.server.navalForces[nPlayer].add(ship);
                System.out.println(String.format("%s's ship%d added into NavalForce", this.username, server.navalForces[nPlayer].size()));

                this.NavalForceToBoard(ship);
            } catch (IOException ioEx) {
                try {
                    sleep(100);
                } catch (InterruptedException ex1) {
                }
            } catch (ShipException sx) {
                System.out.println("You try to put the ship in non-empty location");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }

        System.out.println(String.format("%s has add all ships into NavalForce.", this.username));
        this.printNavalForce();
        NAVAL_FORCE_PLAYERS++;
        if (NAVAL_FORCE_PLAYERS == 1) {
            System.out.println(String.format("%s is waiting for the enemy...", this.username));
        }

        while (NAVAL_FORCE_PLAYERS < 2) {
            try {
                this.sleep(100); //Waiting for other player finishing setting-up the board
            } catch (InterruptedException ex) {
                Logger.getLogger(PlayerHandlingThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            dataOutputStream.writeUTF(EGameMenu.Playing.name());
        } catch (IOException ex) {
            try {
                sleep(100);
            } catch (InterruptedException ex1) {
            }
        }

    }

    private void NavalForceToBoard(Ship ship) {
        char type = 0;
        switch (ship.size()) {
            case 1:
                type = 's';
                break;
            case 2:
                type = 'd';
                break;
            case 3:
                type = 'c';
                break;
            case 4:
                type = 'b';
                break;
        }
        for (Object location : ship.getAllLocs()) {
            this.boards[nPlayer].setBoard(type, (Integer) location);
        }
    }

    private void printNavalForce() {
        System.out.println("=====================");
        for (int i = 0; i < server.navalForces[nPlayer].size(); i++) {
            Ship ship = (Ship) server.navalForces[nPlayer].get(i);
            System.out.println(ship.toString());
        }

        System.out.println(String.format("======%s's NavalForce======", username));
        System.out.println(server.getPlayerName()[nPlayer] + "'s board");
        boards[nPlayer].printBoard();
    }

    private int bombing(int location) {
        char bomb = ' ';
        int enemy = (myTurn + 1) % 2;

        System.out.println("enemy navalForce remaining: " + server.navalForces[enemy].getTotalRemainingLocs());

        System.out.println(server.getPlayerName()[enemy] + "'s board");



        Ship ship = this.server.navalForces[enemy].getShipHit(location);
        if (ship != null) {
            Ship shipDestroyed = this.server.navalForces[enemy].getShipDestroyed(location);
            if (shipDestroyed != null) {
                switch (shipDestroyed.size()) {
                    case 1:
                        bomb = 'S';
                        break;
                    case 2:
                        bomb = 'D';
                        break;
                    case 3:
                        bomb = 'C';
                        break;
                    case 4:
                        bomb = 'B';
                        break;
                }
                System.out.println("The destroyed ship is " + BattleShipConstants.shipTypes[shipDestroyed.size() - 1]);
                for (Object loc : shipDestroyed.getAllLocs()) {
                    boards[enemy].setBoard(bomb, (Integer) loc);
                }
                shipDestroyed.destroy(location);
                boards[enemy].printBoard();
                return server.navalForces[enemy].shipToInteger(ship);
            } else {
                boards[enemy].setBoard('X', (Integer) location);
                boards[enemy].printBoard();
                ship.destroy(location);
                return BattleShipConstants.SHIP_HIT;
            }
        } else {
            boards[enemy].setBoard('/', (Integer) location);
            boards[enemy].printBoard();
            return BattleShipConstants.SHIP_MISS;
        }

    }

    private void isWin() {
        winner = server.getPlayerName()[myTurn];
        if (this.server.navalForces[(nPlayer + 1) % 2].getTotalRemainingLocs() == 0) {
            myTurn = BattleShipConstants.GAMEOVER;
            System.out.println(winner + " Win! ");
            this.isGameOver = true;
        }
    }
}
