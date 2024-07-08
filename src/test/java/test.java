import org.example.Cmc;
import org.example.Crypto;

import static org.example.Settings.ALERT_PERCENT_SOL;
import static org.example.Settings.ALERT_PERCENT_TRUNK;

public class test {
    public static void main(String[] args) {
        Crypto solana = new Crypto("Solana", ALERT_PERCENT_SOL);
        Crypto trunk = new Crypto("Trunk", ALERT_PERCENT_TRUNK);
        solana.load();
        trunk.load();
        Cmc api = new Cmc(solana, trunk);
        api.getPrice();
    }

}
