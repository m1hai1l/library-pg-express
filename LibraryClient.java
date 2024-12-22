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
    private String password;

    public LibraryClient() {
        // Prompt for password at startup
        password = JOptionPane.showInputDialog(this, "Enter password:", "Authentication", JOptionPane.PLAIN_MESSAGE);

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

                // Parse JSON manually and format the response
                String formattedResponse = formatResponse(response.toString());
                resultArea.setText(formattedResponse);
            } catch (IOException ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        }

        private String formatResponse(String jsonResponse) {
            StringBuilder formatted = new StringBuilder();
            try {
                // Remove square brackets if it's a JSON array
                jsonResponse = jsonResponse.trim();
                if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
                    jsonResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
                }

                // Split objects if they are separated by commas
                String[] objects = jsonResponse.split("\\},\\{");
                for (String obj : objects) {
                    obj = obj.trim();

                    // Remove braces
                    if (obj.startsWith("{")) obj = obj.substring(1);
                    if (obj.endsWith("}")) obj = obj.substring(0, obj.length() - 1);

                    // Split key-value pairs
                    String[] pairs = obj.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            String value = keyValue[1].trim().replace("\"", "");
                            formatted.append(key).append(": ").append(value).append("\n");
                        }
                    }
                    formatted.append("\n");
                }
            } catch (Exception ex) {
                formatted.append("Invalid JSON response: ").append(jsonResponse);
            }
            return formatted.toString();
        }
    }

    private abstract class AuthenticatedButtonListener implements ActionListener {
        protected boolean isAuthenticated() {
            return password != null && password.equals("1234");
        }

        protected void showAccessDeniedMessage() {
            resultArea.setText("Access denied: Invalid password.");
        }
    }

    private class AddButtonListener extends AuthenticatedButtonListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isAuthenticated()) {
                showAccessDeniedMessage();
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
                URL url = new URL("http://localhost:4000/books?password=" + password);
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

    private class UpdateButtonListener extends AuthenticatedButtonListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isAuthenticated()) {
                showAccessDeniedMessage();
                return;
            }

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
                URL url = new URL("http://localhost:4000/books/" + id + "?password=" + password);
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

    private class DeleteButtonListener extends AuthenticatedButtonListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isAuthenticated()) {
                showAccessDeniedMessage();
                return;
            }

            String id = JOptionPane.showInputDialog("Enter book ID to delete:");
            if (id == null || id.isEmpty()) {
                resultArea.setText("Error: ID is required.");
                return;
            }

            try {
                URL url = new URL("http://localhost:4000/books/" + id + "?password=" + password);
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


