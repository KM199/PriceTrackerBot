import org.example.Cmc;
import org.example.Crypto;

import static org.example.Settings.*;
import static org.example.Settings.TRUNK_CMC_ID;

public class test {
    public static void main(String[] args) {
        Crypto solana = new Crypto("Solana", SOLANA_CMC_ID, ALERT_PERCENT_SOL);
        Crypto trunk = new Crypto("Trunk", TRUNK_CMC_ID, ALERT_PERCENT_TRUNK);
        solana.load();
        trunk.load();
        Cmc api = new Cmc(Crypto.cryptos);
        api.getPrice();
    }

}
