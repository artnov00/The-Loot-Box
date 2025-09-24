package screens;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;
import database.ConexaoSQLite;

public class TelaGerenciarFuncionarios extends JPanel {
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public TelaGerenciarFuncionarios() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "Login", "Cargo", "Salário"};
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

        carregarFuncionarios();

        btnAdicionar.addActionListener(e -> showFuncionarioDialog(null));
        btnEditar.addActionListener(e -> editarFuncionario());
        btnExcluir.addActionListener(e -> excluirFuncionario());
    }

    private void carregarFuncionarios() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT * FROM Funcionario ORDER BY nome";
        try (Connection conn = ConexaoSQLite.abrirConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_funcionario"));
                row.add(rs.getString("nome"));
                row.add(rs.getString("login"));
                row.add(rs.getString("cargo"));
                row.add(String.format("R$ %.2f", rs.getBigDecimal("salario")));
                modeloTabela.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar funcionários: " + e.getMessage());
        }
    }

    private void showFuncionarioDialog(Integer funcionarioId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(funcionarioId == null ? "Adicionar Funcionário" : "Editar Funcionário");
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        JTextField txtLogin = new JTextField(20);
        JTextField txtCargo = new JTextField(20);
        JTextField txtSalario = new JTextField(20);
        JPasswordField txtSenha = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtLogin, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Cargo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtCargo, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Salário:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(txtSalario, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel(funcionarioId == null ? "Senha:" : "Nova Senha (opcional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(txtSenha, gbc);

        if (funcionarioId != null) {
            String sql = "SELECT nome, login, cargo, salario FROM Funcionario WHERE id_funcionario = ?";
            try (Connection conn = ConexaoSQLite.abrirConexao();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, funcionarioId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    txtNome.setText(rs.getString("nome"));
                    txtLogin.setText(rs.getString("login"));
                    txtCargo.setText(rs.getString("cargo"));
                    txtSalario.setText(rs.getBigDecimal("salario").toString());
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(dialog, "Erro ao carregar funcionário: " + e.getMessage());
            }
        }

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText();
            String login = txtLogin.getText();
            char[] senha = txtSenha.getPassword();

            if (nome.trim().isEmpty() || login.trim().isEmpty() || (funcionarioId == null && senha.length == 0)) {
                JOptionPane.showMessageDialog(dialog, "Nome, Login e Senha são obrigatórios para novos funcionários.");
                return;
            }

            String sql;
            if (funcionarioId == null) {
                sql = "INSERT INTO Funcionario(nome, login, cargo, salario, senha) VALUES(?,?,?,?,?)";
            } else {
                sql = "UPDATE Funcionario SET nome=?, login=?, cargo=?, salario=?" + (senha.length > 0 ? ", senha=?" : "") + " WHERE id_funcionario=?";
            }

            try (Connection conn = ConexaoSQLite.abrirConexao();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, nome);
                pstmt.setString(2, login);
                pstmt.setString(3, txtCargo.getText());
                pstmt.setBigDecimal(4, new BigDecimal(txtSalario.getText().replace(",", ".")));

                if (funcionarioId == null) { // Adicionar
                    pstmt.setString(5, new String(senha));
                } else { // Editar
                    int paramIndex = 5;
                    if (senha.length > 0) {
                        pstmt.setString(paramIndex++, new String(senha));
                    }
                    pstmt.setInt(paramIndex, funcionarioId);
                }

                pstmt.executeUpdate();
                carregarFuncionarios();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de salário inválido.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar funcionário: " + ex.getMessage());
            } finally {
                Arrays.fill(senha, '0');
            }
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSalvar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editarFuncionario() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um funcionário para editar.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        showFuncionarioDialog(id);
    }

    private void excluirFuncionario() {
        if (tabela.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um funcionário para excluir.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        if (id == 1) {
            JOptionPane.showMessageDialog(this, "Não é possível excluir o administrador principal.", "Ação Proibida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir o funcionário selecionado?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexaoSQLite.abrirConexao();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Funcionario WHERE id_funcionario = ?")) {

                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                carregarFuncionarios();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir funcionário. Pode haver vendas ou clientes associados a ele.");
            }
        }
    }
}

