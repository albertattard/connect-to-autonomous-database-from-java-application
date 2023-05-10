# Connect to Autonomous Database from Java Application

## Obtain the wallet

1. Go to the [Autonomous Database](https://cloud.oracle.com/db/adb) and access
   the database with which you like to connect.

2. Click on the _Database Connection_ button.
   ![Autonomous Database details - Database connection](./assets/images/Autonomous%20Database%20details%20-%20Database%20connection.png)

3. Click on the _Download wallet_ button.
   ![Autonomous Database details - Download Wallet](./assets/images/Autonomous%20Database%20details%20-%20Download%20Wallet.png)

4. Provide a password to this wallet and click the _Download_ button.
   ![Autonomous Database details - Provide Password and Download Wallet](./assets/images/Autonomous%20Database%20details%20-%20Provide%20Password%20and%20Download%20Wallet.png)

   While it is important to use a strong password, it will not be used in this
   example.

5. Unzip the downloaded file.

   ```shell
   $ unzip Wallet_xxxxxx.zip
   ```

   This file can be unzipped anywhere you like, as long as it is accessible to
   the program. I unzipped mine under the `~/Downloads/wallet` directory. Make
   sure that the directory where the ZIP file was extracted
   (`~/Downloads/wallet`) contains the following files.

   ```shell
   $ tree ~/Downloads/wallet

   ~/Downloads/wallet
   ├── README
   ├── cwallet.sso
   ├── cwallet.sso.lck
   ├── ewallet.p12
   ├── ewallet.p12.lck
   ├── ewallet.pem
   ├── keystore.jks
   ├── ojdbc.properties
   ├── sqlnet.ora
   ├── tnsnames.ora
   └── truststore.jks
   ```

   The path to this directory (`~/Downloads/wallet`) will be passed to the
   program to connect with the autonomous database.

## Commands

- Create fat JAR file containing all dependencies in it (all in one)

  The `fatJar` custom Gradle task will create one (fat) JAR file that includes
  all the dependencies.

  ```shell
  $ ./gradlew fatJar
  ```

  (_Optional_) Verify that the JAR file was created

  ```shell
  $ tree './build/libs/'
  ./build/libs/
  └── app-all-in-one.jar
  ```

- Run the application

  ```shell
  $ java --class-path './build/libs/*' demo.Main
  ```

  The program will ask for some input. Provide the requested information. If the
  provided information is correct, the program will then query the sample `SH`
  schema and prints the results.

  ```
  12:34:56.000 [main] INFO  demo.Main -- Connecting to autonomous database: ConnectionDetails[jdbcUrl=jdbc:oracle:thin:@xxxxxx?TNS_ADMIN=/path/to/wallet, username=xxxxxx, password=xxxxxx]
  12:34:56.138 [main] INFO  demo.Main -- Listing 20 customers from the sample 'SH' schema
  12:34:56.252 [main] INFO  demo.Main -- Customer Id First Name Last Name City                 Credit Limit
  12:34:56.252 [main] INFO  demo.Main -- ------------------------------------------------------------------
  12:34:56.257 [main] INFO  demo.Main --        3228 Abigail    Ruddy     Hoofddorp            7000
  12:34:56.257 [main] INFO  demo.Main --        4117 Abner      Everett   Clermont-l'Herault   15000
  12:34:56.257 [main] INFO  demo.Main --        6783 Abigail    Ruddy     Schimmert            11000
  12:34:56.258 [main] INFO  demo.Main --        7673 Abner      Everett   Schwaebisch Gmuend   11000
  12:34:56.258 [main] INFO  demo.Main --       10338 Abigail    Ruddy     Scheveningen         1500
  12:34:56.258 [main] INFO  demo.Main --       13894 Abigail    Ruddy     Joinville            9000
  12:34:56.258 [main] INFO  demo.Main --       17449 Abigail    Ruddy     Nagoya               9000
  12:34:56.258 [main] INFO  demo.Main --       21005 Abigail    Ruddy     Santos               3000
  12:34:56.258 [main] INFO  demo.Main --       24561 Abigail    Ruddy     Yokohama             7000
  12:34:56.258 [main] INFO  demo.Main --       25470 Abner      Everett   Stuttgart            15000
  12:34:56.365 [main] INFO  demo.Main --       28116 Abigail    Ruddy     Haarlem              11000
  12:34:56.365 [main] INFO  demo.Main --       31671 Abigail    Ruddy     Bolton               1500
  12:34:56.365 [main] INFO  demo.Main --       35227 Abigail    Ruddy     Lelystad             9000
  12:34:56.365 [main] INFO  demo.Main --       36117 Abner      Everett   Wolverhampton        15000
  12:34:56.365 [main] INFO  demo.Main --       39672 Abner      Everett   Murnau               11000
  12:34:56.366 [main] INFO  demo.Main --       43228 Abner      Everett   Los Angeles          7000
  12:34:56.366 [main] INFO  demo.Main --       47006 Abner      Everett   Montara              11000
  12:34:56.366 [main] INFO  demo.Main --       49671 Abigail    Ruddy     Ede                  1500
  12:34:56.366 [main] INFO  demo.Main --       50561 Abner      Everett   Neuss                7000
  ```

  Otherwise, an error is printed depending on the error type. Following is an
  example of the error printed when provided a wrong password.

  ```
  12:34:56.000 [main] INFO  demo.Main -- Connecting to autonomous database: ConnectionDetails[jdbcUrl=jdbc:oracle:thin:@xxxxxx?TNS_ADMIN=/path/to/wallet, username=xxxxxx, password=xxxxxx]
  12:34:56.999 [main] ERROR demo.Main -- Failed to connect adn execute query
  java.sql.SQLException: UCP-0: Unable to start the Universal Connection Pool
      at oracle.ucp.util.UCPErrorHandler.newSQLException(UCPErrorHandler.java:399)
      at oracle.ucp.util.UCPErrorHandler.throwSQLException(UCPErrorHandler.java:163)
  ...
  ```

  Verify that the correct credentials are used and try again.

## Pending

1. Add the terraform scripts that create the Autonomous Database
