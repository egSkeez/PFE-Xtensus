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
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Personne;
import tn.xtensus.service.IDocService;
import tn.xtensus.service.IPersonneService;
import org.ocpsoft.rewrite.el.ELBeanName;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Integer.getInteger;
import static java.lang.Integer.valueOf;

@Scope(value = "session")
@Component(value = "personneController")
@ELBeanName(value = "personneController")
@Join(path = "/", to = "/login.jsf")
@ViewScoped
@Named("personneController")
public class PersonneController implements IPersonneController, Serializable {

    private String nom;
    private String password;
    private Personne personne;
    private Boolean loggedIn;
    private UploadedFile file;
    private  LocalConfig prem = new LocalConfig();
    private Session session;
    private List<Doc> docs;
    private List<Personne> people;
    private List<Personne> filteredPeople;
    @Autowired
    IPersonneService iPersonneService;
    @Deferred
    @RequestAction
    @IgnorePostback
     public List<Personne> loadData() {
        System.out.println("Loading people");
       people = iPersonneService.loadData();

       return people;

    }
//Filter people function
public boolean globalFilterFunction(Object value, Object filter, Locale locale) {
    System.out.println("globaFilterFunction started !!!");
    String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
    if (filterText == null || filterText.equals("")) {
        return true;
    }

    Personne person = (Personne) value;
    return   person.getNom().toLowerCase().contains(filterText)
            || person.getPrenom().toLowerCase().contains(filterText);
}

    public List<Personne> getPersonnes() {
        System.out.println("Getting people");
        return people = iPersonneService.getPersonnes();
    }

    public String doLogin(){
        System.out.println("doLogin Function triggered !");
       session = prem.getCmisSession(nom,password);
        String navigateTo = "null";
        Personne personne=iPersonneService.getPersonneByNomAndPassword(nom,password);
        docs = personne.getDocs();
        for(Doc dc: docs){
            System.out.println("Document de "+personne.getNom()+" Est: "+dc.getNom());
        }
        if(personne != null ){
            navigateTo = "/docs-list.xhtml?faces-redirect=true";
            loggedIn=true;
        } else {
            FacesMessage facesMessage =
                    new FacesMessage("Login  Failed: please check your  username/password and try again.");
        FacesContext.getCurrentInstance().addMessage("form:btn",facesMessage);

    } return navigateTo;}


    public void uploadFile() {
        System.out.println("Upload File triggered !!!");


        if (!session.getRepositoryInfo().getCapabilities().getAclCapability()
                .equals(CapabilityAcl.MANAGE)) {
            System.out.println("Le GED ne supporte pas les ACL");
        } else {
            System.out.println("Le GED supporte les ACL");


    	/*  HashMap<String, String> newFolderProps = new HashMap<String, String>();
    	    newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
    	    newFolderProps.put(PropertyIds.NAME, "Courrier");
    	    Folder folderAssociations = ((Folder) session.getObjectByPath("/Espaces Utilisateurs/motaz")).createFolder(newFolderProps); */
            System.out.println("The file's name is: "+file.getFileName());
            BigInteger bi;
            bi = BigInteger.valueOf(file.getSize());
            HashMap<String, Object> newFileProps = new HashMap<String, Object>();
            ContentStream contentStream = new ContentStreamImpl(file.getFileName(), bi,
                    file.getContentType(), new ByteArrayInputStream(file.getContents()));


            //newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            newFileProps.put(PropertyIds.NAME, file.getFileName());
            newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            List<String> permissions = new ArrayList<String>();
            permissions.add("cmis:all");
            String principal = nom;
            Ace aceIn = session.getObjectFactory().createAce(principal, permissions);
            List<Ace> aceListIn = new ArrayList<Ace>();
            aceListIn.add(aceIn);
            Document testDoc = ((Folder) session.getObjectByPath("/Partagé/PostArion")).createDocument(newFileProps,
                    contentStream,
                    VersioningState.MAJOR);
            testDoc.addAcl(aceListIn, AclPropagation.REPOSITORYDETERMINED);

            OperationContext operationContext = new OperationContextImpl();
            operationContext.setIncludeAcls(true);
            testDoc = (Document) session.getObject(testDoc, operationContext);
            System.out.println("l'id du document est: "+testDoc.getId());

            System.out.println("ACL avant la création d'un ace...");
            Acl acl = testDoc.getAcl();
            List<Ace> aces = acl.getAces();
            aces.removeAll(aces);
            for (Ace ace : aces) {
                System.out.println("Found ace: " + ace.getPrincipalId() + " toString "+ ace.toString());
            }
            testDoc.setAcl(aces);
            testDoc.refresh();

        }

    }


    public void sendTo(String docId, String intermediaire){
        Document document = (Document)session.getObject(docId);


        String aspectName = "P:cm:titled";

        // Make sure we got a document, and then add the aspect to it
        if (document != null) {
            // Check that document don't already got the aspect applied
            List<Object> aspects = document.getProperty("cmis:secondaryObjectTypeIds").getValues();
            if (!aspects.contains(aspectName)) {
                aspects.add(aspectName);

                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put("cmis:secondaryObjectTypeIds", aspects);
                properties.put("cm:description", intermediaire);


                Document updatedDocument = (Document) document.updateProperties(properties);

                System.out.println("Added aspect " + aspectName + " to " );
            } else {
                System.out.println("Aspect " + aspectName + " is already applied to ");
            }
        } else {
            System.out.println("Document is null, cannot add aspect to it!");
        }
    }

    public String doLogout()
    {FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";


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

    public LocalConfig getPrem() {
        return prem;
    }

    public void setPrem(LocalConfig prem) {
        this.prem = prem;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


    public List<Personne> getPeople() {
        return people;
    }

    public void setPeople(List<Personne> people) {
        this.people = people;
    }

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    public List<Personne> getFilteredPeople() {
        return filteredPeople;
    }

    public void setFilteredPeople(List<Personne> filteredPeople) {
        this.filteredPeople = filteredPeople;
    }
}
