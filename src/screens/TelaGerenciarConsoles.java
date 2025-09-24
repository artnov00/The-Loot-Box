package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaGerenciarConsoles extends JPanel {
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private static class FornecedorItem {
        final int id; final String nome;
        FornecedorItem(int id, String nome) { this.id = id; this.nome = nome; }
        public int getId() { return id; }
        @Override public String toString() { return nome; }
    }

    public TelaGerenciarConsoles() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Modelo", "Preço", "Estoque", "Fornecedor"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        add(painelBotoes, BorderLayout.SOUTH);

        carregarConsoles();

        btnAdicionar.addActionListener(e -> showConsoleDialog(null));
        btnEditar.addActionListener(e -> editarConsole());
        btnExcluir.addActionListener(e -> excluirConsole());
    }

    private void carregarConsoles() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT c.id_console, c.modelo, c.preco, c.estoque, f.nome as fornecedor_nome " +
                "FROM Console c JOIN Fornecedor f ON c.id_fornecedor = f.id_fornecedor ORDER BY c.modelo";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_console"));
                row.add(rs.getString("modelo"));
                row.add(String.format("R$ %.2f", rs.getBigDecimal("preco")));
                row.add(rs.getInt("estoque"));
                row.add(rs.getString("fornecedor_nome"));
                modeloTabela.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar consoles: " + e.getMessage());
        }
    }

    private void showConsoleDialog(Integer consoleId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(consoleId == null ? "Adicionar Console" : "Editar Console");
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtModelo = new JTextField();
        JTextField txtPreco = new JTextField();
        JSpinner spinnerEstoque = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        JComboBox<FornecedorItem> cbFornecedores = new JComboBox<>(getFornecedores().toArray(new FornecedorItem[0]));

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Modelo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtModelo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Preço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtPreco, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Estoque:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(spinnerEstoque, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Fornecedor:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(cbFornecedores, gbc);

        if (consoleId != null) {
            String sql = "SELECT modelo, preco, estoque, id_fornecedor FROM Console WHERE id_console = ?";
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, consoleId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    txtModelo.setText(rs.getString("modelo"));
                    txtPreco.setText(rs.getBigDecimal("preco").toString());
                    spinnerEstoque.setValue(rs.getInt("estoque"));
                    int idFornecedor = rs.getInt("id_fornecedor");
                    for (int i = 0; i < cbFornecedores.getItemCount(); i++) {
                        if (cbFornecedores.getItemAt(i).getId() == idFornecedor) {
                            cbFornecedores.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Erro ao carregar console: " + e.getMessage());
            }
        }

        JButton btnSalvar = new JButton("Salvar");
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSalvar, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> {
            String modelo = txtModelo.getText();
            String precoStr = txtPreco.getText();
            int estoque = (int) spinnerEstoque.getValue();
            FornecedorItem fornecedor = (FornecedorItem) cbFornecedores.getSelectedItem();

            if (modelo.trim().isEmpty() || precoStr.trim().isEmpty() || fornecedor == null) {
                JOptionPane.showMessageDialog(dialog, "Modelo, Preço e Fornecedor são obrigatórios.");
                return;
            }

            BigDecimal preco;
            try {
                preco = new BigDecimal(precoStr.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de preço inválido.");
                return;
            }

            String sql = (consoleId == null) ?
                    "INSERT INTO Console(modelo, preco, estoque, id_fornecedor) VALUES(?,?,?,?)" :
                    "UPDATE Console SET modelo=?, preco=?, estoque=?, id_fornecedor=? WHERE id_console=?";

            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, modelo);
                pstmt.setBigDecimal(2, preco);
                pstmt.setInt(3, estoque);
                pstmt.setInt(4, fornecedor.getId());
                if (consoleId != null) pstmt.setInt(5, consoleId);

                pstmt.executeUpdate();
                carregarConsoles();
                dialog.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar console: " + ex.getMessage());
            }
        });
        dialog.setVisible(true);
    }

    private void editarConsole() {
        if (tabela.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um console para editar.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        showConsoleDialog(id);
    }

    private void excluirConsole() {
        if (tabela.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um console para excluir.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir o console selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Console WHERE id_console = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                carregarConsoles();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir console: " + e.getMessage());
            }
        }
    }

    private List<FornecedorItem> getFornecedores() {
        List<FornecedorItem> lista = new ArrayList<>();
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id_fornecedor, nome FROM Fornecedor ORDER BY nome")) {
            while (rs.next()) {
                lista.add(new FornecedorItem(rs.getInt("id_fornecedor"), rs.getString("nome")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}

