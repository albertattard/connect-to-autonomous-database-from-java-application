package demo;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

/*
 * Adapted from: https://github.com/oracle-samples/oracle-db-examples/blob/main/java/jdbc/ConnectionSamples/ADBQuickStart.java
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) throws Exception {
        final PoolDataSource dataSource = createDataSource();

        try (Connection connection = dataSource.getConnection()) {
            withConnection(connection);
        } catch (final SQLException e) {
            LOGGER.error("Failed to connect adn execute query", e);
        }
    }

    private static void withConnection(final Connection connection) throws SQLException {
        final String query = """
                SELECT CUST_ID, CUST_FIRST_NAME, CUST_LAST_NAME, CUST_CITY, CUST_CREDIT_LIMIT
                FROM SH.CUSTOMERS
                WHERE ROWNUM < 20
                ORDER BY CUST_ID""";
        LOGGER.info("Listing 20 customers from the sample 'SH' schema");

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            LOGGER.info("Customer Id First Name Last Name City                 Credit Limit");
            LOGGER.info("------------------------------------------------------------------");

            while (resultSet.next()) {
                LOGGER.info("{} {} {} {} {}",
                        String.format("%11s", resultSet.getString(1)),
                        String.format("%-10s", resultSet.getString(2)),
                        String.format("%-9s", resultSet.getString(3)),
                        String.format("%-20s", resultSet.getString(4)),
                        String.format("%-12s", resultSet.getString(5)));
            }
        }
    }

    private static PoolDataSource createDataSource() throws SQLException {
        final ConnectionDetails connectionDetails = ConnectionDetails.readFromConsole();
        LOGGER.info("Connecting to autonomous database: {}", connectionDetails);

        final PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
        dataSource.setURL(connectionDetails.jdbcUrl());
        dataSource.setUser(connectionDetails.value);
        dataSource.setPassword(connectionDetails.password());
        dataSource.setConnectionPoolName("JDBC_UCP_POOL");
        dataSource.setInitialPoolSize(1);
        dataSource.setMinPoolSize(1);
        dataSource.setMaxPoolSize(5);
        return dataSource;
    }

    private record ConnectionDetails(String jdbcUrl, String value, String password) {
        public static ConnectionDetails readFromConsole() {
            Console console = System.console();
            if (console == null) {
                throw new RuntimeException("Console is not available");
            }

            final Path walletPath = readWalletPath(console);
            final String tnsAlias = readTnsAlias(console, walletPath);
            final String username = readUsername(console);
            final String password = readPassword(console);

            final String jdbcUrl = "jdbc:oracle:thin:@%s?TNS_ADMIN=%s".formatted(tnsAlias, walletPath.toAbsolutePath());
            return new ConnectionDetails(jdbcUrl, username, password);
        }

        private static Path readWalletPath(final Console console) throws IllegalArgumentException {
            /* If there is a directory 'wallet', then use this as a default
                option otherwise do not provide a default option */
            final boolean showDefaultOption = Files.isDirectory(Path.of("wallet"));

            final String message = showDefaultOption
                    ? "Wallet directory [wallet]: "
                    : "Wallet directory: ";

            String input = console.readLine(message);
            if (showDefaultOption && isNullOrBlank(input)) {
                input = "wallet";
            } else if (input == null) {
                throw new IllegalArgumentException("Invalid wallet directory. The provided path is not a directory.");
            } else if (input.startsWith("~" + File.separator)) {
                /* Replace ~ with the user home directory */
                input = System.getProperty("user.home") + input.substring(1);
            }

            final Path walletPath = Path.of(input);
            if (!Files.isDirectory(walletPath)) {
                throw new IllegalArgumentException("Invalid wallet directory. The provided path is not a directory.");
            }

            return walletPath;
        }

        private static String readTnsAlias(final Console console, final Path walletPath) {
            final Path tnsnamesPath = walletPath.resolve("tnsnames.ora");
            if (!Files.isRegularFile(tnsnamesPath)) {
                throw new IllegalArgumentException("Invalid wallet directory. The provided directory does not contain the 'tnsnames.ora' file.");
            }

            final List<String> tnsAliases = readTnsAliasesInFile(tnsnamesPath);
            final String input = console.readLine("TNS Alias (one of: %s) [%s]: ", String.join(", ", tnsAliases), tnsAliases.get(0));
            return defaultIfBlank(input, tnsAliases.get(0));
        }

        private static List<String> readTnsAliasesInFile(final Path file) {
            try (Stream<String> lines = Files.lines(file)) {
                final List<String> tnsAliases = lines.filter(line -> line.contains("="))
                        .map(line -> line.split("=", 2)[0].trim())
                        .toList();

                if (tnsAliases.isEmpty()) {
                    throw new IllegalArgumentException("The file %s does not contain TNS aliases".formatted(file.toAbsolutePath()));
                }

                return tnsAliases;
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static String readUsername(final Console console) {
            final String input = console.readLine("Username [Admin]: ");
            return defaultIfBlank(input, "Admin");
        }

        private static String readPassword(final Console console) {
            return String.valueOf(console.readPassword("Password: "));
        }

        private static String defaultIfBlank(final String value, final String defaultValue) {
            return isNullOrBlank(value)
                    ? defaultValue
                    : value;
        }

        private static boolean isNullOrBlank(final String value) {
            return value == null || value.isBlank();
        }

        @Override
        public String toString() {
            return "ConnectionDetails[jdbcUrl=%s, username=%s, password=xxxxxx]".formatted(jdbcUrl, value);
        }
    }
}