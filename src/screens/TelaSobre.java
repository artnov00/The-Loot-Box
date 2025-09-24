package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaSobre extends JPanel {
    public TelaSobre() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        areaTexto.setOpaque(false);
        areaTexto.setText(
                "The Loot Box - Sistema de Gestão de Loja de Games\n\n" +
                        "Versão: 1.0\n\n" +
                        "Este software foi desenvolvido como um sistema de ponto de venda (PDV) e gerenciamento " +
                        "para uma loja de jogos eletrônicos, baseado em um modelo conceitual de banco de dados.\n\n" +
                        "Funcionalidades:\n" +
                        "- Gestão completa (CRUD) de Produtos (Jogos e Consoles)\n" +
                        "- Gestão de Clientes e Funcionários\n" +
                        "- Gestão de Fornecedores, Categorias e Plataformas\n" +
                        "- Módulo de Vendas com controle de estoque\n" +
                        "- Histórico de Pedidos e Vendas\n\n" +
                        "Tecnologias Utilizadas:\n" +
                        "- Java com biblioteca Swing para a interface gráfica\n" +
                        "- Banco de Dados SQLite para armazenamento local\n" +
                        "- JDBC para comunicação com o banco de dados"
        );

        add(new JScrollPane(areaTexto), BorderLayout.CENTER);
    }
}

