package model.server;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ViewerHandlingThread {

    private ObjectOutputStream objectOutputStream;

    public ViewerHandlingThread(Socket socket, String username) throws SocketException {
    }

    public void run() {
    }
}
