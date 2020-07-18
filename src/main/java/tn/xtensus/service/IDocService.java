package tn.xtensus.service;

import tn.xtensus.entities.Doc;

import java.util.List;

public interface IDocService {
    public String save();
    public Doc getDoc();
    public String delete(int id);
    public void loadData();
    public List<Doc> getDocs();


}
