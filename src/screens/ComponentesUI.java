package screens;

import javax.swing.*;
import java.awt.*;

public class ComponentesUI {

    private static final Color COR_FUNDO_PAINEL = new Color(245, 245, 245);
    private static final Color COR_BOTAO_PRIMARIO = new Color(0, 120, 215);
    private static final Color COR_TEXTO_BOTAO = Color.WHITE;
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);

    /**
     * Cria um painel padronizado com a logo da empresa.
     * @return JPanel com a logo.
     */
    public static JPanel criarPainelLogo() {
        JPanel painelLogo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelLogo.setBackground(COR_FUNDO_PAINEL);

        ImageIcon logoIcon = new ImageIcon("src/imagem.png");
        JLabel lblLogo = new JLabel();

        if (logoIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image image = logoIcon.getImage().getScaledInstance(200, 67, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(image));
        } else {
            lblLogo.setText("The Loot Box");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblLogo.setForeground(Color.DARK_GRAY);
        }

        painelLogo.add(lblLogo);
        painelLogo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return painelLogo;
    }

    /**
     * Aplica um estilo padronizado a um JButton.
     * @param button O botão a ser estilizado.
     * @param backgroundColor A cor de fundo do botão.
     */
    public static void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(COR_TEXTO_BOTAO);
        button.setFont(FONTE_BOTAO);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}

