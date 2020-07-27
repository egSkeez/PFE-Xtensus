package tn.xtensus.controller;


import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
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
import org.springframework.transaction.annotation.Transactional;
import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Personne;
import tn.xtensus.repository.DocRepository;
import tn.xtensus.repository.PersonneRepository;
import tn.xtensus.service.IDocService;
import tn.xtensus.service.IPersonneService;
import org.ocpsoft.rewrite.el.ELBeanName;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
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
public class PersonneController implements IPersonneController, Serializable, IDocumentController {

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
    private String[] selectedRights;
    private List<SelectItem> rights;
    private List<Personne> recievers;
    private List<Doc> selectedDocs;
    private Boolean loggedin = false;
    private Set<Doc> inbox;
    private List<Doc> selectedInbox;
    @Autowired
    PersonneRepository personneRepository;
    @Autowired
    DocRepository docRepository;
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


    public List<Personne> getPersonnes() {
        System.out.println("Getting people");
        return people = iPersonneService.getPersonnes();
    }

    public String doLogin(){
        System.out.println("######################### doLogin Function triggered #########################");
        System.out.println("User signed up: "+nom);
       session = prem.getCmisSession(nom,password);
        session.getDefaultContext().setCacheEnabled(false);
        loggedIn = true;
        System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));

        String navigateTo = "null";
        personne=iPersonneService.getPersonneByNomAndPassword(nom,password);
        System.out.println("brought person: "+personne.getNom());
        if(personne.getDocs().size()!=0){
            docs = personne.getDocs();
        }
        inbox = personne.getInbox();
        System.out.println("Inbox size: "+inbox.size());
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
        System.out.println("######################### Upload File triggered #########################");
        Doc dbDoc = new Doc();


        if (!session.getRepositoryInfo().getCapabilities().getAclCapability()
                .equals(CapabilityAcl.MANAGE)) {
            System.out.println("Le GED ne supporte pas les ACL");
        } else {
            System.out.println("Le GED supporte les ACL");

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
            testDoc.refresh();
            dbDoc.setAlfrescoId(testDoc.getId());
            dbDoc.setNom(file.getFileName());
            dbDoc.setExpediteur(personne);
            docRepository.save(dbDoc);
            System.out.println("Document added to Database !");



        }

    }

    @Override
    public void creationDocument(String nomFichier, String owner) {

    }

    @Override
    public void applyACL(String docId, String principlID, String acl) {

    }

    @Override
    public void applyACLFolder(String docId, String principlID, String acl) {

    }

    @Override
    public void envoyerVers(String docId, String intermediaire) {

    }

    @Override
    public void createFolderStructure() {

    }

    @Override
    public String save() {
        return null;
    }

    @Override
    public Doc getDocument() {
        return null;
    }

    @Transactional
    public String delete() {
        System.out.println("######################### Delete function started #########################");
        for(Doc dc: selectedDocs){
            System.out.println("Deleting file: "+dc.getNom());
        docRepository.delete(dc);
        Document doc =(Document) session.getObject(dc.getAlfrescoId());
        doc.delete();
        docs.remove(dc);
        System.out.println("Deleted file: "+dc.getNom());
        }
        return "/docs-list.xhtml?faces-redirect=true";
    }

    @Transactional
    public void sendTo(){
        System.out.println("######################### Send function started #########################");
        System.out.println("Recievers size: "+recievers.size());
        System.out.println("Documents selected: "+selectedDocs.size());
        System.out.println("Number of rights selected: "+Arrays.asList(selectedRights).size());

        for(int i=0;i<recievers.size();i++) {
            System.out.println("Treating user: "+recievers.get(i).getNom());

            for(Doc d: selectedDocs) {


                List<Ace> aceListIn = new ArrayList<Ace>();
                Document document = (Document) session.getObject(d.getAlfrescoId());
                System.out.println("Treating document: "+document.getName());
                String aspectName = "P:cm:titled";

                    // Check that document don't already got the aspect applied
                    if(Arrays.asList(selectedRights).contains("middle man"))
                    {
                        System.out.println("Treating middle man!");
                    List<Object> aspects = document.getProperty("cmis:secondaryObjectTypeIds").getValues();

                        aspects.add(aspectName);

                        Map<String, Object> properties = new HashMap<String, Object>();
                        properties.put("cmis:secondaryObjectTypeIds", aspects);
                        properties.put("cm:description", recievers.get(i).getNom());
                        List<String> permission = new ArrayList<String>();
                        permission.add("cmis:all");
                        String principal = recievers.get(i).getNom();
                        Ace aceIn = session.getObjectFactory().createAce(principal, permission);

                        aceListIn.add(aceIn);

                        document.updateProperties(properties);

                        System.out.println("Gave "+recievers.get(i).getNom()+" all the rights!");
                    } else {
                        for(String rght: selectedRights) {

                            List<String> permissions = new ArrayList<String>();
                            permissions.add("cmis:" + rght);
                            String principal = recievers.get(i).getNom();
                            Ace aceIn = session.getObjectFactory().createAce(principal, permissions);
                            aceListIn.add(aceIn);
                            System.out.println("Treated right: "+rght);
                        }
                    }
                    document.addAcl(aceListIn, AclPropagation.REPOSITORYDETERMINED);
                System.out.println("user Id is: "+recievers.get(i).getId());
                System.out.println("Document id: "+d.getId());
               d.getDestinations().add(recievers.get(i));
               // personneRepository.save(recievers.get(i));
               // docRepository.save(d);
               // recievers.get(i).getInbox().add(d);


            }
            System.out.println("Done with the the iteration number: "+i);
        }

    }
    public void downloadDocument()
    {
        System.out.println("######################### Download Function #########################");
        for(Doc dc: selectedDocs) {
            Document newDocument = (Document) session.getObject(dc.getAlfrescoId());
            System.out.println(newDocument.getId());
            try {
                ContentStream cs = newDocument.getContentStream(null);
                BufferedInputStream in = new BufferedInputStream(cs.getStream());
                String home = System.getProperty("user.home");
              //  File file = new File(home+"/Downloads/" + fileName + ".txt");
                FileOutputStream fos = new FileOutputStream(home+"/Downloads/" + dc.getNom() + ".txt");
                OutputStream bufferedOutputStream = new BufferedOutputStream(fos);
                byte[] buf = new byte[1024];
                int n = 0;
                while ((n = in.read(buf)) > 0) {
                    bufferedOutputStream.write(buf, 0, n);
                }
                bufferedOutputStream.close();
                fos.close();
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getLocalizedMessage());
            }
        }
    }

    public String doLogout()
    {  System.out.println("######################### Logout Function #########################");
        //this.session.clear();
        loggedIn = false;
        System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();


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

    public String[] getSelectedRights() {
        return selectedRights;
    }

    public void setSelectedRights(String[] selectedRights) {
        this.selectedRights = selectedRights;
    }

    public List<SelectItem> getRights() {
        return rights;
    }

    public void setRights(List<SelectItem> rights) {
        this.rights = rights;
    }

    public List<Personne> getRecievers() {
        return recievers;
    }

    public void setRecievers(List<Personne> recievers) {
        this.recievers = recievers;
    }

    public List<Doc> getSelectedDocs() {
        return selectedDocs;
    }

    public void setSelectedDocs(List<Doc> selectedDocs) {
        this.selectedDocs = selectedDocs;
    }

    public Boolean getLoggedin() {
        return loggedin;
    }

    public void setLoggedin(Boolean loggedin) {
        this.loggedin = loggedin;
    }

    public Set<Doc> getInbox() {
        return inbox;
    }

    public void setInbox(Set<Doc> inbox) {
        this.inbox = inbox;
    }

    public List<Doc> getSelectedInbox() {
        return selectedInbox;
    }

    public void setSelectedInbox(List<Doc> selectedInbox) {
        this.selectedInbox = selectedInbox;
    }
}
