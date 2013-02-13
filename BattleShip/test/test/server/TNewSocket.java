package test.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import model.util.EGameMenu;

public class TNewSocket
{
    public static void main(String argsS[]) throws IOException
    {
        for(int loopI=0;loopI<=12;loopI++)
        {
            Socket socket=new Socket("localhost",4000);
            DataInputStream dis=new DataInputStream(socket.getInputStream());
            DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("client "+loopI);
            System.out.println("NPlayer: "+dis.readInt());
            EGameMenu eGameMenu=EGameMenu.valueOf(dis.readUTF());
            switch(eGameMenu)
            {
                case NewGame:
                    System.out.println("I'm player 1");
                    break;
                case SetupGame:
                    System.out.println("I'm player 2");
                    break;
                case ViewGame:
                    System.out.println("I'm a viewer");
                    break;
                case ViewGameFull:
                    System.out.println("Sorry, the server is busy");
                    socket.close();
                    break;
                default:
                    System.out.println("ERROR");
                    socket.close();
                    break;
            }
        }
    }
}
