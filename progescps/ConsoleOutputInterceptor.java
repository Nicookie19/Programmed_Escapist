package progescps;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.*;
import javax.swing.text.*;


public class ConsoleOutputInterceptor extends OutputStream {
    private final JTextPane area;
    private final StringBuilder buffer = new StringBuilder();
    
    // Define colors for ANSI codes
    private static final Color RED = new Color(255, 80, 80);
    private static final Color GREEN = new Color(80, 255, 80);
    private static final Color YELLOW = new Color(255, 255, 80);
    private static final Color BLUE = new Color(80, 80, 255);
    private static final Color PURPLE = new Color(255, 80, 255);
    private static final Color WHITE = new Color(255, 255, 255);
    private static final Color GRAY = new Color(180, 180, 180);
    private static final Color DEFAULT_COLOR = WHITE;

    /**
     * Constructs a new ConsoleOutputInterceptor that redirects output to a JTextPane.
     * @param area The JTextPane to append output to.
     */
    public ConsoleOutputInterceptor(JTextPane area) {
        this.area = area;
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (buffer) {
            buffer.append((char) b);
            if (b == '\n') {
                flushBuffer();
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        synchronized (buffer) {
            buffer.append(new String(b, off, len));
            if (buffer.indexOf("\n") >= 0) {
                flushBuffer();
            }
        }
    }

    private void flushBuffer() {
        final String text;
        synchronized (buffer) {
            text = buffer.toString();
            buffer.setLength(0);
        }
        if (text.isEmpty()) return;
        SwingUtilities.invokeLater(() -> {
            // Process ANSI color codes and apply to JTextPane
            processAndAppendText(text);
            area.setCaretPosition(area.getDocument().getLength());
        });
    }
    
    private void processAndAppendText(String text) {
        StyledDocument doc = area.getStyledDocument();
        
        try {
            if (text.contains("\u001B[")) {
                int currentPos = 0;
                Color currentColor = DEFAULT_COLOR;
                
                while (currentPos < text.length()) {
                    int escapeIndex = text.indexOf("\u001B[", currentPos);
                    
                    if (escapeIndex == -1) {
                        // No more escape sequences, append the rest with current color
                        appendWithColor(doc, text.substring(currentPos), currentColor);
                        break;
                    }
                    
                    // Append text before the escape sequence
                    if (escapeIndex > currentPos) {
                        appendWithColor(doc, text.substring(currentPos, escapeIndex), currentColor);
                    }
                    
                    // Process the escape sequence
                    int endIndex = text.indexOf('m', escapeIndex);
                    if (endIndex == -1) {
                        // Malformed escape sequence, just skip it
                        currentPos = escapeIndex + 2;
                        continue;
                    }
                    
                    String colorCode = text.substring(escapeIndex + 2, endIndex);
                    switch (colorCode) {
                        case "31": currentColor = RED; break;
                        case "32": currentColor = GREEN; break;
                        case "33": currentColor = YELLOW; break;
                        case "34": currentColor = BLUE; break;
                        case "35": currentColor = PURPLE; break;
                        case "37": currentColor = WHITE; break;
                        case "90": currentColor = GRAY; break;
                        case "0": currentColor = DEFAULT_COLOR; break;
                    }
                    
                    currentPos = endIndex + 1;
                }
            } else {
                // No color codes, just append with default color
                appendWithColor(doc, text, DEFAULT_COLOR);
            }
        } catch (BadLocationException e) {
            // Fallback to simple append if styling fails
            try {
                doc.insertString(doc.getLength(), text, null);
            } catch (BadLocationException ex) {
                // Last resort
                System.err.println("Error appending text: " + ex.getMessage());
            }
        }
    }
    
    private void appendWithColor(StyledDocument doc, String text, Color color) throws BadLocationException {
        Style style = area.addStyle("ColorStyle", null);
        StyleConstants.setForeground(style, color);
        doc.insertString(doc.getLength(), text, style);
    }

    @Override
    public void flush() throws IOException {
        synchronized (buffer) {
            if (buffer.length() > 0) {
                flushBuffer();
            }
        }
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}