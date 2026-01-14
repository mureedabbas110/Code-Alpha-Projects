import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class GradeTrackerGUI extends JFrame {

    private ArrayList<Double> grades = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();

    private JTextField nameField, gradeField;
    private JTextArea outputArea;
    private JTable table;
    private DefaultTableModel tableModel;

    public GradeTrackerGUI() {
        setTitle("Student Grade Tracker");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // ===== Input Panel =====
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 10, 5));

        inputPanel.add(new JLabel("Student Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        JButton addButton = new JButton("Add Student");
        inputPanel.add(addButton);

        inputPanel.add(new JLabel("Grade (0–100):"));
        gradeField = new JTextField();
        inputPanel.add(gradeField);

        JButton clearButton = new JButton("Clear / Reset");
        inputPanel.add(clearButton);

        add(inputPanel, BorderLayout.NORTH);

        // ===== Table =====
        tableModel = new DefaultTableModel(new String[]{"Student Name", "Grade"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== Bottom Panel =====
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JButton summaryButton = new JButton("Show Summary");
        bottomPanel.add(summaryButton, BorderLayout.NORTH);

        outputArea = new JTextArea(6, 20);
        outputArea.setEditable(false);
        bottomPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Button Actions =====
        addButton.addActionListener(e -> addStudent());
        summaryButton.addActionListener(e -> showSummary());
        clearButton.addActionListener(e -> clearAll());
    }

    private void addStudent() {
        String name = nameField.getText().trim();

        try {
            double grade = Double.parseDouble(gradeField.getText());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter student name.");
                return;
            }

            if (grade < 0 || grade > 100) {
                JOptionPane.showMessageDialog(this, "Grade must be between 0 and 100.");
                return;
            }

            names.add(name);
            grades.add(grade);
            tableModel.addRow(new Object[]{name, grade});

            nameField.setText("");
            gradeField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for grade.");
        }
    }

    private void showSummary() {
        if (grades.isEmpty()) {
            outputArea.setText("No student data available.\n");
            return;
        }

        double total = 0;
        for (double g : grades) {
            total += g;
        }

        double average = total / grades.size();
        double highest = Collections.max(grades);
        double lowest = Collections.min(grades);

        outputArea.setText("--- Summary Report of All Students ---\n");

        for (int i = 0; i < names.size(); i++) {
            outputArea.append(names.get(i) + " : " + grades.get(i) + "\n");
        }

        outputArea.append("\nTotal Students: " + grades.size() + "\n");
        outputArea.append(String.format("Average Score: %.2f\n", average));
        outputArea.append("Highest Score: " + highest + "\n");
        outputArea.append("Lowest Score: " + lowest + "\n");
    }

    private void clearAll() {
        names.clear();
        grades.clear();
        tableModel.setRowCount(0);
        outputArea.setText("");
        nameField.setText("");
        gradeField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GradeTrackerGUI().setVisible(true));
    }
}
