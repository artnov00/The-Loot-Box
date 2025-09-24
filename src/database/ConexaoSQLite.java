package database;

import javax.swing.JOptionPane;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoSQLite {

    /**
     * Abre e retorna uma nova conexão com o banco de dados SQLite.
     * Este método garante que o arquivo do banco de dados seja criado em um diretório
     * dedicado no home do usuário, se não existir.
     * @return Uma nova instância de Connection com o banco de dados.
     */
    public static Connection abrirConexao() {
        Connection conexao = null;
        try {
            // Carrega o driver JDBC do SQLite.
            Class.forName("org.sqlite.JDBC");

            // 1. Obter o diretório "home" do usuário (ex: C:\Users\SeuNome)
            String userHome = System.getProperty("user.home");

            // 2. Criar um diretório para os dados da aplicação (se não existir)
            // O ponto no início torna a pasta oculta em sistemas Linux/macOS
            File appDataDir = new File(userHome, ".thelootbox");
            if (!appDataDir.exists()) {
                boolean dirCreated = appDataDir.mkdirs();
                if (dirCreated) {
                    System.out.println("Diretório de dados da aplicação criado em: " + appDataDir.getAbsolutePath());
                } else {
                    // Se não conseguir criar o diretório, é um erro crítico de permissão.
                    throw new SQLException("Falha crítica ao criar o diretório de dados da aplicação em: " + appDataDir.getAbsolutePath());
                }
            }

            // 3. Definir o caminho completo e a URL de conexão para o arquivo do banco de dados
            String dbPath = appDataDir.getAbsolutePath() + File.separator + "lootbox.db";
            String url = "jdbc:sqlite:" + dbPath;

            System.out.println("Conectando ao banco de dados em: " + dbPath);

            // 4. Cria a conexão. O driver SQLite criará o arquivo neste caminho se ele não existir.
            conexao = DriverManager.getConnection(url);

        } catch (ClassNotFoundException e) {
            // Erro fatal: o driver não foi encontrado no projeto.
            String mensagem = "Driver JDBC do SQLite não encontrado.\n" +
                    "Verifique se o arquivo .jar do driver foi adicionado às bibliotecas do projeto.";
            System.err.println(mensagem + "\n" + e.getMessage());
            JOptionPane.showMessageDialog(null, mensagem, "Erro Crítico de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Encerra a aplicação.

        } catch (SQLException e) {
            // Erro fatal: problema com o arquivo do banco de dados, a URL ou permissões.
            String mensagem = "Erro ao conectar ou criar o banco de dados SQLite: " + e.getMessage();
            System.err.println(mensagem);
            JOptionPane.showMessageDialog(null, mensagem, "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Encerra a aplicação.
        }
        return conexao;
    }
}

