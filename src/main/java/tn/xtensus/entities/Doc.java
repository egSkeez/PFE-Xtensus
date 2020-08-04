package tn.xtensus.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "documents")
public class Doc implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String alfrescoId;
    private String nom;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "doc",fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Doc_Person> inbox = new HashSet<Doc_Person>();


    @ManyToOne
    private Personne expediteur;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "person_doc",
            joinColumns = { @JoinColumn(name = "doc_id",referencedColumnName ="id") },
            inverseJoinColumns = { @JoinColumn(name = "person_id",referencedColumnName ="id") }
    )
    private Set<Personne> destinations = new HashSet<>();

    public Doc() {
    }

    public Doc(int id, String alfrescoId, String nom) {
        this.id = id;
        this.alfrescoId = alfrescoId;
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlfrescoId() {
        return alfrescoId;
    }

    public void setAlfrescoId(String alfrescoId) {
        this.alfrescoId = alfrescoId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Personne getExpediteur() {
        return expediteur;
    }

    public void setExpediteur(Personne expediteur) {
        this.expediteur = expediteur;
    }

    public Set<Personne> getDestinations() {
        return destinations;
    }

    public void setDestinations(Set<Personne> destinations) {
        this.destinations = destinations;
    }

    public Set<Doc_Person> getInbox() {
        return inbox;
    }

    public void setInbox(Set<Doc_Person> inbox) {
        this.inbox = inbox;
    }
}
