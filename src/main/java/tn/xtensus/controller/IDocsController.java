package tn.xtensus.controller;

import tn.xtensus.entities.Doc;

import java.util.List;

public interface IDocsController {
    public void loadData();
    public List<Doc> getDocs();
}
