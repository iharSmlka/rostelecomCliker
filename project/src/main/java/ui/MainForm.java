package ui;

import lombok.extern.slf4j.Slf4j;
import session.SessionController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class MainForm extends JFrame {
    private JTextField LoginBox;
    private JTextField passwordBox;
    private JTextField agentsCountBox;
    private JButton okBtn;
    private JTextField toChangeBox;
    private JTextField forChangeBox;
    private JButton forChangeBtnChoose;
    private JButton toChangeBtnChoose;
    private JPanel Content;
    private JButton logBtn;

    public MainForm() {
        this.setContentPane(Content);
        setTitle("Main");
        setBounds(300, 90, 900, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {

            }

            public void windowClosing(WindowEvent e) {
                SessionController.getInstance().emergencyEndSession();
            }
        });
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SessionController.getInstance().startSession(
                            Integer.parseInt(agentsCountBox.getText()),
                            LoginBox.getText(),
                            passwordBox.getText(),
                            forChangeBox.getText(),
                            toChangeBox.getText()
                                    );
                    JOptionPane.showMessageDialog(null, "Закончено", "Info", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        forChangeBtnChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile(forChangeBox);
            }
        });
        toChangeBtnChoose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile(toChangeBox);
            }
        });
        logBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SessionController.getInstance().createLogFile();
                } catch (Exception exception) {
                    log.error("Ошибка записи лога ", exception);
                    JOptionPane.showMessageDialog(null, exception.getMessage(), "Error", JOptionPane.INFORMATION_MESSAGE);

                }
            }
        });
    }

    private void chooseFile(JTextField textField) {
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textField.setText(fc.getSelectedFile().getAbsolutePath());;
        } else {
            log.error("Файл не выбран");
        }
    }


}
