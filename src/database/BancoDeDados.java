package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BancoDeDados {

    public static void criarTabelas() {
        String[] sqlStatements = {
                // Tabela Funcionario
                "CREATE TABLE IF NOT EXISTS Funcionario (" +
                        "    id_funcionario INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    login TEXT NOT NULL UNIQUE," +
                        "    nome TEXT NOT NULL," +
                        "    cargo TEXT," +
                        "    salario REAL," +
                        "    senha TEXT NOT NULL" +
                        ");",

                // Tabela Usuario (Cliente)
                "CREATE TABLE IF NOT EXISTS Usuario (" +
                        "    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    nome TEXT NOT NULL," +
                        "    telefone TEXT," +
                        "    endereco TEXT," +
                        "    cpf TEXT UNIQUE," +
                        "    email TEXT NOT NULL UNIQUE," +
                        "    senha TEXT NOT NULL" +
                        ");",

                // Tabela Fornecedor
                "CREATE TABLE IF NOT EXISTS Fornecedor (" +
                        "    id_fornecedor INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    nome TEXT NOT NULL," +
                        "    email TEXT," +
                        "    endereco TEXT," +
                        "    cnpj TEXT UNIQUE," +
                        "    telefone TEXT" +
                        ");",

                // Tabela Categoria
                "CREATE TABLE IF NOT EXISTS Categoria (" +
                        "    id_categoria INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    nome TEXT NOT NULL UNIQUE," +
                        "    descricao TEXT" +
                        ");",

                // Tabela Plataforma
                "CREATE TABLE IF NOT EXISTS Plataforma (" +
                        "    id_plataforma INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    nome TEXT NOT NULL UNIQUE," +
                        "    fabricante TEXT" +
                        ");",

                // Tabela Console
                "CREATE TABLE IF NOT EXISTS Console (" +
                        "    id_console INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    estoque INTEGER NOT NULL DEFAULT 0," +
                        "    modelo TEXT NOT NULL UNIQUE," +
                        "    preco REAL NOT NULL," +
                        "    id_fornecedor INTEGER," +
                        "    FOREIGN KEY(id_fornecedor) REFERENCES Fornecedor(id_fornecedor)" +
                        ");",

                // Tabela Jogo
                "CREATE TABLE IF NOT EXISTS Jogo (" +
                        "    id_jogo INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    nome TEXT NOT NULL," +
                        "    preco REAL NOT NULL," +
                        "    estoque INTEGER NOT NULL DEFAULT 0," +
                        "    id_categoria INTEGER," +
                        "    FOREIGN KEY(id_categoria) REFERENCES Categoria(id_categoria)" +
                        ");",

                // Tabela Associativa Jogo_Plataforma
                "CREATE TABLE IF NOT EXISTS Jogo_Plataforma (" +
                        "    id_jogo INTEGER," +
                        "    id_plataforma INTEGER," +
                        "    PRIMARY KEY (id_jogo, id_plataforma)," +
                        "    FOREIGN KEY(id_jogo) REFERENCES Jogo(id_jogo) ON DELETE CASCADE," +
                        "    FOREIGN KEY(id_plataforma) REFERENCES Plataforma(id_plataforma) ON DELETE CASCADE" +
                        ");",

                // Tabela Venda
                "CREATE TABLE IF NOT EXISTS Venda (" +
                        "    id_venda INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    data_venda TEXT NOT NULL," +
                        "    forma_pagamento TEXT," +
                        "    valor_total REAL NOT NULL," +
                        "    id_funcionario INTEGER," +
                        "    id_usuario INTEGER NOT NULL," +
                        "    FOREIGN KEY(id_funcionario) REFERENCES Funcionario(id_funcionario)," +
                        "    FOREIGN KEY(id_usuario) REFERENCES Usuario(id_usuario)" +
                        ");",

                // Tabela ItemCompra (Itens da Venda)
                "CREATE TABLE IF NOT EXISTS ItemCompra (" +
                        "    id_item_compra INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "    quantidade INTEGER NOT NULL," +
                        "    preco_unitario REAL NOT NULL," +
                        "    id_venda INTEGER NOT NULL," +
                        "    id_jogo INTEGER," +
                        "    id_console INTEGER," +
                        "    FOREIGN KEY(id_venda) REFERENCES Venda(id_venda) ON DELETE CASCADE," +
                        "    FOREIGN KEY(id_jogo) REFERENCES Jogo(id_jogo)," +
                        "    FOREIGN KEY(id_console) REFERENCES Console(id_console)," +
                        "    CHECK (id_jogo IS NOT NULL OR id_console IS NOT NULL)" +
                        ");",

                // Inserir funcionário admin padrão se não existir
                "INSERT OR IGNORE INTO Funcionario (id_funcionario, login, nome, senha) VALUES (1, 'admin', 'Administrador', 'admin');"
        };

        try (Connection conn = ConexaoSQLite.abrirConexao();
             Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("Tabelas criadas com sucesso (ou já existentes).");
        } catch (SQLException e) {
            System.err.println("Erro ao criar as tabelas: " + e.getMessage());
            // Considerar lançar uma exceção em um aplicativo real para parar a execução
            // throw new RuntimeException(e);
        }
    }
}

