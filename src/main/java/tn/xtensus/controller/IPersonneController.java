package tn.xtensus.controller;

import tn.xtensus.entities.Personne;

import java.util.List;
import java.util.Locale;

public interface IPersonneController {
    public String doLogin();
    public void uploadFile();
    public List<Personne> loadData();
    public List<Personne> getPersonnes();

}
