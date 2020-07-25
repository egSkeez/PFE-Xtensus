package tn.xtensus.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
public class Personne implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String prenom;
    private String password;
    @OneToMany(mappedBy = "expediteur", fetch = FetchType.EAGER)
    private List<Doc> docs;
    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinTable(
            name = "person_doc",
            joinColumns = { @JoinColumn(name = "person_id") },
            inverseJoinColumns = { @JoinColumn(name = "doc_id") }
    )
    private Set<Doc> inbox;

    public Personne() {
    }

    public Personne(int id, String nom, String prenom, String password, List<Doc> docs) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.docs = docs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    public Set<Doc> getInbox() {
        return inbox;
    }

    public void setInbox(Set<Doc> inbox) {
        this.inbox = inbox;
    }
}
