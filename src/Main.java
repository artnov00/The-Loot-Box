import screens.TelaSplash;
import javax.swing.SwingUtilities;
import database.BancoDeDados;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BancoDeDados.criarTabelas();
            new TelaSplash().setVisible(true);
        });
    }
}

