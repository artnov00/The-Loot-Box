package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaGerenciarCategorias extends JPanel {
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public TelaGerenciarCategorias() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "Descrição"};
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

        carregarCategorias();

        btnAdicionar.addActionListener(e -> showCategoriaDialog(null));
        btnEditar.addActionListener(e -> editarCategoria());
        btnExcluir.addActionListener(e -> excluirCategoria());
    }

    private void carregarCategorias() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT * FROM Categoria ORDER BY nome";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_categoria"));
                row.add(rs.getString("nome"));
                row.add(rs.getString("descricao"));
                modeloTabela.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar categorias: " + e.getMessage());
        }
    }

    private void showCategoriaDialog(Integer categoriaId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(categoriaId == null ? "Adicionar Categoria" : "Editar Categoria");
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        JTextField txtDescricao = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtDescricao, gbc);

        if (categoriaId != null) {
            String sql = "SELECT nome, descricao FROM Categoria WHERE id_categoria = ?";
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoriaId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    txtNome.setText(rs.getString("nome"));
                    txtDescricao.setText(rs.getString("descricao"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Erro ao carregar categoria: " + e.getMessage());
            }
        }

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText();
            if (nome.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "O campo Nome é obrigatório.");
                return;
            }

            String sql = (categoriaId == null) ?
                    "INSERT INTO Categoria(nome, descricao) VALUES(?,?)" :
                    "UPDATE Categoria SET nome=?, descricao=? WHERE id_categoria=?";

            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, txtDescricao.getText());
                if (categoriaId != null) pstmt.setInt(3, categoriaId);

                pstmt.executeUpdate();
                carregarCategorias();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar categoria: " + ex.getMessage());
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSalvar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editarCategoria() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para editar.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        showCategoriaDialog(id);
    }

    private void excluirCategoria() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione uma categoria para excluir.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir a categoria selecionada?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Categoria WHERE id_categoria = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                carregarCategorias();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir categoria. Verifique se existem jogos associados a ela.");
            }
        }
    }
}

