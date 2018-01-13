package com.fsm.gui;

import static com.fsm.logic.Elevator.OPEN_COMMAND;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class ElevatorDashboard extends JFrame {

    private static final int MAX_LEVEL_NUMBER = 20;

    private final int levelNumber;

    private ActionListener internalButtonsListener;
    private ActionListener externalButtonsListener;

    private JToggleButton[] internalLevelButtons;
    private JToggleButton[] externalLevelButtons;
    private JToggleButton openButton;

    private JLabel statusField;

    public ElevatorDashboard(int levelNumber) throws HeadlessException {
        this.levelNumber = levelNumber;
    }

    /**
     * Launch the form
     * 
     * @param internalButtonsListener
     *            - listener for internal buttons
     * @param externalButtonsListener
     *            - listener for external buttons
     */
    public void start(@Nonnull ActionListener internalButtonsListener,
            @Nonnull ActionListener externalButtonsListener) {
        this.internalButtonsListener = internalButtonsListener;
        this.externalButtonsListener = externalButtonsListener;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addComponentToPane(getContentPane());
        pack();
        setVisible(true);
        setResizable(false);
    }

    /**
     * Deselect buttons of required level
     * 
     * @param levelNumber
     *            - required level
     */
    public void deselectLevelButtons(int levelNumber) {
        internalLevelButtons[levelNumber - 1].setSelected(false);
        externalLevelButtons[levelNumber - 1].setSelected(false);
    }

    /**
     * Deselect emergency open button
     */
    public void deselectOpenButton() {
        openButton.setSelected(false);
    }

    /**
     * Set text of current elevator state
     * 
     * @param text
     *            - new text
     */
    public void setStatusText(@Nonnull String text) {
        statusField.setText(text);
    }

    private void addComponentToPane(@Nonnull Container pane) {
        JPanel internalButtonsPanel = new JPanel(new GridLayout(11, 2));

        internalLevelButtons = generateLevelButtons(levelNumber, internalButtonsListener);
        Stream.of(internalLevelButtons).forEach(internalButtonsPanel::add);

        openButton = generateButton("open", internalButtonsListener, OPEN_COMMAND);
        internalButtonsPanel.add(openButton);

        JPanel externalButtonsPanel = new JPanel(new GridLayout(11, 2));

        externalLevelButtons = generateLevelButtons(levelNumber, externalButtonsListener);
        Stream.of(externalLevelButtons).forEach(externalButtonsPanel::add);
        JToggleButton hiddenButton = generateButton("open", (e) -> {});
        hiddenButton.setVisible(false);
        externalButtonsPanel.add(hiddenButton);

        JPanel statusPanel = new JPanel();
        statusField = new JLabel("Status");
        statusPanel.add(statusField);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel internalButtonCard = new JPanel(new CardLayout(5, 5));
        internalButtonCard.add(internalButtonsPanel, BorderLayout.NORTH);

        JPanel externalButtonsCard = new JPanel(new CardLayout(5, 5));
        externalButtonsCard.add(externalButtonsPanel);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel mainLabel = new JLabel("Elevator");
        mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
        labelPanel.add(mainLabel, BorderLayout.NORTH);
        labelPanel.add(new JLabel("Internal buttons"), BorderLayout.WEST);
        labelPanel.add(new JLabel("External buttons"), BorderLayout.EAST);

        pane.add(labelPanel, BorderLayout.PAGE_START);
        pane.add(internalButtonCard, BorderLayout.WEST);
        pane.add(externalButtonsCard, BorderLayout.EAST);
        pane.add(statusPanel, BorderLayout.SOUTH);
    }

    @Nonnull
    private static JToggleButton[] generateLevelButtons(int buttonsNumber, @Nonnull ActionListener actionListener) {
        JToggleButton[] buttons = new JToggleButton[MAX_LEVEL_NUMBER];
        for (int i = 0; i < MAX_LEVEL_NUMBER; i++) {
            buttons[i] = generateButton(String.valueOf(i + 1), actionListener);
            if (i >= buttonsNumber) {
                buttons[i].setEnabled(false);
            }
        }
        return buttons;
    }

    @Nonnull
    private static JToggleButton generateButton(@Nonnull String name, @Nonnull ActionListener actionListener) {
        return generateButton(name, actionListener, null);
    }

    @Nonnull
    private static JToggleButton generateButton(@Nonnull String name, @Nonnull ActionListener actionListener,
            @Nullable String actionCommand) {
        JToggleButton button = new JToggleButton(name);
        button.addActionListener(actionListener);
        if (actionCommand != null) {
            button.setActionCommand(actionCommand);
        }
        return button;
    }
}
