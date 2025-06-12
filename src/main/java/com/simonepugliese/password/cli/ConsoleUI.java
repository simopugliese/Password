package com.simonepugliese.password.cli;

import com.simonepugliese.password.db.CredentialDAO;
import com.simonepugliese.password.db.DatabaseManager;
import com.simonepugliese.password.model.PasswordModel;
import com.simonepugliese.password.model.credential.*;
import com.simonepugliese.password.service.AuthService;
import com.simonepugliese.password.model.CryptoManager;
import com.simonepugliese.password.model.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);

    private final Scanner scanner = new Scanner(System.in);
    private CredentialDAO dao;
    private final PasswordModel model = new PasswordModel();
    private String loggedUsername;

    public void start() {
        System.out.println("=== Password Manager CLI ===");
        logger.info("Avviato Password Manager CLI");

        boolean authenticated = false;
        while (!authenticated) {
            System.out.print("[1] Login  [2] Registrazione  [0] Esci > ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> authenticated = login();
                case "2" -> register();
                case "0" -> System.exit(0);
                default -> System.out.println("Scelta non valida.");
            }
        }

        try {
            Connection conn = DatabaseManager.getConnection();
            dao = new CredentialDAO(conn);
            loadAndShowCredentials();
        } catch (Exception e) {
            System.out.println("Errore connessione DB: " + e.getMessage());
            return;
        }

        mainMenu();
    }

    private boolean login() {
        try {
            System.out.print("Nome utente: ");
            String username = scanner.nextLine();
            char[] password = readPassword("Master password: ");
            boolean success = AuthService.loginUser(username, password);
            java.util.Arrays.fill(password, '\0');
            if (success) {
                loggedUsername = username;
                System.out.println("✔️ Login riuscito.");
                logger.info("Si è loggato l'utente " + loggedUsername);
                return true;
            } else {
                logger.error("Autenticazione fallita di username " + username);
                System.out.println("❌ Credenziali non valide.");
                return false;
            }
        } catch (Exception e) {
            logger.error("Errore nel login " + e.getMessage());
            System.out.println("Errore nel login: " + e.getMessage());
            return false;
        }
    }

    private void register() {
        try {
            System.out.print("Scegli un nome utente: ");
            String username = scanner.nextLine();
            char[] password = readPassword("Imposta master password: ");
            AuthService.registerUser(username, password);
            java.util.Arrays.fill(password, '\0');
            System.out.println("✔️ Registrazione completata.");
            logger.info("Registrato correttamente l'utente " + username);
        } catch (Exception e) {
            logger.error("Errore nella registrazione: " + e.getMessage());
            System.out.println("Errore nella registrazione: " + e.getMessage());
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.print("""
                \n--- Menu ---
                1. Mostra credenziali
                2. Aggiungi credenziale
                3. Modifica credenziale
                4. Elimina credenziale
                5. Visualizza password
                0. Logout
                > """);
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> loadAndShowCredentials();
                    case "2" -> addCredential();
                    case "3" -> modifyCredential();
                    case "4" -> deleteCredential();
                    case "5" -> showDecryptedPassword();
                    case "0" -> {
                        System.out.println("Logout.");
                        SessionManager.clearKey();
                        logger.info("Ha fatto il logout l'utente " + loggedUsername);
                        loggedUsername = null;
                        return;
                    }
                    default -> System.out.println("Scelta non valida.");
                }
            } catch (Exception e) {
                logger.error("Errore: " + e.getMessage());
                System.out.println("Errore: " + e.getMessage());
            }
        }
    }

    private void loadAndShowCredentials() throws Exception {
        List<Credential> creds = dao.findAllForUser(loggedUsername);
        if (creds.isEmpty()) {
            System.out.println("⚠️ Nessuna credenziale salvata.");
        } else {
            logger.info("Richiesta stampa delle credenziali dell'utente " + loggedUsername);
            System.out.println("📂 Credenziali salvate:");
            for (Credential c : creds) {
                System.out.printf("[%d] [%s] %s%n", c.getId(), c.getType(), c.getLabel());
            }
        }
    }

    private void addCredential() throws Exception {
        System.out.print("Tipo (WEB, APP, WIFI): ");
        String typeStr = scanner.nextLine().toUpperCase();
        CredentialType type = CredentialType.valueOf(typeStr);

        System.out.print("Label (es. sito, app, SSID): ");
        String label = scanner.nextLine();

        String username = null;
        if (type == CredentialType.WEB || type == CredentialType.APP) {
            System.out.print("Username: ");
            username = scanner.nextLine();
        }

        System.out.print("Password (testuale): ");
        char[] pwdChars = scanner.nextLine().toCharArray();

        byte[] salt = CryptoManager.generateSalt();
        byte[] iv = CryptoManager.generateIV();
        byte[] encrypted;
        try {
            encrypted = CryptoManager.encrypt(
                    new String(pwdChars).getBytes(),
                    SessionManager.getKey(),
                    iv);
        } finally {
            java.util.Arrays.fill(pwdChars, '\0');
        }

        Credential newCred;
        switch (type) {
            case WEB -> newCred = new WebCredential(label, username, encrypted, salt, iv, loggedUsername);
            case APP -> newCred = new AppCredential(label, username, encrypted, salt, iv, loggedUsername);
            case WIFI -> newCred = new WifiCredential(label, encrypted, salt, iv, loggedUsername);
            default -> throw new IllegalArgumentException("Tipo non supportato.");
        }

        dao.insert(newCred);
        logger.info("Aggiunta credenziale " + newCred.getLabel() + " per l'utente " + loggedUsername);
        System.out.println("✔️ Credenziale aggiunta.");
    }

    private void modifyCredential() throws Exception {
        System.out.print("ID credenziale da modificare: ");
        int id = Integer.parseInt(scanner.nextLine());
        List<Credential> creds = dao.findAllForUser(loggedUsername);
        Credential target = creds.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (target == null) {
            System.out.println("Credenziale non trovata.");
            return;
        }

        System.out.print("Nuova password (lascia vuoto per mantenere): ");
        String newPwd = scanner.nextLine();

        byte[] encrypted = target.getEncrypted();
        byte[] salt = target.getSalt();
        byte[] iv = target.getIv();

        if (!newPwd.isBlank()) {
            byte[] newSalt = CryptoManager.generateSalt();
            byte[] newIv = CryptoManager.generateIV();
            byte[] enc;
            try {
                enc = CryptoManager.encrypt(newPwd.getBytes(), SessionManager.getKey(), newIv);
            } catch (Exception e) {
                System.out.println("Errore nella cifratura: " + e.getMessage());
                return;
            }
            encrypted = enc;
            salt = newSalt;
            iv = newIv;
        }

        Credential updated;
        switch (target) {
            case WebCredential wc ->
                    updated = new WebCredential(id, wc.getLabel(), wc.getUsername(), encrypted, salt, iv, loggedUsername);
            case AppCredential ac ->
                    updated = new AppCredential(id, ac.getLabel(), ac.getUsername(), encrypted, salt, iv, loggedUsername);
            case WifiCredential wfc ->
                    updated = new WifiCredential(id, wfc.getLabel(), encrypted, salt, iv, loggedUsername);
            default -> {
                System.out.println("Tipo credenziale non supportato.");
                return;
            }
        }

        dao.update(updated);
        logger.info("Aggiornata credenziale " + updated.getLabel() + " dell'utente " + loggedUsername);
        System.out.println("✔️ Credenziale aggiornata.");
    }

    private void deleteCredential() throws Exception {
        System.out.print("ID credenziale da eliminare: ");
        int id = Integer.parseInt(scanner.nextLine());
        dao.delete(id, loggedUsername);
        logger.info("Eliminata credenziale con id %d dell'utente ".formatted(id) + loggedUsername);
        System.out.println("✔️ Credenziale eliminata.");
    }

    private void showDecryptedPassword() throws Exception {
        System.out.print("ID credenziale da visualizzare: ");
        int id = Integer.parseInt(scanner.nextLine());
        List<Credential> creds = dao.findAllForUser(loggedUsername);
        Credential target = creds.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        if (target == null) {
            System.out.println("Credenziale non trovata.");
            return;
        }

        System.out.print("Confermi di voler mostrare la password in chiaro? (s/n): ");
        String confirm = scanner.nextLine().toLowerCase();
        if (!confirm.equals("s")) {
            System.out.println("Visualizzazione annullata.");
            return;
        }

        byte[] decrypted = CryptoManager.decrypt(target.getEncrypted(), SessionManager.getKey(), target.getIv());
        String plainPwd = new String(decrypted);

        System.out.println("Password: " + plainPwd);
        logger.info("L'utente " + loggedUsername + " ha mostrato la password di " + target.getLabel());

        java.util.Arrays.fill(decrypted, (byte) 0);
    }

    private char[] readPassword(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().toCharArray();
    }
}
