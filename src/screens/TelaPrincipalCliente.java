package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaPrincipalCliente extends JFrame {

    private final int idCliente;
    private JComboBox<ProdutoItem> cbProdutos;
    private JSpinner spinnerQuantidade;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloTabelaCarrinho;
    private JLabel lblTotal;

    private static class ProdutoItem {
        final int id;
        final String nome;
        final BigDecimal preco;
        final String tipo; // "JOGO" ou "CONSOLE"
        int estoque;

        ProdutoItem(int id, String nome, BigDecimal preco, String tipo, int estoque) {
            this.id = id;
            this.nome = nome;
            this.preco = preco;
            this.tipo = tipo;
            this.estoque = estoque;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - R$ %.2f", nome, tipo, preco);
        }
    }

    public TelaPrincipalCliente(int idCliente, String nomeCliente) {
        this.idCliente = idCliente;
        setTitle("The Loot Box - Bem-vindo(a) " + nomeCliente);
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Painel Superior
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblTitulo = new JLabel("Faça sua Compra", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        JButton btnSair = new JButton("Sair");
        painelTopo.add(lblTitulo, BorderLayout.CENTER);
        painelTopo.add(btnSair, BorderLayout.EAST);
        add(painelTopo, BorderLayout.NORTH);

        // Painel de Compra (Esquerda)
        JPanel painelCompra = new JPanel(new GridBagLayout());
        painelCompra.setBorder(BorderFactory.createTitledBorder("Selecione os Produtos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbProdutos = new JComboBox<>();
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton btnAdicionar = new JButton("Adicionar ao Carrinho");

        gbc.gridx = 0; gbc.gridy = 0; painelCompra.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; painelCompra.add(cbProdutos, gbc);
        gbc.gridx = 0; gbc.gridy = 1; painelCompra.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; painelCompra.add(spinnerQuantidade, gbc);
        gbc.gridy = 2; gbc.gridwidth = 2; painelCompra.add(btnAdicionar, gbc);

        // Painel do Carrinho (Direita)
        JPanel painelCarrinho = new JPanel(new BorderLayout(10, 10));
        painelCarrinho.setBorder(BorderFactory.createTitledBorder("Carrinho de Compras"));

        String[] colunas = {"Produto", "Qtd", "Preço Unit.", "Subtotal"};
        modeloTabelaCarrinho = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaCarrinho = new JTable(modeloTabelaCarrinho);
        painelCarrinho.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        // Painel de Finalização (Abaixo do Carrinho)
        JPanel painelFinalizar = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: R$ 0,00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel painelPagamento = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelPagamento.add(new JLabel("Pagamento:"));
        JComboBox<String> cbPagamento = new JComboBox<>(new String[]{"Crédito", "Débito", "PIX"});
        JButton btnFinalizar = new JButton("Finalizar Compra");
        painelPagamento.add(cbPagamento);
        painelPagamento.add(btnFinalizar);

        painelFinalizar.add(lblTotal, BorderLayout.WEST);
        painelFinalizar.add(painelPagamento, BorderLayout.EAST);
        painelCarrinho.add(painelFinalizar, BorderLayout.SOUTH);

        // Split Pane para dividir a tela
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelCompra, painelCarrinho);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);

        carregarProdutos();

        btnSair.addActionListener(e -> {
            new TelaLogin().setVisible(true);
            dispose();
        });

        btnAdicionar.addActionListener(e -> adicionarAoCarrinho());
        btnFinalizar.addActionListener(e -> finalizarCompra((String) cbPagamento.getSelectedItem()));
    }

    private void carregarProdutos() {
        List<ProdutoItem> produtos = new ArrayList<>();
        // Carregar Jogos
        String sqlJogos = "SELECT id_jogo, nome, preco, estoque FROM Jogo WHERE estoque > 0";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlJogos)) {
            while (rs.next()) {
                produtos.add(new ProdutoItem(rs.getInt("id_jogo"), rs.getString("nome"), rs.getBigDecimal("preco"), "JOGO", rs.getInt("estoque")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar jogos: " + e.getMessage());
        }
        // Carregar Consoles
        String sqlConsoles = "SELECT id_console, modelo, preco, estoque FROM Console WHERE estoque > 0";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlConsoles)) {
            while (rs.next()) {
                produtos.add(new ProdutoItem(rs.getInt("id_console"), rs.getString("modelo"), rs.getBigDecimal("preco"), "CONSOLE", rs.getInt("estoque")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar consoles: " + e.getMessage());
        }

        cbProdutos.setModel(new DefaultComboBoxModel<>(produtos.toArray(new ProdutoItem[0])));
        if (produtos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum produto com estoque disponível no momento.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void adicionarAoCarrinho() {
        ProdutoItem produtoSelecionado = (ProdutoItem) cbProdutos.getSelectedItem();
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto.");
            return;
        }
        int quantidade = (int) spinnerQuantidade.getValue();
        if (quantidade > produtoSelecionado.estoque) {
            JOptionPane.showMessageDialog(this, "Quantidade solicitada (" + quantidade + ") é maior que o estoque disponível (" + produtoSelecionado.estoque + ").");
            return;
        }

        BigDecimal subtotal = produtoSelecionado.preco.multiply(new BigDecimal(quantidade));

        Vector<Object> row = new Vector<>();
        row.add(produtoSelecionado); // Adiciona o objeto inteiro para fácil recuperação
        row.add(quantidade);
        row.add(produtoSelecionado.preco);
        row.add(subtotal);
        modeloTabelaCarrinho.addRow(row);

        atualizarTotal();
    }

    private void atualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < modeloTabelaCarrinho.getRowCount(); i++) {
            total = total.add((BigDecimal) modeloTabelaCarrinho.getValueAt(i, 3));
        }
        lblTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void finalizarCompra(String formaPagamento) {
        if (modeloTabelaCarrinho.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "O carrinho está vazio.");
            return;
        }

        Connection conn = null;
        try {
            conn = ConexaoSQLite.abrirConexao();
            conn.setAutoCommit(false); // Iniciar transação

            // 1. Inserir a Venda
            String sqlVenda = "INSERT INTO Venda(data_venda, forma_pagamento, valor_total, id_funcionario, id_usuario) VALUES (?, ?, ?, ?, ?)";
            int idVenda;
            try (PreparedStatement pstmtVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                BigDecimal total = new BigDecimal(lblTotal.getText().replace("Total: R$ ", "").replace(",", "."));
                pstmtVenda.setString(1, LocalDate.now().toString());
                pstmtVenda.setString(2, formaPagamento);
                pstmtVenda.setBigDecimal(3, total);
                pstmtVenda.setInt(4, 1); // **CORREÇÃO: Associa ao funcionário admin padrão**
                pstmtVenda.setInt(5, this.idCliente);
                pstmtVenda.executeUpdate();

                ResultSet rs = pstmtVenda.getGeneratedKeys();
                idVenda = rs.getInt(1);
            }

            // 2. Inserir Itens da Compra e Atualizar Estoque
            String sqlItem = "INSERT INTO ItemCompra(quantidade, preco_unitario, id_venda, id_jogo, id_console) VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateEstoqueJogo = "UPDATE Jogo SET estoque = estoque - ? WHERE id_jogo = ?";
            String sqlUpdateEstoqueConsole = "UPDATE Console SET estoque = estoque - ? WHERE id_console = ?";

            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem);
                 PreparedStatement pstmtUpdateJogo = conn.prepareStatement(sqlUpdateEstoqueJogo);
                 PreparedStatement pstmtUpdateConsole = conn.prepareStatement(sqlUpdateEstoqueConsole)) {

                for (int i = 0; i < modeloTabelaCarrinho.getRowCount(); i++) {
                    ProdutoItem produto = (ProdutoItem) modeloTabelaCarrinho.getValueAt(i, 0);
                    int quantidade = (int) modeloTabelaCarrinho.getValueAt(i, 1);
                    BigDecimal precoUnitario = (BigDecimal) modeloTabelaCarrinho.getValueAt(i, 2);

                    pstmtItem.setInt(1, quantidade);
                    pstmtItem.setBigDecimal(2, precoUnitario);
                    pstmtItem.setInt(3, idVenda);
                    if ("JOGO".equals(produto.tipo)) {
                        pstmtItem.setInt(4, produto.id);
                        pstmtItem.setNull(5, Types.INTEGER);
                        pstmtUpdateJogo.setInt(1, quantidade);
                        pstmtUpdateJogo.setInt(2, produto.id);
                        pstmtUpdateJogo.executeUpdate();
                    } else { // CONSOLE
                        pstmtItem.setNull(4, Types.INTEGER);
                        pstmtItem.setInt(5, produto.id);
                        pstmtUpdateConsole.setInt(1, quantidade);
                        pstmtUpdateConsole.setInt(2, produto.id);
                        pstmtUpdateConsole.executeUpdate();
                    }
                    pstmtItem.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Compra finalizada com sucesso!");
            modeloTabelaCarrinho.setRowCount(0);
            atualizarTotal();
            carregarProdutos();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            JOptionPane.showMessageDialog(this, "Erro ao finalizar a compra: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}

