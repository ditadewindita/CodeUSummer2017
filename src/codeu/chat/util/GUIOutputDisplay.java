package codeu.chat.util;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Jiahui Chen on 8/2/2017.
 * Creates a custom output stream that will display to the GUI's text panel.
 */
class GUIOutputDisplay extends OutputStream {
    private JTextArea textDisplay;

    public GUIOutputDisplay(JTextArea textArea){
        textDisplay = textArea;
    }

    //redirects console/terminal output to JTextArea
    @Override
    public void write(int b) throws IOException {
        textDisplay.append(String.valueOf((char) b));
    }
}
