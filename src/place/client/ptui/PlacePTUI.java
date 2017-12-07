package place.client.ptui;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.client.model.ClientModel;
import place.client.network.NetworkClient;

import java.io.PrintWriter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

/**
 * The PlacePTUI application is a plain text Place UI.
 */
public class PlacePTUI extends ConsoleApplication implements Observer {
    /**
     * The model for place
     */
    private ClientModel model;

    /**
     * Connection to network interface to server
     */
    private NetworkClient serverConn;

    /**
     * Scanner to read the user's input
     */
    private Scanner userIn;

    /**
     * Where to send output for the user to see
     */
    private PrintWriter userOut;


    public PlacePTUI() {
    }

    public synchronized void go(Scanner userIn, PrintWriter userOut) {
        this.userIn = userIn;
        this.userOut = userOut;
        this.model.addObserver(this);
        this.refresh();
        while (true) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update(Observable t, Object o) {
        assert t == this.model : "Update from non-model Observable";
        this.refresh();
    }

    public void refresh() {
        if(model.getBoard() == null){
            return;
        }
        try {
            for (PlaceTile[] row : model.getBoard()) {
                for (PlaceTile tile : row) {
                    userOut.print(tile.getColor());
                }
                userOut.println();
            }
            userOut.print("Enter your move [row] [col] [color]: ");
            userOut.flush();
            PlaceColor color = PlaceColor.WHITE;
            String[] response = userIn.nextLine().split(" "); //TODO: will this get interrupted if the board updates again?
            for(PlaceColor c: PlaceColor.values()){
                if(c.getNumber() == Integer.parseInt(response[2]))
                    color = c;
            }
            serverConn.sendMove(new PlaceTile(Integer.parseInt(response[0]), Integer.parseInt(response[1]), getArguments().get(2), color));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        try {
            List<String> args = super.getArguments();
            String host = args.get(0);
            int port = Integer.parseInt(args.get(1));
            String username = args.get(2);

            this.model = new ClientModel();
            this.serverConn = new NetworkClient(host, port, username, model);
        } catch (PlaceException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConn.close();
    }


    public static void main(String[] args) {
        ConsoleApplication.launch(PlacePTUI.class, args);
    }
}
