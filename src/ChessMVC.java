import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Clase principal que lanza la aplicación MVC.
 * Crea e interconecta el Modelo, la Vista y el Controlador.
 */
public class ChessMVC {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 1. Crear el Modelo (ahora son clases separadas)
            BoardModel model = new BoardModel();

            // 2. Crear la Vista (y pasarle el modelo para que pueda leerlo)
            ChessView view = new ChessView(model);

            // 3. Crear el Controlador (y pasarle el modelo y la vista)
            ChessController controller = new ChessController(model, view);

            // 4. Conectar la Vista al Controlador (para que los botones funcionen)
            view.setController(controller);

            // 5. Mostrar la Vista
            view.pack();
            view.setLocationRelativeTo(null);
            view.setVisible(true);
        });
    }
}