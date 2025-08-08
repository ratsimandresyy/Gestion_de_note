package carnetdenotes;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {

    private final TableView<Note> table = new TableView<>();
    private final TextField matiereField = new TextField();
    private final TextField noteField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final Label moyenneLabel = new Label("Moyenne : N/A");
    private final ArrayList<Note> notes = new ArrayList<>();
    private BarChart<String, Number> barChart;

    // Nouveau : pour suivre si on est en mode modification
    private Note noteEnModification = null;

    // Boutons
    private final Button ajouterBtn = new Button("Ajouter");
    private final Button annulerBtn = new Button("Annuler");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Carnet de Notes");

        // --- Boutons ---
        ajouterBtn.setOnAction(e -> ajouterNote());
        annulerBtn.setOnAction(e -> annulerModification());
        annulerBtn.setDisable(true); // Désactivé au départ

        // --- Formulaire ---
        HBox form = new HBox(10);
        matiereField.setPromptText("Matière");
        noteField.setPromptText("Note /20");
        form.getChildren().addAll(matiereField, noteField, datePicker, ajouterBtn, annulerBtn);

        // --- Tableau ---
        TableColumn<Note, String> matiereCol = new TableColumn<>("Matière");
        matiereCol.setCellValueFactory(cell -> cell.getValue().matiereProperty());

        TableColumn<Note, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(cell -> cell.getValue().noteProperty());

        TableColumn<Note, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());

        // Colonne Actions : Modifier + Supprimer
        TableColumn<Note, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            private final Button editBtn = new Button("Modifier");

            {
                editBtn.setOnAction(event -> {
                    Note note = getTableView().getItems().get(getIndex());
                    noteEnModification = note;

                    // Remplir le formulaire
                    matiereField.setText(note.getMatiere());
                    noteField.setText(String.valueOf(note.getNote()));
                    datePicker.setValue(java.time.LocalDate.parse(note.getDate()));

                    // Changer l'interface
                    ajouterBtn.setText("Mettre à jour");
                    annulerBtn.setDisable(false);
                });

                deleteBtn.setOnAction(event -> {
                    Note note = getTableView().getItems().get(getIndex());
                    notes.remove(note);
                    updateTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });

        table.getColumns().addAll(matiereCol, noteCol, dateCol, actionCol);

        // --- Graphique ---
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 20, 1);
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Graphique des notes");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(300);

        // --- Layout principal ---
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");
        layout.getChildren().addAll(form, table, moyenneLabel, barChart);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Gère à la fois l'ajout et la modification d'une note
     */
    private void ajouterNote() {
        try {
            String matiere = matiereField.getText().trim();
            if (matiere.isEmpty()) {
                throw new IllegalArgumentException("La matière est requise.");
            }

            double note;
            try {
                note = Double.parseDouble(noteField.getText());
                if (note < 0 || note > 20) {
                    throw new IllegalArgumentException("La note doit être entre 0 et 20.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("La note doit être un nombre.");
            }

            String date = datePicker.getValue() != null
                    ? datePicker.getValue().toString()
                    : java.time.LocalDate.now().toString();

            if (noteEnModification != null) {
                // Mode modification
                noteEnModification.setMatiere(matiere);
                noteEnModification.setNote(note);
                noteEnModification.setDate(date);

                // Réinitialiser
                noteEnModification = null;
                ajouterBtn.setText("Ajouter");
                annulerBtn.setDisable(true);

                matiereField.clear();
                noteField.clear();
                datePicker.setValue(null);

                updateTable();
                System.out.println("✅ Note mise à jour : " + matiere);
            } else {
                // Mode ajout
                notes.add(new Note(matiere, note, date));
                matiereField.clear();
                noteField.clear();
                datePicker.setValue(null);
                updateTable();
                System.out.println("✅ Note ajoutée : " + matiere);
            }
        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur inattendue. Vérifiez les champs.");
            alert.show();
        }
    }

    /**
     * Annule le mode modification
     */
    private void annulerModification() {
        noteEnModification = null;
        ajouterBtn.setText("Ajouter");
        annulerBtn.setDisable(true);
        matiereField.clear();
        noteField.clear();
        datePicker.setValue(null);
    }

    /**
     * Met à jour le tableau, la moyenne et le graphique
     */
    private void updateTable() {
        table.getItems().setAll(notes);
        updateMoyenne();
        updateGraph();
    }

    private void updateMoyenne() {
        if (notes.isEmpty()) {
            moyenneLabel.setText("Moyenne : N/A");
            return;
        }
        double total = notes.stream().mapToDouble(Note::getNote).sum();
        double moyenne = total / notes.size();
        moyenneLabel.setText("Moyenne : " + String.format("%.2f", moyenne) + " / 20");
    }

    private void updateGraph() {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Note n : notes) {
            series.getData().add(new XYChart.Data<>(n.getMatiere() + " (" + n.getDate() + ")", n.getNote()));
        }
        barChart.getData().add(series);
    }
}