
import model.client.ClientPlayer;
import model.util.EGameMenu;

public class MainClient {

    public static void main(String[] args) {
        EGameMenu eGameMenu = EGameMenu.NewGame;
        ClientPlayer client;
        if (args.length == 1) {
            client = new ClientPlayer(args[0]);
        } else {
            client = new ClientPlayer();
        }
        client.connectToServer();
        if (eGameMenu == EGameMenu.SetupGame) {
            client.init();
        } else if (eGameMenu == EGameMenu.ViewGame) {
            System.out.println("This is a viewer.");
        } else if (eGameMenu == EGameMenu.ResumeGame) {
            System.out.println("Please wait to resume the game.");
        }
    }
}
