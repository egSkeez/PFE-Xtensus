package tn.xtensus.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user",fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Doc_Person> userGroups = new HashSet<Doc_Person>();


    @OneToMany(mappedBy = "expediteur", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Doc> docs;
    @ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER,mappedBy = "destinations")
    private Set<Doc> inbox = new HashSet<>();

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

    public Set<Doc_Person> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Set<Doc_Person> userGroups) {
        this.userGroups = userGroups;
    }
}
