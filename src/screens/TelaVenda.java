package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaVenda extends JPanel {

    private JComboBox<ProdutoVenda> cbProdutos;
    private JComboBox<Cliente> cbClientes;
    private JSpinner spinnerQuantidade;
    private DefaultTableModel modeloCarrinho;
    private JLabel lblTotal;
    private static final String TIPO_JOGO = "Jogo";
    private static final String TIPO_CONSOLE = "Console";

    
    private static class ProdutoVenda {
        int id; String nome; BigDecimal preco; int estoque; String tipo;
        ProdutoVenda(int id, String nome, BigDecimal preco, int estoque, String tipo) {
            this.id = id; this.nome = nome; this.preco = preco; this.estoque = estoque; this.tipo = tipo;
        }
        @Override public String toString() { return String.format("%s - R$ %.2f (Estoque: %d)", nome, preco, estoque); }
    }

    private static class Cliente {
        int id; String nome; String cpf;
        Cliente(int id, String nome, String cpf) { this.id = id; this.nome = nome; this.cpf = cpf; }
        @Override public String toString() { return nome + " (" + cpf + ")"; }
    }

    public TelaVenda() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

       
        JPanel painelSuperior = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbProdutos = new JComboBox<>();
        cbClientes = new JComboBox<>();
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton btnAdicionar = new JButton("Adicionar ao Carrinho");

        gbc.gridx = 0; gbc.gridy = 0; painelSuperior.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; painelSuperior.add(cbClientes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; painelSuperior.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; painelSuperior.add(cbProdutos, gbc);

        gbc.gridx = 0; gbc.gridy = 2; painelSuperior.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0; painelSuperior.add(spinnerQuantidade, gbc);

        gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        painelSuperior.add(btnAdicionar, gbc);

        add(painelSuperior, BorderLayout.NORTH);

        
        String[] colunas = {"ID Produto", "Tipo", "Nome", "Qtd", "Preço Unit.", "Subtotal"};
        modeloCarrinho = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabelaCarrinho = new JTable(modeloCarrinho);
        add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        
        JPanel painelInferior = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: R$ 0,00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton btnFinalizar = new JButton("Finalizar Venda");
        painelInferior.add(lblTotal, BorderLayout.WEST);
        painelInferior.add(btnFinalizar, BorderLayout.EAST);
        add(painelInferior, BorderLayout.SOUTH);

        carregarDadosIniciais();

        btnAdicionar.addActionListener(e -> adicionarAoCarrinho());
        btnFinalizar.addActionListener(e -> finalizarVenda());
    }

    private void carregarDadosIniciais() {
        cbProdutos.removeAllItems();
        cbClientes.removeAllItems();

       
        try (Connection conn = ConexaoSQLite.abrirConexao()) {
           
            String sqlJogos = "SELECT id_jogo, nome, preco, estoque FROM Jogo WHERE estoque > 0";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlJogos)) {
                while (rs.next()) {
                    cbProdutos.addItem(new ProdutoVenda(
                            rs.getInt("id_jogo"), rs.getString("nome"), rs.getBigDecimal("preco"), rs.getInt("estoque"), TIPO_JOGO
                    ));
                }
            }

            
            String sqlConsoles = "SELECT id_console, modelo, preco, estoque FROM Console WHERE estoque > 0";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlConsoles)) {
                while (rs.next()) {
                    cbProdutos.addItem(new ProdutoVenda(
                            rs.getInt("id_console"), rs.getString("modelo"), rs.getBigDecimal("preco"), rs.getInt("estoque"), TIPO_CONSOLE
                    ));
                }
            }

            if (cbProdutos.getItemCount() == 0) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Nenhum jogo ou console com estoque disponível foi encontrado.",
                                "Aviso de Estoque",
                                JOptionPane.INFORMATION_MESSAGE)
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT id_usuario, nome, cpf FROM Usuario ORDER BY nome";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    cbClientes.addItem(new Cliente(rs.getInt("id_usuario"), rs.getString("nome"), rs.getString("cpf")));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar clientes: " + e.getMessage(), "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void adicionarAoCarrinho() {
        ProdutoVenda produto = (ProdutoVenda) cbProdutos.getSelectedItem();
        int quantidade = (int) spinnerQuantidade.getValue();

        if (produto == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto.");
            return;
        }
        if (quantidade > produto.estoque) {
            JOptionPane.showMessageDialog(this, "Quantidade solicitada excede o estoque disponível.");
            return;
        }

        BigDecimal subtotal = produto.preco.multiply(new BigDecimal(quantidade));
        modeloCarrinho.addRow(new Object[]{produto.id, produto.tipo, produto.nome, quantidade, produto.preco, subtotal});
        atualizarTotal();
    }

    private void atualizarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < modeloCarrinho.getRowCount(); i++) {
            total = total.add((BigDecimal) modeloCarrinho.getValueAt(i, 5));
        }
        lblTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void finalizarVenda() {
        if (modeloCarrinho.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "O carrinho está vazio.");
            return;
        }
        Cliente cliente = (Cliente) cbClientes.getSelectedItem();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente.");
            return;
        }

       
        int idFuncionario = 1;
        String formaPagamento = "Cartão"; 
        BigDecimal valorTotal = new BigDecimal(lblTotal.getText().replace("Total: R$ ", "").replace(",", "."));

        Connection conn = null;
        try {
            conn = ConexaoSQLite.abrirConexao();
            conn.setAutoCommit(false); 

            
            String sqlVenda = "INSERT INTO Venda(forma_pagamento, valor_total, id_funcionario, id_usuario) VALUES(?,?,?,?)";
            int idVenda;
            try (PreparedStatement pstmtVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVenda.setString(1, formaPagamento);
                pstmtVenda.setBigDecimal(2, valorTotal);
                pstmtVenda.setInt(3, idFuncionario);
                pstmtVenda.setInt(4, cliente.id);
                pstmtVenda.executeUpdate();
                try (ResultSet rs = pstmtVenda.getGeneratedKeys()) {
                    idVenda = rs.getInt(1);
                }
            }

           
            String sqlItem = "INSERT INTO ItemCompra(quantidade, preco_unitario, id_venda, id_jogo, id_console) VALUES(?,?,?,?,?)";
            String sqlUpdateEstoqueJogo = "UPDATE Jogo SET estoque = estoque - ? WHERE id_jogo = ?";
            String sqlUpdateEstoqueConsole = "UPDATE Console SET estoque = estoque - ? WHERE id_console = ?";

            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem);
                 PreparedStatement pstmtJogo = conn.prepareStatement(sqlUpdateEstoqueJogo);
                 PreparedStatement pstmtConsole = conn.prepareStatement(sqlUpdateEstoqueConsole)) {

                for (int i = 0; i < modeloCarrinho.getRowCount(); i++) {
                    int idProduto = (int) modeloCarrinho.getValueAt(i, 0);
                    String tipo = (String) modeloCarrinho.getValueAt(i, 1);
                    int qtd = (int) modeloCarrinho.getValueAt(i, 3);
                    BigDecimal precoUnit = (BigDecimal) modeloCarrinho.getValueAt(i, 4);

                    pstmtItem.setInt(1, qtd);
                    pstmtItem.setBigDecimal(2, precoUnit);
                    pstmtItem.setInt(3, idVenda);

                    if (TIPO_JOGO.equals(tipo)) {
                        pstmtItem.setInt(4, idProduto);
                        pstmtItem.setNull(5, Types.INTEGER);
                        pstmtJogo.setInt(1, qtd);
                        pstmtJogo.setInt(2, idProduto);
                        pstmtJogo.addBatch();
                    } else { 
                        pstmtItem.setNull(4, Types.INTEGER);
                        pstmtItem.setInt(5, idProduto);
                        pstmtConsole.setInt(1, qtd);
                        pstmtConsole.setInt(2, idProduto);
                        pstmtConsole.addBatch();
                    }
                    pstmtItem.executeUpdate();
                }
                pstmtJogo.executeBatch();
                pstmtConsole.executeBatch();
            }

            conn.commit(); 

            JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso!");
            modeloCarrinho.setRowCount(0);
            atualizarTotal();
            carregarDadosIniciais();

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            JOptionPane.showMessageDialog(this, "Erro ao finalizar venda: " + e.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); } }
        }
    }
}


