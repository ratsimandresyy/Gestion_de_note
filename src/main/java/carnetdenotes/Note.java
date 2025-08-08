package carnetdenotes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Note {
    private final StringProperty matiere;
    private final StringProperty note;
    private final StringProperty date;

    public Note(String matiere, double note, String date) {
        this.matiere = new SimpleStringProperty(matiere);
        this.note = new SimpleStringProperty(String.valueOf(note));
        this.date = new SimpleStringProperty(date);
    }

    // --- Getters ---
    public String getMatiere() {
        return matiere.get();
    }

    public double getNote() {
        return Double.parseDouble(note.get());
    }

    public String getDate() {
        return date.get();
    }

    // --- Properties (pour JavaFX) ---
    public StringProperty matiereProperty() {
        return matiere;
    }

    public StringProperty noteProperty() {
        return note;
    }

    public StringProperty dateProperty() {
        return date;
    }

    // --- Setters (nécessaires pour la modification) ---
    public void setMatiere(String matiere) {
        this.matiere.set(matiere);
    }

    public void setNote(double note) {
        this.note.set(String.valueOf(note));
    }

    public void setDate(String date) {
        this.date.set(date);
    }
}