package screens;

import javax.swing.*;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("The Loot Box - Painel Administrativo");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Adiciona o ícone da janela
        Image icone = ComponentesUI.getIconeLogo();
        if (icone != null) {
            setIconImage(icone);
        }

        // Adiciona a logo no topo
        add(ComponentesUI.criarPainelLogo(), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Vendas", new TelaVenda());
        abas.addTab("Pedidos", new TelaGerenciarPedidos());
        abas.addTab("Jogos", new TelaGerenciarJogos());
        abas.addTab("Consoles", new TelaGerenciarConsoles());
        abas.addTab("Categorias", new TelaGerenciarCategorias());
        abas.addTab("Plataformas", new TelaGerenciarPlataformas());
        abas.addTab("Fornecedores", new TelaGerenciarFornecedores());
        abas.addTab("Funcionários", new TelaGerenciarFuncionarios());
        abas.addTab("Clientes", new TelaGerenciarUsuarios());
        abas.addTab("Sobre", new TelaSobre());

        add(abas, BorderLayout.CENTER);
    }
}