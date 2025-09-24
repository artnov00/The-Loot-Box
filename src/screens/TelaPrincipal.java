package screens;

import javax.swing.*;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("The Loot Box - Painel de Gerenciamento");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Adicionando as abas de gerenciamento
        abas.addTab("Venda", new TelaVenda());
        abas.addTab("Pedidos", new TelaGerenciarPedidos());
        abas.addTab("Clientes", new TelaGerenciarUsuarios());
        abas.addTab("Funcionários", new TelaGerenciarFuncionarios());
        abas.addTab("Jogos", new TelaGerenciarJogos());
        abas.addTab("Consoles", new TelaGerenciarConsoles());
        abas.addTab("Fornecedores", new TelaGerenciarFornecedores());
        abas.addTab("Categorias", new TelaGerenciarCategorias());
        abas.addTab("Plataformas", new TelaGerenciarPlataformas());
        abas.addTab("Sobre", new TelaSobre());

        add(abas, BorderLayout.CENTER);
    }
}

