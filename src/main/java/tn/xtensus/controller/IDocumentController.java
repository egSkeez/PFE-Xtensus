package tn.xtensus.controller;

import java.io.IOException;

public interface IDocumentController {
    public void uploadFile() throws IOException;
    public void creationDocument(String nomFichier, String owner);
    public void applyACL(String docId, String principlID, String acl);
    public void applyACLFolder(String docId, String principlID, String acl);
    public void envoyerVers(String docId, String intermediaire);
    public void createFolderStructure();
}
