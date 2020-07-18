package tn.xtensus.controller;


import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.ocpsoft.rewrite.annotation.Join;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Personne;
import tn.xtensus.service.IPersonneService;
import org.ocpsoft.rewrite.el.ELBeanName;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Scope(value = "session")
@Component(value = "personneController")
@ELBeanName(value = "personneController")
@Join(path = "/", to = "/login.jsf")

public class PersonneController implements IPersonneController{
    @Autowired
    IPersonneService iPersonneService;
    private String nom;
    private String password;
    private Personne personne;
    private Boolean loggedIn;
    private UploadedFile file;
    public String doLogin(){
        System.out.println("doLogin Function triggered !");
        String navigateTo = "null";
        Personne personne=iPersonneService.getPersonneByNomAndPassword(nom,password);
        if(personne != null ){
            navigateTo = "/welcome.xhtml?faces-redirect=true";
            loggedIn=true;
        } else {
            FacesMessage facesMessage =
                    new FacesMessage("Login  Failed: please check your  username/password and try again.");
        FacesContext.getCurrentInstance().addMessage("form:btn",facesMessage);

    } return navigateTo;}


    public void uploadFile() {
        System.out.println("Upload File triggered !!!");
            LocalConfig prem = new LocalConfig();
            Session session = prem.getCmisSession();
            if (!session.getRepositoryInfo().getCapabilities().getAclCapability()
                    .equals(CapabilityAcl.MANAGE)) {
                System.out.println("Le GED ne supporte pas les ACL");
            } else {
                System.out.println("Le GED supporte les ACL");


    	/*  HashMap<String, String> newFolderProps = new HashMap<String, String>();
    	    newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
    	    newFolderProps.put(PropertyIds.NAME, "Courrier");
    	    Folder folderAssociations = ((Folder) session.getObjectByPath("/Espaces Utilisateurs/motaz")).createFolder(newFolderProps); */

                HashMap<String, Object> newFileProps = new HashMap<String, Object>();
                ContentStream contentStream = null;
                try {
                    contentStream = new ContentStreamImpl(file.getFileName(), null,
                            file.getContentType(), file.getInputstream());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                newFileProps.put(PropertyIds.NAME, file.getFileName());
                newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                List<String> permissions = new ArrayList<String>();
                permissions.add("cmis:all");
                String principal = "admin";
                Ace aceIn = ((Session) session).getObjectFactory().createAce(principal, permissions);
                List<Ace> aceListIn = new ArrayList<Ace>();
                aceListIn.add(aceIn);
                Document testDoc = ((Folder) session.getObjectByPath("/Partagé/PostArion")).createDocument(newFileProps, contentStream,
                        VersioningState.MAJOR);
                testDoc.addAcl(aceListIn, AclPropagation.REPOSITORYDETERMINED);

                OperationContext operationContext = new OperationContextImpl();
                operationContext.setIncludeAcls(true);
                testDoc = (Document) session.getObject(testDoc, operationContext);
                System.out.println("l'id du document est: " + testDoc.getId());

                System.out.println("ACL avant la création d'un ace...");
                Acl acl = testDoc.getAcl();
                List<Ace> aces = acl.getAces();
                aces.removeAll(aces);
                for (Ace ace : aces) {
                    System.out.println("Found ace: " + ace.getPrincipalId() + " toString " + ace.toString());
                }
                testDoc.setAcl(aces);
                testDoc.refresh();
                Doc doc = new Doc();
                doc.setAlfrescoId(testDoc.getId());
               // doc.setExpediteur(nom);
                doc.setNom(testDoc.getName());



            }

    }

    public String doLogout()
    {FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";


    }

    public IPersonneService getiPersonneService() {
        return iPersonneService;
    }

    public void setiPersonneService(IPersonneService iPersonneService) {
        this.iPersonneService = iPersonneService;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Personne getPersonne() {
        return personne;
    }

    public void setPersonne(Personne personne) {
        this.personne = personne;
    }

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }
}
