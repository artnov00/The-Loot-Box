package screens;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import database.ConexaoSQLite;

public class TelaCadastroCliente extends JFrame {

    public TelaCadastroCliente() {
        setTitle("Cadastro de Novo Cliente");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(ComponentesUI.criarPainelLogo(), BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtCpf = new JTextField(20);
        JTextField txtTelefone = new JTextField(20);
        JTextField txtEndereco = new JTextField(20);
        JPasswordField txtSenha = new JPasswordField(20);
        JPasswordField txtConfirmaSenha = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nome Completo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtCpf, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(txtTelefone, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Endereço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(txtEndereco, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(txtSenha, gbc);
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Confirmar Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; panel.add(txtConfirmaSenha, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnSalvar = new JButton("Confirmar Cadastro");
        JButton btnVoltar = new JButton("Voltar ao Login");
        ComponentesUI.styleButton(btnSalvar, new Color(40, 167, 69));
        ComponentesUI.styleButton(btnVoltar, new Color(108, 117, 125));
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnVoltar);

        add(panel, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String cpf = txtCpf.getText();
            char[] senha = txtSenha.getPassword();
            char[] confirmaSenha = txtConfirmaSenha.getPassword();

            if (nome.trim().isEmpty() || email.trim().isEmpty() || cpf.trim().isEmpty() || senha.length == 0) {
                JOptionPane.showMessageDialog(this, "Nome, Email, CPF e Senha são obrigatórios.");
                return;
            }
            if (!Arrays.equals(senha, confirmaSenha)) {
                JOptionPane.showMessageDialog(this, "As senhas não coincidem.");
                return;
            }

            String sql = "INSERT INTO Usuario(nome, telefone, endereco, cpf, email, senha) VALUES(?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexaoSQLite.abrirConexao();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, nome);
                pstmt.setString(2, txtTelefone.getText());
                pstmt.setString(3, txtEndereco.getText());
                pstmt.setString(4, cpf);
                pstmt.setString(5, email);
                pstmt.setString(6, new String(senha));
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!");
                dispose();
                new TelaLogin().setVisible(true);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar cliente: " + ex.getMessage());
            } finally {
                Arrays.fill(senha, '0');
                Arrays.fill(confirmaSenha, '0');
            }
        });

        btnVoltar.addActionListener(e -> {
            dispose();
            new TelaLogin().setVisible(true);
        });
    }
}

