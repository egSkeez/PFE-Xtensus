package tn.xtensus.service;

import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Personne;

import java.util.List;

public interface IPersonneService {
    public Personne getPersonneByNomAndPassword(String nom, String password);
    public List<Personne> loadData();
    public List<Personne> getPersonnes();
}
