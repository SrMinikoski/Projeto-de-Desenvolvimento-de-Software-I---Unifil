package br.com.harmonia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private final String url;

    public Database() throws IOException, SQLException {
        Path directory = Path.of(System.getProperty("user.home"), ".harmonia-studio");
        Files.createDirectories(directory);
        url = "jdbc:sqlite:" + directory.resolve("harmonia.db");
        initialize();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initialize() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS progressions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    musical_key TEXT NOT NULL,
                    scale TEXT NOT NULL,
                    chords TEXT NOT NULL
                )
                """;
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS custom_cadences (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        degrees TEXT NOT NULL
                    )
                    """);
        }
    }

    public List<Progression> findAll() throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        String sql = "SELECT id, name, musical_key, scale, chords FROM progressions ORDER BY name COLLATE NOCASE";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                progressions.add(new Progression(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("musical_key"),
                        result.getString("scale"),
                        result.getString("chords")));
            }
        }
        return progressions;
    }

    public void create(String name, String key, String scale, String chords) throws SQLException {
        String sql = "INSERT INTO progressions(name, musical_key, scale, chords) VALUES (?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, name, key, scale, chords);
            statement.executeUpdate();
        }
    }

    public void update(int id, String name, String key, String scale, String chords) throws SQLException {
        String sql = "UPDATE progressions SET name = ?, musical_key = ?, scale = ?, chords = ? WHERE id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, name, key, scale, chords);
            statement.setInt(5, id);
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM progressions WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public List<CustomCadence> findAllCadences() throws SQLException {
        List<CustomCadence> cadences = new ArrayList<>();
        String sql = "SELECT id, name, degrees FROM custom_cadences ORDER BY name COLLATE NOCASE";
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                List<Integer> degrees = List.of(result.getString("degrees").split(",")).stream()
                        .map(Integer::parseInt)
                        .toList();
                cadences.add(new CustomCadence(result.getInt("id"), result.getString("name"), degrees));
            }
        }
        return cadences;
    }

    public int createCadence(String name, List<Integer> degrees) throws SQLException {
        String sql = "INSERT INTO custom_cadences(name, degrees) VALUES (?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, encodeDegrees(degrees));
            statement.executeUpdate();
            try (Statement query = connection.createStatement();
                 ResultSet result = query.executeQuery("SELECT last_insert_rowid()")) {
                if (!result.next()) {
                    throw new SQLException("Não foi possível obter o identificador da cadência criada.");
                }
                return result.getInt(1);
            }
        }
    }

    public void updateCadence(int id, String name, List<Integer> degrees) throws SQLException {
        String sql = "UPDATE custom_cadences SET name = ?, degrees = ? WHERE id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, encodeDegrees(degrees));
            statement.setInt(3, id);
            statement.executeUpdate();
        }
    }

    public void deleteCadence(int id) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM custom_cadences WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private String encodeDegrees(List<Integer> degrees) {
        return degrees.stream().map(String::valueOf).reduce((left, right) -> left + "," + right).orElse("");
    }

    private void bind(PreparedStatement statement, String name, String key, String scale, String chords)
            throws SQLException {
        statement.setString(1, name);
        statement.setString(2, key);
        statement.setString(3, scale);
        statement.setString(4, chords);
    }
}
