import org.example.Cmc;
import org.example.Crypto;

public class test {
    public static void main(String[] args) {
        Crypto solana = new Crypto("Solana");
        Crypto trunk = new Crypto("Trunk");
        solana.load();
        trunk.load();
        Cmc api = new Cmc(solana, trunk);
        api.getPrice();
    }

}
