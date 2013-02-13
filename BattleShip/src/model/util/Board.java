package model.util;

public class Board implements BattleShipConstants {

    private char[] board = new char[36];

    public Board() {
        for (int i = 0; i < 36; i++) {
            board[i] = ' ';
        }
    }

    public char getBoard(int loc) {
        return board[loc];
    }

    public void setBoard(char shipType, int loc) {
        board[loc] = shipType;
    }

    public void setBoard(char[] board) {
        this.board = board;
    }

    public void changeBoard(int hit, int loc) throws ShipException {
        if (hit == SHIP_HIT) {
            board[loc] = 'X';
        } else if (hit == SHIP_MISS) {
            board[loc] = '/';
        } else if (hit >= 1000) {
            int size = hit / 1000;
            int orientation = (hit - size * 1000) / 100;
            int topLeftPosition = hit - size * 1000 - orientation * 100;
            char shipType = ' ';
            switch (size) {
                case 1:
                    shipType = 'S';
                    break;
                case 2:
                    shipType = 'D';
                    break;
                case 3:
                    shipType = 'C';
                    break;
                case 4:
                    shipType = 'B';
                    break;
            }
            int inc = orientation == 0 ? 1 : 6;
//      if (orientation == 1 && topLeftPosition/6 + size > 6 ||
//          orientation == 0 && topLeftPosition%6 + size > 6)
//             throw new ShipException("Invalid Dimensions");
            for (int i = 0; i < size; i++) {
                board[topLeftPosition + inc * i] = shipType;
            }
        }
    }

    public void printBoard() {
        System.out.println("     1 2 3 4 5 6\n  ----------------");
        for (int i = 0; i < 6; i++) {
            System.out.print("  " + (i + 1) + " |");
            for (int j = 0; j < 6; j++) {
                System.out.print(board[i * 6 + j] + "|");
            }
            System.out.println("\n  ----------------");
        }
    }
}
