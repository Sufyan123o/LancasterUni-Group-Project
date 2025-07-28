package g82;

import javax.swing.SwingUtilities;
public class App 
{
    public static void main( String[] args )
    {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
        });
    }
}

