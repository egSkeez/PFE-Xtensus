package tn.xtensus.controller;

import tn.xtensus.entities.Doc;

import java.io.IOException;
import java.util.List;

public interface IDocumentController {
    public void uploadFile() throws IOException;
    public void creationDocument(String nomFichier, String owner);
    public void applyACL(String docId, String principlID, String acl);
    public void applyACLFolder(String docId, String principlID, String acl);
    public void envoyerVers(String docId, String intermediaire);
    public void createFolderStructure();
    public String save();
    public Doc getDocument();
    public String delete(int id);
    public void loadData();
    public List<Doc> getDocs();

}
