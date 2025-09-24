package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaGerenciarUsuarios extends JPanel {
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private static class FuncionarioItem {
        final int id; final String nome;
        FuncionarioItem(int id, String nome) { this.id = id; this.nome = nome; }
        public int getId() { return id; }
        @Override public String toString() { return nome; }
    }

    public TelaGerenciarUsuarios() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "CPF", "Email", "Telefone", "Endereço", "Atendido Por"};
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

        carregarUsuarios();

        btnAdicionar.addActionListener(e -> showUsuarioDialog(null));
        btnEditar.addActionListener(e -> editarUsuario());
        btnExcluir.addActionListener(e -> excluirUsuario());
    }

    private void carregarUsuarios() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT u.*, f.nome as nome_funcionario FROM Usuario u " +
                "LEFT JOIN Funcionario f ON u.id_funcionario = f.id_funcionario ORDER BY u.nome";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_usuario"));
                row.add(rs.getString("nome"));
                row.add(rs.getString("cpf"));
                row.add(rs.getString("email"));
                row.add(rs.getString("telefone"));
                row.add(rs.getString("endereco"));
                row.add(rs.getString("nome_funcionario")); // Pode ser null
                modeloTabela.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar clientes: " + e.getMessage());
        }
    }

    private void showUsuarioDialog(Integer usuarioId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(usuarioId == null ? "Adicionar Cliente" : "Editar Cliente");
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        JTextField txtCpf = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtTelefone = new JTextField(20);
        JTextField txtEndereco = new JTextField(20);
        JPasswordField txtSenha = new JPasswordField(20);
        JComboBox<FuncionarioItem> cbFuncionarios = new JComboBox<>(getFuncionarios().toArray(new FuncionarioItem[0]));

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtCpf, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(txtTelefone, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(txtEndereco, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel(usuarioId == null ? "Senha:" : "Nova Senha (opcional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(txtSenha, gbc);
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Atendido por:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; panel.add(cbFuncionarios, gbc);

        if (usuarioId != null) {
            String sql = "SELECT * FROM Usuario WHERE id_usuario = ?";
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, usuarioId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    txtNome.setText(rs.getString("nome"));
                    txtCpf.setText(rs.getString("cpf"));
                    txtEmail.setText(rs.getString("email"));
                    txtTelefone.setText(rs.getString("telefone"));
                    txtEndereco.setText(rs.getString("endereco"));
                    int idFuncionario = rs.getInt("id_funcionario");
                    for (int i = 0; i < cbFuncionarios.getItemCount(); i++) {
                        if (cbFuncionarios.getItemAt(i).getId() == idFuncionario) {
                            cbFuncionarios.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Erro ao carregar cliente: " + e.getMessage());
            }
        }

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText();
            String cpf = txtCpf.getText();
            String email = txtEmail.getText();
            char[] senha = txtSenha.getPassword();

            if (nome.trim().isEmpty() || cpf.trim().isEmpty() || email.trim().isEmpty() || (usuarioId == null && senha.length == 0)) {
                JOptionPane.showMessageDialog(dialog, "Nome, CPF, Email e Senha são obrigatórios para novos clientes.");
                return;
            }

            String sql;
            if (usuarioId == null) {
                sql = "INSERT INTO Usuario(nome, cpf, email, telefone, endereco, senha, id_funcionario) VALUES(?,?,?,?,?,?,?)";
            } else {
                sql = "UPDATE Usuario SET nome=?, cpf=?, email=?, telefone=?, endereco=?, id_funcionario=?" + (senha.length > 0 ? ", senha=?" : "") + " WHERE id_usuario=?";
            }

            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                FuncionarioItem func = (FuncionarioItem) cbFuncionarios.getSelectedItem();

                pstmt.setString(1, nome);
                pstmt.setString(2, cpf);
                pstmt.setString(3, email);
                pstmt.setString(4, txtTelefone.getText());
                pstmt.setString(5, txtEndereco.getText());

                if (func != null) {
                    pstmt.setInt(6, func.getId());
                } else {
                    pstmt.setNull(6, Types.INTEGER);
                }

                if (usuarioId == null) { // Adicionar
                    pstmt.setString(7, new String(senha));
                } else { // Editar
                    int paramIndex = 7;
                    if (senha.length > 0) {
                        pstmt.setString(paramIndex++, new String(senha));
                    }
                    pstmt.setInt(paramIndex, usuarioId);
                }

                pstmt.executeUpdate();
                carregarUsuarios();
                dialog.dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar cliente: " + ex.getMessage());
            } finally {
                Arrays.fill(senha, '0');
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSalvar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editarUsuario() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para editar.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        showUsuarioDialog(id);
    }

    private void excluirUsuario() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para excluir.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir o cliente selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Usuario WHERE id_usuario = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                carregarUsuarios();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir cliente. Verifique se existem vendas associadas a ele.");
            }
        }
    }

    private List<FuncionarioItem> getFuncionarios() {
        List<FuncionarioItem> lista = new ArrayList<>();
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id_funcionario, nome FROM Funcionario ORDER BY nome")) {
            while (rs.next()) {
                lista.add(new FuncionarioItem(rs.getInt("id_funcionario"), rs.getString("nome")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}

