package model.client;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import model.util.BattleShipConstants;
import model.util.EGameMenu;
import model.util.Board;
import model.util.NavalForce;
import model.util.Ship;
import model.util.ShipException;

public class ClientPlayer {

    private Socket socket;
    private String username = "";
    private int nPlayer = -1;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private EGameMenu eGameMenu;
    private Board myBoard = new Board();
    private Board enemyBoard = new Board();
    private NavalForce navalForce = new NavalForce();
    private int currentTurn = 0;
    private String host;
    private int hit = 0;
    private int loc = 0;
    private int signal = -1;

    //Constructor
    public ClientPlayer() {
        this.host = "localhost";
    }

    public ClientPlayer(String host) {
        this.host = host;
    }

    public void connectToServer() {
        try {
            socket = new Socket(host, 4000);
            System.out.println("Connected to the server.");

            toServer = new DataOutputStream(socket.getOutputStream());
            fromServer = new DataInputStream(socket.getInputStream());

            Scanner scan = new Scanner(System.in);
            System.out.print("Please enter your username:");

            username = scan.next();
            toServer.writeUTF(username);
            toServer.flush();
            nPlayer = fromServer.readInt();
            System.out.println("Username sent. You are the player" + nPlayer + ".");



            while (true) {
                this.eGameMenu = eGameMenu.valueOf(this.fromServer.readUTF());
                if (eGameMenu == eGameMenu.NewGame) {
                    System.out.println("Please wait for the second player...");
                    continue;
                } else if (eGameMenu == eGameMenu.SetupGame) {
                    init();
                } else if (eGameMenu == eGameMenu.ViewGame) {
                    System.out.println("This is a viewer.");
                }


            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }

    public void init() {
        System.out.println("\n\nGame start. Please set up your board...");
        inputInitBoard();

        try {
            System.out.println("Please wait for server's response.");
            this.eGameMenu = eGameMenu.valueOf(this.fromServer.readUTF());
        } catch (IOException ex) {
        }
        if (eGameMenu == eGameMenu.Playing) {

            System.out.println("\n\nGame start....");

            while (true) {
                try {
                    signal = fromServer.readInt();
                    if (signal > 2) {
                        break;
                    }
                    currentTurn = (signal + 1) % 2;

                    if (currentTurn == nPlayer % 2) {

                        int location = bombAt();

                        //send the location to the server
                        toServer.writeInt(location);
                        loc = fromServer.readInt();
                        if (loc == BattleShipConstants.GAMEOVER) {
                            break;
                        }
                        hit = fromServer.readInt();

                        try {
                            enemyBoard.changeBoard(hit, loc);
                        } catch (ShipException ex) {
                        }
                        System.out.println("Your enemy's board:");
                        enemyBoard.printBoard();

                    } else {
                        System.out.println("It's not your turn, please wait.");
                        loc = fromServer.readInt();
                        hit = fromServer.readInt();

                        try {
                            myBoard.changeBoard(hit, loc);
                        } catch (ShipException ex) {
                        }
                        System.out.println("Your Board:");
                        myBoard.printBoard();
                    }

                } catch (IOException ex) {
                }
            }

            String winner = "";
            try {
                winner = fromServer.readUTF();
            } catch (IOException ex) {
            }
            if (loc == BattleShipConstants.GAMEOVER) {
                System.out.println("You win! Game Over!");
            } else {
                System.out.println("Game Over!  The winner is " + winner);
            }
            this.closeConnection();
        }


    }

    public int bombAt() {

        System.out.println("Your enemy's board.");
        int location = 0;
        enemyBoard.printBoard();
        System.out.println("It's your turn.");

        int rowNum;
        int columnNum;


        do {
            rowNum = -1;
            columnNum = -1;
            Scanner scan = new Scanner(System.in);
            while (rowNum < 1 || rowNum > 6) {

                System.out.print("Please enter the row number: ");
                try {
                    rowNum = Integer.parseInt(scan.nextLine());
                } catch (Exception e) {
                    System.out.println("Please check your input. Try again.");

                }
            }
            while (columnNum < 1 || columnNum > 6) {

                System.out.print("Please enter the column number: ");
                try {

                    columnNum = Integer.parseInt(scan.nextLine());
                } catch (Exception e) {
                    System.out.println("Please check your input. Try again.");

                }
            }
            location = (rowNum - 1) * 6 + columnNum - 1;

            if (enemyBoard.getBoard(location) != ' ') {
                System.out.println("You have guessed this location before. Please enter a different location.");
                continue;
            } else {
                return location;

            }

        } while (true);



    }

    public void inputInitBoard() {

        HashMap ships = new HashMap();
        ships.put(1, "Submarine");
        ships.put(2, "Destroyer");
        ships.put(3, "Cruiser");
        ships.put(4, "Battleship");

        char[] board = new char[36];
        for (int i = 0; i < 36; i++) {
            board[i] = ' ';
        }



        while (!ships.isEmpty()) {
            Scanner scan = new Scanner(System.in);
            System.out.println("     1 2 3 4 5 6\n  ----------------");
            for (int i = 0; i < 6; i++) {
                System.out.print("  " + (i + 1) + " |");
                for (int j = 0; j < 6; j++) {
                    System.out.print(board[i * 6 + j] + "|");
                }
                System.out.println("\n  ----------------");
            }
            int size = -1;
            int orientation = -1;
            int rowNum = -1;
            int columnNum = -1;

            System.out.println("Ship(s) left:" + ships.toString());
            while (size < 1 || size > 4) {
                System.out.print("Please enter the size of a ship: ");
                try {
                    size = Integer.parseInt(scan.next());

                    if (!ships.containsKey(size)) {
                        size = -1;
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("Please check your input. Try again.");
                    size = -1;
                }

            }
            while (orientation != 0 && orientation != 1) {
                System.out.print("Please select the orientation(0=horizontal,1=vertical): ");
                try {
                    orientation = Integer.parseInt(scan.next());

                } catch (Exception e) {
                    System.out.println("Please check your input. Try again.");

                }
            }
            while (rowNum < 1 || rowNum > 6) {
                System.out.print("Please enter the row number: ");
                try {
                    rowNum = Integer.parseInt(scan.next());
                } catch (Exception e) {
                    System.out.println("Please check your input. Try again.");

                }
            }
            while (columnNum < 1 || columnNum > 6) {
                System.out.print("Please enter the column number: ");
                try {
                    columnNum = Integer.parseInt(scan.next());
                } catch (Exception e) {
                    System.out.println("Please check your input. Try again.");

                }
            }


            int leftTopLocation = (rowNum - 1) * 6 + columnNum - 1;


            try {
                Ship ship = new Ship(size, orientation, leftTopLocation);
                navalForce.add(ship);
                this.toServer.writeInt(size);
                this.toServer.writeInt(orientation);
                this.toServer.writeInt(leftTopLocation);
                System.out.println("Ship added.");

                if (orientation == 0) {
                    for (int i = leftTopLocation; i < leftTopLocation + size; i++) {
                        board[i] = shipChar(size);
                    }
                } else if (orientation == 1) {
                    for (int i = leftTopLocation; i < leftTopLocation + size * 6; i += 6) {
                        board[i] = shipChar(size);
                    }
                }
                ships.remove(size);
            } catch (ShipException ex) {
                System.out.println("Ship overlaps");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        System.out.println("     1 2 3 4 5 6\n  ----------------");
        for (int i = 0; i < 6; i++) {
            System.out.print("  " + (i + 1) + " |");
            for (int j = 0; j < 6; j++) {
                System.out.print(board[i * 6 + j] + "|");
            }
            System.out.println("\n  ----------------");
        }

        myBoard.setBoard(board);

        for (int i = 0; i < navalForce.size(); i++) {
            Ship ship = (Ship) navalForce.get(i);
        }

    }

    static char shipChar(int size) {
        if (size == 1) {
            return 's';
        } else if (size == 2) {
            return 'd';
        } else if (size == 3) {
            return 'c';
        } else if (size == 4) {
            return 'b';
        } else {
            return ' ';
        }
    }

    //Methods
    //Tells the server if this client disconnect
    public void closeConnection() {
        String response = String.format("%s has terminated\n", this.username);
        try {
            this.fromServer.close();
            this.toServer.close();
            this.socket.close();
        } catch (IOException ioEx) {
            System.out.println("");
        } catch (Exception ex) {
            System.out.println("");
        }
    }
}
