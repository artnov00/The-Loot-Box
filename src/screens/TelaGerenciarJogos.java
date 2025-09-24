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

public class TelaGerenciarJogos extends JPanel {

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private static class CategoriaItem {
        final int id; final String nome;
        CategoriaItem(int id, String nome) { this.id = id; this.nome = nome; }
        public int getId() { return id; }
        @Override public String toString() { return nome; }
    }

    private static class PlataformaItem {
        final int id; final String nome;
        PlataformaItem(int id, String nome) { this.id = id; this.nome = nome; }
        public int getId() { return id; }
        @Override public String toString() { return nome; }
    }

    public TelaGerenciarJogos() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "Preço", "Estoque", "Categoria"};
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

        carregarJogos();

        btnAdicionar.addActionListener(e -> showJogoDialog(null));
        btnEditar.addActionListener(e -> editarJogo());
        btnExcluir.addActionListener(e -> excluirJogo());
    }

    private void carregarJogos() {
        modeloTabela.setRowCount(0);
        String sql = "SELECT j.id_jogo, j.nome, j.preco, j.estoque, c.nome as categoria_nome " +
                "FROM Jogo j JOIN Categoria c ON j.id_categoria = c.id_categoria ORDER BY j.nome";
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id_jogo"));
                row.add(rs.getString("nome"));
                row.add(String.format("R$ %.2f", rs.getBigDecimal("preco")));
                row.add(rs.getInt("estoque"));
                row.add(rs.getString("categoria_nome"));
                modeloTabela.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar jogos: " + e.getMessage());
        }
    }

    private void showJogoDialog(Integer jogoId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setTitle(jogoId == null ? "Adicionar Jogo" : "Editar Jogo");
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField();
        JTextField txtPreco = new JTextField();
        JSpinner spinnerEstoque = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        JComboBox<CategoriaItem> cbCategorias = new JComboBox<>(getCategorias().toArray(new CategoriaItem[0]));
        JList<PlataformaItem> listPlataformas = new JList<>(getPlataformas().toArray(new PlataformaItem[0]));
        listPlataformas.setVisibleRowCount(5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Preço:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtPreco, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Estoque:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(spinnerEstoque, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(cbCategorias, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHEAST; panel.add(new JLabel("Plataformas:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(new JScrollPane(listPlataformas), gbc);

        if (jogoId != null) {
            carregarDadosJogoParaEdicao(jogoId, txtNome, txtPreco, spinnerEstoque, cbCategorias, listPlataformas);
        }

        JButton btnSalvar = new JButton("Salvar");
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnSalvar, BorderLayout.SOUTH);

        btnSalvar.addActionListener(e -> {
            salvarJogo(jogoId, txtNome.getText(), txtPreco.getText(), (Integer) spinnerEstoque.getValue(),
                    (CategoriaItem) cbCategorias.getSelectedItem(), listPlataformas.getSelectedValuesList(), dialog);
        });

        dialog.setVisible(true);
    }

    private void carregarDadosJogoParaEdicao(int jogoId, JTextField txtNome, JTextField txtPreco, JSpinner spinnerEstoque, JComboBox<CategoriaItem> cbCategorias, JList<PlataformaItem> listPlataformas) {
        try (Connection conn = ConexaoSQLite.abrirConexao()) {
            String sqlJogo = "SELECT nome, preco, estoque, id_categoria FROM Jogo WHERE id_jogo = ?";
            try (PreparedStatement pstmtJogo = conn.prepareStatement(sqlJogo)) {
                pstmtJogo.setInt(1, jogoId);
                ResultSet rsJogo = pstmtJogo.executeQuery();
                if (rsJogo.next()) {
                    txtNome.setText(rsJogo.getString("nome"));
                    txtPreco.setText(rsJogo.getBigDecimal("preco").toString());
                    spinnerEstoque.setValue(rsJogo.getInt("estoque"));
                    int idCategoria = rsJogo.getInt("id_categoria");
                    for (int i = 0; i < cbCategorias.getItemCount(); i++) {
                        if (cbCategorias.getItemAt(i).getId() == idCategoria) {
                            cbCategorias.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            String sqlPlataformas = "SELECT id_plataforma FROM Jogo_Plataforma WHERE id_jogo = ?";
            try (PreparedStatement pstmtPlataformas = conn.prepareStatement(sqlPlataformas)) {
                pstmtPlataformas.setInt(1, jogoId);
                ResultSet rsPlataformas = pstmtPlataformas.executeQuery();
                List<Integer> idsPlataformas = new ArrayList<>();
                while (rsPlataformas.next()) idsPlataformas.add(rsPlataformas.getInt("id_plataforma"));

                ListModel<PlataformaItem> model = listPlataformas.getModel();
                List<Integer> indicesParaSelecionar = new ArrayList<>();
                for (int i = 0; i < model.getSize(); i++) {
                    if (idsPlataformas.contains(model.getElementAt(i).getId())) {
                        indicesParaSelecionar.add(i);
                    }
                }
                listPlataformas.setSelectedIndices(indicesParaSelecionar.stream().mapToInt(i -> i).toArray());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do jogo: " + e.getMessage());
        }
    }

    private void salvarJogo(Integer jogoId, String nome, String precoStr, int estoque, CategoriaItem categoria, List<PlataformaItem> plataformas, JDialog dialog) {
        if (nome.trim().isEmpty() || precoStr.trim().isEmpty() || categoria == null) {
            JOptionPane.showMessageDialog(dialog, "Nome, Preço e Categoria são obrigatórios.");
            return;
        }

        BigDecimal preco;
        try { preco = new BigDecimal(precoStr.replace(",", ".")); } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Formato de preço inválido."); return;
        }

        Connection conn = null;
        try {
            conn = ConexaoSQLite.abrirConexao();
            conn.setAutoCommit(false);

            int currentJogoId;
            if (jogoId == null) {
                String sqlInsert = "INSERT INTO Jogo(nome, preco, estoque, id_categoria) VALUES(?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, nome); pstmt.setBigDecimal(2, preco); pstmt.setInt(3, estoque); pstmt.setInt(4, categoria.getId());
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) { currentJogoId = rs.getInt(1); }
                }
            } else {
                String sqlUpdate = "UPDATE Jogo SET nome=?, preco=?, estoque=?, id_categoria=? WHERE id_jogo=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                    pstmt.setString(1, nome); pstmt.setBigDecimal(2, preco); pstmt.setInt(3, estoque); pstmt.setInt(4, categoria.getId()); pstmt.setInt(5, jogoId);
                    pstmt.executeUpdate();
                }
                currentJogoId = jogoId;

                String sqlDeleteAssoc = "DELETE FROM Jogo_Plataforma WHERE id_jogo = ?";
                try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteAssoc)) {
                    pstmtDelete.setInt(1, currentJogoId);
                    pstmtDelete.executeUpdate();
                }
            }

            String sqlAssoc = "INSERT INTO Jogo_Plataforma(id_jogo, id_plataforma) VALUES(?, ?)";
            try (PreparedStatement pstmtAssoc = conn.prepareStatement(sqlAssoc)) {
                for (PlataformaItem plataforma : plataformas) {
                    pstmtAssoc.setInt(1, currentJogoId); pstmtAssoc.setInt(2, plataforma.getId());
                    pstmtAssoc.addBatch();
                }
                pstmtAssoc.executeBatch();
            }

            conn.commit();
            carregarJogos();
            dialog.dispose();

        } catch (SQLException ex) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); } }
            JOptionPane.showMessageDialog(dialog, "Erro ao salvar jogo: " + ex.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    private void editarJogo() {
        if (tabela.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um jogo para editar.");
            return;
        }
        int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
        showJogoDialog(id);
    }

    private void excluirJogo() {
        if (tabela.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um jogo para excluir.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deseja realmente excluir este jogo?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabela.getValueAt(tabela.getSelectedRow(), 0);
            try (Connection conn = ConexaoSQLite.abrirConexao(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Jogo WHERE id_jogo = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                carregarJogos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir jogo: " + e.getMessage());
            }
        }
    }

    private List<CategoriaItem> getCategorias() {
        List<CategoriaItem> lista = new ArrayList<>();
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id_categoria, nome FROM Categoria ORDER BY nome")) {
            while (rs.next()) lista.add(new CategoriaItem(rs.getInt("id_categoria"), rs.getString("nome")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private List<PlataformaItem> getPlataformas() {
        List<PlataformaItem> lista = new ArrayList<>();
        try (Connection conn = ConexaoSQLite.abrirConexao(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id_plataforma, nome FROM Plataforma ORDER BY nome")) {
            while (rs.next()) lista.add(new PlataformaItem(rs.getInt("id_plataforma"), rs.getString("nome")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}

