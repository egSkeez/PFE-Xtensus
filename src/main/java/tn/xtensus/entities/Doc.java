package tn.xtensus.entities;

import javax.persistence.*;
import java.io.Serializable;
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
    @ManyToOne
    private Personne expediteur;
    @ManyToMany(mappedBy = "inbox")
    private Set<Personne> destinations;

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
}
