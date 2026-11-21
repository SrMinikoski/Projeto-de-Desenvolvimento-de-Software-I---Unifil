package br.com.harmonia;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.sql.SQLException;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
                System.err.println("Não foi possível aplicar o tema do sistema: " + exception.getMessage());
            }
            try {
                new MainFrame().setVisible(true);
            } catch (IOException | SQLException exception) {
                JOptionPane.showMessageDialog(null,
                        "Não foi possível iniciar o banco de dados local.\n" + exception.getMessage(),
                        "Erro ao iniciar Harmonia Studio", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
