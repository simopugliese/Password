package com.simonepugliese.password.gui;

import com.simonepugliese.password.db.CredentialDAO;
import com.simonepugliese.password.db.DatabaseManager;
import com.simonepugliese.password.model.credential.AppCredential;
import com.simonepugliese.password.model.credential.Credential;
import com.simonepugliese.password.model.credential.CredentialType;
import com.simonepugliese.password.model.SessionManager;
import com.simonepugliese.password.model.CryptoManager;
import com.simonepugliese.password.model.credential.WebCredential;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableScheduledFuture;

public class GuiController {

    private static final Logger logger = LoggerFactory.getLogger(GuiController.class);

    @FXML private TableView<Credential> tableView;
    @FXML private TableColumn<Credential, CredentialType> colType;
    @FXML private TableColumn<Credential, String> colLabel;
    @FXML private TableColumn<Credential, String> colUsername;
    @FXML private Label messageLabel;

    private CredentialDAO dao;
    private ObservableList<Credential> data;
    private String loggedUsername;

    public void setLoggedUsername(String username) {
        this.loggedUsername = username;
    }

    @FXML
    private void initialize() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colLabel.setCellValueFactory(new PropertyValueFactory<>("label"));
        colUsername.setCellValueFactory(cellData -> {
            Credential c = cellData.getValue();
            if (c instanceof WebCredential wc) {
                return new SimpleStringProperty(wc.getUsername());
            } else if (c instanceof AppCredential ac) {
                return new SimpleStringProperty(ac.getUsername());
            } else {
                return new SimpleStringProperty("-");
            }
        });

        tableView.setRowFactory(tv -> {
            TableRow<Credential> row = new TableRow<>();

            ContextMenu contextMenu = new ContextMenu();

            ImageView modifyIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/simonepugliese/password/gui/icons/edit.png")));
            //modifyIcon.setFitHeight(20);
            //modifyIcon.setFitWidth(20);
            MenuItem editItem = new MenuItem("Modifica", modifyIcon);
            editItem.setOnAction(e -> handleEdit());

            ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/simonepugliese/password/gui/icons/delete.png")));
            //modifyIcon.setFitHeight(20);
            //modifyIcon.setFitWidth(20);
            MenuItem deleteItem = new MenuItem("Elimina", deleteIcon);
            deleteItem.setOnAction(e -> handleDelete());

            ImageView showIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/simonepugliese/password/gui/icons/show.png")));
            //modifyIcon.setFitHeight(20);
            //modifyIcon.setFitWidth(20);
            MenuItem showItem = new MenuItem("Mostra password",showIcon);
            showItem.setOnAction(e -> handleShowPassword());

            contextMenu.getItems().addAll(editItem, deleteItem, showItem);

            // Solo se la riga non è vuota
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings
                            .when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );


            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit();
                }
            });

            return row;
        });
    }

    public void loadData() {
        try {
            if (dao == null) {
                dao = new CredentialDAO(DatabaseManager.getConnection());
            }
            List<Credential> list = dao.findAllForUser(loggedUsername);
            data = FXCollections.observableArrayList(list);
            tableView.setItems(data);
            messageLabel.setText("");
        } catch (Exception e) {
            messageLabel.setText("Errore caricamento dati: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        CredentialDialog dialog = new CredentialDialog(null);
        dialog.showAndWait().ifPresent(credential -> {
            try {
                dao.insert(credential);
                loadData();
                messageLabel.setText("Credenziale aggiunta.");
                logger.info("Inserita credenziale da GUI, l'id è " + credential.getId());
            } catch (Exception e) {
                messageLabel.setText("Errore aggiunta: " + e.getMessage());
                logger.error("Errore nell'inserimento di credenziale da GUI " + e.getMessage());
            }
        });
    }


    @FXML
    private void handleEdit() {
        Credential selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una credenziale da modificare.");
            return;
        }

        CredentialDialog dialog = new CredentialDialog(selected);
        dialog.showAndWait().ifPresent(credential -> {
            try {
                dao.update(credential);
                loadData();
                messageLabel.setText("Credenziale modificata.");
                logger.info("Modificata credenziale da GUI, l'id è " + credential.getId());
            } catch (Exception e) {
                logger.error("Errore nella modifica di una credenziale da GUI " + e.getMessage());
                messageLabel.setText("Errore modifica: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Credential selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una credenziale da eliminare.");
            return;
        }
        try {
            dao.delete(selected.getId(), loggedUsername);
            loadData();
            logger.info("Eliminata da GUI credenziale dell'utente " + loggedUsername);
            messageLabel.setText("Credenziale eliminata.");
        } catch (Exception e) {
            logger.error("Errore nell'eliminazione di credenziale da GUI dell'utente " + loggedUsername + " " + e.getMessage());
            messageLabel.setText("Errore eliminazione: " + e.getMessage());
        }
    }

    @FXML
    private void handleShowPassword() {
        Credential selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Seleziona una credenziale.");
            return;
        }
        try {
            byte[] decrypted = CryptoManager.decrypt(selected.getEncrypted(), SessionManager.getKey(), selected.getIv());
            String plainPwd = new String(decrypted);
            messageLabel.setText("Password: " + plainPwd);
            java.util.Arrays.fill(decrypted, (byte) 0);
            logger.info("Mostrata password da GUI con id " + selected.getId());
        } catch (Exception e) {
            logger.error("Errore nel mostrare password su GUI " + e.getMessage());
            messageLabel.setText("Errore decifrazione: " + e.getMessage());
        }
    }
}
