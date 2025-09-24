package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaGerenciarFornecedores extends JPanel {
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public TelaGerenciarFornecedores() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "CNPJ", "Email", "Telefone", "Endereço"};
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

        carregarFornecedores();

        btnAdicionar.addActionListener(e -> showFornecedorDialog(null));
        btnEditar.addActionListener(e -> editarFornecedor());
        btnExcluir.addActionListener(e -> excluirFornecedor());
    }

    private void carregarFornecedores() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT * FROM Fornecedor ORDER BY nome";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_fornecedor"));
                row.add(rs.getString("nome"));
                row.add(rs.getString("cnpj"));
                row.add(rs.getString("email"));
                row.add(rs.getString("telefone"));
                row.add(rs.getString("endereco"));
                modeloTabela.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar fornecedores: " + e.getMessage());
        }
    }

    private void showFornecedorDialog(Integer fornecedorId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(fornecedorId == null ? "Adicionar Fornecedor" : "Editar Fornecedor");
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        JTextField txtCnpj = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtTelefone = new JTextField(20);
        JTextField txtEndereco = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("CNPJ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtCnpj, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(txtTelefone, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(txtEndereco, gbc);

        if (fornecedorId != null) {
            String sql = "SELECT nome, cnpj, email, telefone, endereco FROM Fornecedor WHERE id_fornecedor = ?";
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, fornecedorId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    txtNome.setText(rs.getString("nome"));
                    txtCnpj.setText(rs.getString("cnpj"));
                    txtEmail.setText(rs.getString("email"));
                    txtTelefone.setText(rs.getString("telefone"));
                    txtEndereco.setText(rs.getString("endereco"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Erro ao carregar fornecedor: " + e.getMessage());
            }
        }

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText();
            String cnpj = txtCnpj.getText();
            if (nome.trim().isEmpty() || cnpj.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nome e CNPJ são obrigatórios.");
                return;
            }

            String sql = (fornecedorId == null) ?
                    "INSERT INTO Fornecedor(nome, cnpj, email, telefone, endereco) VALUES(?,?,?,?,?)" :
                    "UPDATE Fornecedor SET nome=?, cnpj=?, email=?, telefone=?, endereco=? WHERE id_fornecedor=?";

            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, cnpj);
                pstmt.setString(3, txtEmail.getText());
                pstmt.setString(4, txtTelefone.getText());
                pstmt.setString(5, txtEndereco.getText());
                if (fornecedorId != null) pstmt.setInt(6, fornecedorId);

                pstmt.executeUpdate();
                carregarFornecedores();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar fornecedor: " + ex.getMessage());
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSalvar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editarFornecedor() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um fornecedor para editar.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        showFornecedorDialog(id);
    }

    private void excluirFornecedor() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um fornecedor para excluir.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir o fornecedor selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Fornecedor WHERE id_fornecedor = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                carregarFornecedores();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir fornecedor. Verifique se existem consoles associados a ele.");
            }
        }
    }
}

