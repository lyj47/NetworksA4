/** This class is adapted from:
        http://docs.oracle.com/javase/tutorial/uiswing/examples/components/
        TextDemoProject/src/components/TextDemo.java
 */

import java.awt.*;
import javax.swing.*;
 
public class GUI extends JPanel {
    public static JLabel label;
    public static JTextArea textArea;
    public static JLabel label2;
    public static JTextArea textArea2;
    public static boolean ready = false;

    public GUI() {
        super(new GridBagLayout());
        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);	
        textArea2 = new JTextArea(8, 30);
        textArea2.setEditable(false);	
	GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;

        c.fill = GridBagConstraints.HORIZONTAL;
	label = new JLabel(" Output of the Lookup Thread:");
	add(label,c);
	add(new JScrollPane(textArea),c);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
	label2 = new JLabel(" Output of the File Transfer Thread:");
	add(label2,c);
	add(new JScrollPane(textArea2),c);
    }
 
    static void createAndShowGUI(String title) {
	JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.add(new GUI());
	frame.pack();
        frame.setVisible(true);
	ready = true;
    }

    static void displayLU(String line) {
	textArea.append(line + "\n");
	textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    static void displayFT(String line) {
	textArea2.append(line + "\n");
	textArea2.setCaretPosition(textArea2.getDocument().getLength());
    }

}// GUI class
