package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/** Centralizes the blue palette and paints the translucent Swing surfaces. */
public final class Theme {
    static final Color BLUE = new Color(31, 84, 166);
    static final Color INK = new Color(23, 38, 60);
    static final Color LINE = new Color(204, 219, 237);
    static final Color PALE = new Color(238, 245, 254);

    /** Applies the palette to standard Swing controls and dialogs. */
    public static void install() {
        FontUIResource body = new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 14);
        for (String control : new String[] {"Label", "Button", "TextField", "PasswordField",
                "ComboBox", "Table", "TableHeader", "TabbedPane", "OptionPane"}) {
            UIManager.put(control + ".font", body);
            UIManager.put(control + ".foreground", new ColorUIResource(INK));
        }
        UIManager.put("Panel.background", new ColorUIResource(Color.WHITE));
        UIManager.put("Table.selectionBackground", new ColorUIResource(BLUE));
        UIManager.put("Table.selectionForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("Button.focus", new ColorUIResource(BLUE));
        UIManager.put("TabbedPane.focus", new ColorUIResource(BLUE));
        UIManager.put("TabbedPane.selected", new ColorUIResource(Color.WHITE));
        UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(9, 17, 9, 17));
        UIManager.put("TabbedPane.selectedTabPadInsets", new InsetsUIResource(0, 0, 0, 0));
        UIManager.put("TabbedPane.contentBorderInsets", new InsetsUIResource(8, 0, 0, 0));
    }

    /** Creates the pale blue background with a fine, low-contrast grain. */
    static JPanel backdrop(LayoutManager layout) {
        return new JPanel(layout) {
            /** Paints the blue wash and a subtle dotted texture. */
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D paint = (Graphics2D) graphics.create();
                paint.setPaint(new GradientPaint(0, 0, new Color(203, 224, 250),
                        getWidth(), getHeight(), new Color(246, 249, 253)));
                paint.fillRect(0, 0, getWidth(), getHeight());
                paint.setColor(new Color(31, 84, 166, 22));
                for (int y = 4; y < getHeight(); y += 5) {
                    for (int x = 4 + y % 3; x < getWidth(); x += 5) {
                        paint.fillRect(x, y, 1, 1);
                    }
                }
                paint.dispose();
            }
        };
    }

    /** Creates a frosted card that lets the underlying background show through. */
    static JPanel glass(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            /** Paints soft depth, a translucent fill and a light rim. */
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D paint = (Graphics2D) graphics.create();
                paint.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                paint.setColor(new Color(26, 63, 111, 26));
                paint.fillRoundRect(1, 4, getWidth() - 2, getHeight() - 4, 23, 23);
                paint.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 242),
                        0, getHeight(), new Color(255, 255, 255, 151)));
                paint.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 23, 23);
                paint.setColor(new Color(255, 255, 255, 245));
                paint.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 4, 23, 23);
                paint.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /** Styles the tabs while retaining Swing keyboard navigation. */
    static void styleTabs(JTabbedPane tabs) {
        tabs.setOpaque(false);
        tabs.setUI(new BasicTabbedPaneUI() {
            /** Paints a rounded highlight for the selected tab. */
            @Override
            protected void paintTabBackground(Graphics graphics, int placement, int index,
                    int x, int y, int width, int height, boolean selected) {
                if (selected) {
                    Graphics2D paint = (Graphics2D) graphics.create();
                    paint.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    paint.setColor(new Color(218, 233, 253));
                    paint.fillRoundRect(x + 2, y + 2, width - 4, height - 4, 12, 12);
                    paint.dispose();
                }
            }

            /** Uses spacing in place of the standard tab outline. */
            @Override
            protected void paintTabBorder(Graphics graphics, int placement, int index,
                    int x, int y, int width, int height, boolean selected) {
            }

            /** Leaves the glass visible between the tab bar and its content. */
            @Override
            protected void paintContentBorder(Graphics graphics, int placement, int selectedIndex) {
            }
        });
    }

    /** Creates a labeled action with a clear hover and keyboard focus state. */
    static JButton button(String label, boolean primary) {
        JButton button = new JButton(label) {
            /** Paints the rounded action surface behind standard button text. */
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D paint = (Graphics2D) graphics.create();
                paint.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = primary ? BLUE : Color.WHITE;
                if (getModel().isPressed()) {
                    fill = primary ? new Color(22, 62, 126) : new Color(219, 233, 251);
                } else if (getModel().isRollover()) {
                    fill = primary ? new Color(24, 73, 148) : PALE;
                }
                paint.setColor(fill);
                paint.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 13, 13);
                paint.setColor(isFocusOwner() ? (primary ? Color.WHITE : BLUE) : LINE);
                paint.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                paint.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 11, 11);
                paint.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setForeground(primary ? Color.WHITE : BLUE);
        return button;
    }
}
