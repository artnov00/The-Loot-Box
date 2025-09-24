package screens;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.ConexaoSQLite;

public class TelaLogin extends JFrame {

    public TelaLogin() {
        setTitle("The Loot Box - Acesso");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBackground(Color.WHITE);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        painelPrincipal.add(ComponentesUI.criarPainelLogo());
        painelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Funcionário", criarPainelLoginFuncionario());
        abas.addTab("Cliente", criarPainelLoginCliente());

        painelPrincipal.add(abas);
        add(painelPrincipal, BorderLayout.CENTER);
    }

    private JPanel criarPainelLoginFuncionario() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtLogin = new JTextField(15);
        JPasswordField txtSenha = new JPasswordField(15);
        JButton btnEntrar = new JButton("Entrar");

        gbc.gridx = 0; gbc.gridy = 0; painel.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; painel.add(txtLogin, gbc);
        gbc.gridx = 0; gbc.gridy = 1; painel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; painel.add(txtSenha, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; painel.add(btnEntrar, gbc);

        btnEntrar.addActionListener(e -> {
            String login = txtLogin.getText();
            String senha = new String(txtSenha.getPassword());
            autenticarFuncionario(login, senha);
        });

        return painel;
    }

    private JPanel criarPainelLoginCliente() {
        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtEmail = new JTextField(15);
        JPasswordField txtSenha = new JPasswordField(15);
        JButton btnEntrar = new JButton("Entrar");
        JButton btnCadastrar = new JButton("Não tenho conta, quero me cadastrar");

        gbc.gridx = 0; gbc.gridy = 0; painel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; painel.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 1; painel.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; painel.add(txtSenha, gbc);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotoes.add(btnEntrar);
        painelBotoes.add(btnCadastrar);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; painel.add(painelBotoes, gbc);

        btnEntrar.addActionListener(e -> {
            String email = txtEmail.getText();
            String senha = new String(txtSenha.getPassword());
            autenticarCliente(email, senha);
        });

        btnCadastrar.addActionListener(e -> {
            new TelaCadastroCliente().setVisible(true);
            dispose();
        });

        return painel;
    }

    private void autenticarFuncionario(String login, String senha) {
        String sql = "SELECT nome FROM Funcionario WHERE login = ? AND senha = ?";
        try (Connection conn = ConexaoSQLite.abrirConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String nomeFuncionario = rs.getString("nome");
                JOptionPane.showMessageDialog(this, "Bem-vindo, " + nomeFuncionario + "!");
                new TelaPrincipal().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login ou senha de funcionário inválidos.", "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco de dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void autenticarCliente(String email, String senha) {
        String sql = "SELECT id_usuario, nome FROM Usuario WHERE email = ? AND senha = ?";
        try (Connection conn = ConexaoSQLite.abrirConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int idCliente = rs.getInt("id_usuario");
                String nomeCliente = rs.getString("nome");
                JOptionPane.showMessageDialog(this, "Bem-vindo(a), " + nomeCliente + "!");
                new TelaPrincipalCliente(idCliente, nomeCliente).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Email ou senha de cliente inválidos.", "Erro de Autenticação", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco de dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

