package tn.xtensus.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

@Entity
public class Site implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String visibility;
    private String siteId;
    @ManyToOne
    private Personne manager;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "site",fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Member> members = new HashSet<Member>();
    @Column
    @ElementCollection(targetClass=Doc.class)
    private Set<Doc> content;
    @Column
    @ElementCollection(targetClass=String.class)
    private List<String> activities;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "site",fetch = FetchType.EAGER,  orphanRemoval = true)
    private Set<Doc> documents;

    public Site() {
    }

    public Site(String nom, Set<Member> members, Set<Doc> content, List<String> activities) {
        this.nom = nom;
        this.members = members;
        this.content = content;
        this.activities = activities;
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

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public Set<Doc> getContent() {
        return content;
    }

    public void setContent(Set<Doc> content) {
        this.content = content;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public Personne getManager() {
        return manager;
    }

    public void setManager(Personne manager) {
        this.manager = manager;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Set<Doc> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Doc> documents) {
        this.documents = documents;
    }
}
