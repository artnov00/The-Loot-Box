package database;

import javax.swing.JOptionPane;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoSQLite {

   
    public static Connection abrirConexao() {
        Connection conexao = null;
        try {
            
            Class.forName("org.sqlite.JDBC");

           
            String userHome = System.getProperty("user.home");

            
            File appDataDir = new File(userHome, ".thelootbox");
            if (!appDataDir.exists()) {
                boolean dirCreated = appDataDir.mkdirs();
                if (dirCreated) {
                    System.out.println("Diretório de dados da aplicação criado em: " + appDataDir.getAbsolutePath());
                } else {
                    
                    throw new SQLException("Falha crítica ao criar o diretório de dados da aplicação em: " + appDataDir.getAbsolutePath());
                }
            }

            
            String dbPath = appDataDir.getAbsolutePath() + File.separator + "lootbox.db";
            String url = "jdbc:sqlite:" + dbPath;

            System.out.println("Conectando ao banco de dados em: " + dbPath);

            
            conexao = DriverManager.getConnection(url);

        } catch (ClassNotFoundException e) {
            
            String mensagem = "Driver JDBC do SQLite não encontrado.\n" +
                    "Verifique se o arquivo .jar do driver foi adicionado às bibliotecas do projeto.";
            System.err.println(mensagem + "\n" + e.getMessage());
            JOptionPane.showMessageDialog(null, mensagem, "Erro Crítico de Conexão", JOptionPane.ERROR_MESSAGE);
            System.exit(1); 

        } catch (SQLException e) {
            
            String mensagem = "Erro ao conectar ou criar o banco de dados SQLite: " + e.getMessage();
            System.err.println(mensagem);
            JOptionPane.showMessageDialog(null, mensagem, "Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
            System.exit(1); 
        }
        return conexao;
    }
}


