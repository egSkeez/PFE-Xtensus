package tn.xtensus.service;

import tn.xtensus.entities.Personne;

public interface IPersonneService {
    public Personne getPersonneByNomAndPassword(String nom, String password);
}
