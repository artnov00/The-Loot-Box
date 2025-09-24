package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaGerenciarPedidos extends JPanel {

    private JTable tabelaVendas, tabelaItens;
    private DefaultTableModel modeloVendas, modeloItens;

    public TelaGerenciarPedidos() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabela de Vendas
        String[] colunasVendas = {"ID Venda", "Data", "Valor Total", "Pagamento", "Funcionário", "Cliente"};
        modeloVendas = new DefaultTableModel(colunasVendas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaVendas = new JTable(modeloVendas);

        // Tabela de Itens da Venda
        String[] colunasItens = {"Produto", "Quantidade", "Preço Unitário"};
        modeloItens = new DefaultTableModel(colunasItens, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabelaItens = new JTable(modeloItens);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabelaVendas), new JScrollPane(tabelaItens));
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        tabelaVendas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaVendas.getSelectedRow() != -1) {
                int idVenda = (int) tabelaVendas.getValueAt(tabelaVendas.getSelectedRow(), 0);
                carregarItensVenda(idVenda);
            }
        });

        JButton btnAtualizar = new JButton("Atualizar Lista de Vendas");
        add(btnAtualizar, BorderLayout.SOUTH);
        btnAtualizar.addActionListener(e -> carregarVendas());

        carregarVendas();
    }

    private void carregarVendas() {
        modeloVendas.setRowCount(0);
        modeloItens.setRowCount(0);
        String sql = "SELECT v.id_venda, v.data_venda, v.valor_total, v.forma_pagamento, f.nome as funcionario, u.nome as cliente " +
                "FROM Venda v " +
                "JOIN Funcionario f ON v.id_funcionario = f.id_funcionario " +
                "JOIN Usuario u ON v.id_usuario = u.id_usuario " +
                "ORDER BY v.data_venda DESC";

        try (Connection conn = ConexaoSQLite.abrirConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_venda"));
                row.add(rs.getString("data_venda"));
                row.add(String.format("R$ %.2f", rs.getBigDecimal("valor_total")));
                row.add(rs.getString("forma_pagamento"));
                row.add(rs.getString("funcionario"));
                row.add(rs.getString("cliente"));
                modeloVendas.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar vendas: " + e.getMessage());
        }
    }

    private void carregarItensVenda(int idVenda) {
        modeloItens.setRowCount(0);
        String sql = "SELECT ic.quantidade, ic.preco_unitario, j.nome as nome_jogo, c.modelo as nome_console " +
                "FROM ItemCompra ic " +
                "LEFT JOIN Jogo j ON ic.id_jogo = j.id_jogo " +
                "LEFT JOIN Console c ON ic.id_console = c.id_console " +
                "WHERE ic.id_venda = ?";

        try (Connection conn = ConexaoSQLite.abrirConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idVenda);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                String nomeProduto = rs.getString("nome_jogo") != null ? rs.getString("nome_jogo") : rs.getString("nome_console");
                row.add(nomeProduto);
                row.add(rs.getInt("quantidade"));
                row.add(String.format("R$ %.2f", rs.getBigDecimal("preco_unitario")));
                modeloItens.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar itens da venda: " + e.getMessage());
        }
    }
}

