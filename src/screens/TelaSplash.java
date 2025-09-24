package screens;

import javax.swing.*;
import java.awt.*;

public class TelaSplash extends JWindow {
    public TelaSplash() {
        setSize(450, 300);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(255, 255, 255));
        content.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 3));

        content.add(ComponentesUI.criarPainelLogo(), BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("A carregar sistema...");
        progressBar.setPreferredSize(new Dimension(getWidth(), 30));
        content.add(progressBar, BorderLayout.SOUTH);

        setContentPane(content);

        // Timer para fechar o splash e abrir a tela de login
        Timer timer = new Timer(3000, e -> {
            dispose();
            new TelaLogin().setVisible(true);
        });
        timer.setRepeats(false);
        timer.start();
    }
}

