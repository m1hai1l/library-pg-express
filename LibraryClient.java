import java.awt.*; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;

public class LibraryClient extends JFrame {
    private JTextField titleField;
    private JTextField authorField;
    private JTextField genreField;
    private JTextField yearField;
    private JTextArea resultArea;

    public LibraryClient() {
        setTitle("Library Client");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("Genre:"));
        genreField = new JTextField();
        inputPanel.add(genreField);
        inputPanel.add(new JLabel("Year:"));
        yearField = new JTextField();
        inputPanel.add(yearField);

        JPanel buttonPanel = new JPanel();
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new SearchButtonListener());
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new AddButtonListener());
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new UpdateButtonListener());
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new DeleteButtonListener());

        buttonPanel.add(searchButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();

            StringBuilder urlBuilder = new StringBuilder("http://localhost:4000/books?");
            if (!title.isEmpty()) {
                urlBuilder.append("title=").append(title).append("&");
            }
            if (!author.isEmpty()) {
                urlBuilder.append("author=").append(author);
            }

            try {
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    resultArea.setText("Server returned error code: " + responseCode);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON manually
                String rawData = response.toString();
                rawData = rawData.substring(1, rawData.length() - 1); // Remove [ and ]
                String[] books = rawData.split("},");
                StringBuilder resultText = new StringBuilder();
                for (String book : books) {
                    book = book.replace("{", "").replace("}", "");
                    String[] fields = book.split(",");
                    for (String field : fields) {
                        String[] keyValue = field.split(":");
                        String key = keyValue[0].replace("\"", "").trim();
                        String value = keyValue[1].replace("\"", "").trim();
                        resultText.append(key).append(": ").append(value).append(" ");
                    }
                    resultText.append("\n");
                }
                resultArea.setText(resultText.toString());
            } catch (IOException ex) {
                resultArea.setText("Error: " + ex.getMessage());
            } catch (Exception ex) {
                resultArea.setText("Invalid response from server: " + ex.getMessage());
            }
        }
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();
            String year = yearField.getText();

            if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || year.isEmpty()) {
                resultArea.setText("Error: All fields are required.");
                return;
            }

            try {
                URL url = new URL("http://localhost:4000/books");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = String.format("{\"title\":\"%s\",\"author\":\"%s\",\"genre\":\"%s\",\"year\":%s}", 
                                                        title, author, genre, year);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    resultArea.setText("Book added successfully.");
                } else {
                    resultArea.setText("Error: Unable to add book. Response code: " + responseCode);
                }
            } catch (IOException ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        }
    }

    private class UpdateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog("Enter book ID to update:");
            if (id == null || id.isEmpty()) {
                resultArea.setText("Error: ID is required.");
                return;
            }

            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();
            String year = yearField.getText();

            if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || year.isEmpty()) {
                resultArea.setText("Error: All fields are required.");
                return;
            }

            try {
                URL url = new URL("http://localhost:4000/books/" + id);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonInputString = String.format("{\"title\":\"%s\",\"author\":\"%s\",\"genre\":\"%s\",\"year\":%s}", 
                                                        title, author, genre, year);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    resultArea.setText("Book updated successfully.");
                } else {
                    resultArea.setText("Error: Unable to update book. Response code: " + responseCode);
                }
            } catch (IOException ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        }
    }

    private class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog("Enter book ID to delete:");
            if (id == null || id.isEmpty()) {
                resultArea.setText("Error: ID is required.");
                return;
            }

            try {
                URL url = new URL("http://localhost:4000/books/" + id);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    resultArea.setText("Book deleted successfully.");
                } else {
                    resultArea.setText("Error: Unable to delete book. Response code: " + responseCode);
                }
            } catch (IOException ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryClient client = new LibraryClient();
            client.setVisible(true);
        });
    }
}