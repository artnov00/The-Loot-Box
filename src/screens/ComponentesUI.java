package screens;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class ComponentesUI {

    private static final Color COR_FUNDO_PAINEL = new Color(245, 245, 245);
    private static final Color COR_BOTAO_PRIMARIO = new Color(0, 120, 215);
    private static final Color COR_TEXTO_BOTAO = Color.WHITE;
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static Image iconeLogo = null;

    public static JPanel criarPainelLogo() {
        JPanel painelLogo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelLogo.setBackground(Color.WHITE);
        try {
           
            URL url = ComponentesUI.class.getResource("/image.png");
            if (url == null) throw new IOException("Arquivo de imagem não encontrado no classpath: /image.png");

            ImageIcon logoIcon = new ImageIcon(url);
            
            Image imagem = logoIcon.getImage().getScaledInstance(200, 60, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(imagem));
            painelLogo.add(lblLogo);
        } catch (Exception e) {
            
            JLabel lblTextoLogo = new JLabel("The Loot Box");
            lblTextoLogo.setFont(new Font("Arial", Font.BOLD, 24));
            painelLogo.add(lblTextoLogo);
            System.err.println("Erro ao carregar a imagem da logo: " + e.getMessage());
        }
        painelLogo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return painelLogo;
    }

   
    public static Image getIconeLogo() {
        if (iconeLogo == null) {
            try {
                URL url = ComponentesUI.class.getResource("/image.png");
                if (url == null) throw new IOException("Arquivo de imagem não encontrado no classpath: /image.png");
                iconeLogo = new ImageIcon(url).getImage();
            } catch (Exception e) {
                System.err.println("Erro ao carregar o ícone da logo: " + e.getMessage());
            }
        }
        return iconeLogo;
    }

    public static void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(COR_TEXTO_BOTAO);
        button.setFont(FONTE_BOTAO);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

}
