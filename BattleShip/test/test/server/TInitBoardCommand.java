package test.server;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import model.util.EGameMenu;

import model.util.Ship;

public class TInitBoardCommand extends Thread
{

    DataOutputStream dos;
    DataInputStream dis;
    Socket socket;
    String username = "Zuko";
    EGameMenu eGamePlay;
    int nPlayer;

    public TInitBoardCommand()
    {
    }

    public static void main(String args[])
    {
        new TInitBoardCommand().start();
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("pre-connect");
            socket = new Socket("localhost", 4000);
            System.out.println("connected");

            dos = new DataOutputStream(socket.getOutputStream());

            System.out.println("Write...");
            dos.writeUTF(username);
            dos.flush();
            System.out.println("username sent");

            dis = new DataInputStream(socket.getInputStream());
            this.nPlayer=this.dis.readInt();
            String test = this.dis.readUTF();
            System.out.println(test);
            this.eGamePlay = eGamePlay.valueOf(test);
            if (eGamePlay == EGameMenu.NewGame)
            {
                System.out.println("Wait for your friend...");
            }

            while (eGamePlay == EGameMenu.NewGame)
            {
                this.eGamePlay = eGamePlay.valueOf(this.dis.readUTF());
            }
            System.out.println(this.eGamePlay.name());
            System.out.println(String.format("Set up %s's board...", this.username));

            //Ship1
            dos.writeInt(4);
            dos.writeInt(1);
            dos.writeInt(1);
            System.out.println("ship1 sent");

            //Ship2
            dos.writeInt(3);
            dos.writeInt(1);
            dos.writeInt(2);
            System.out.println("ship2 sent");

            //Ship3
            dos.writeUTF("abc");
            //dos.flush();
            dos.writeInt(2);
            dos.writeInt(1);
            dos.writeInt(3);
            System.out.println("ship3 sent");

            //Ship3
            dos.writeInt(2);
            dos.writeInt(1);
            dos.writeInt(3);
            System.out.println("ship3 sent");

            //Ship4
            dos.writeInt(1);
            dos.writeInt(1);
            dos.writeInt(4);
            System.out.println("ship4 sent");

            dos.flush();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
