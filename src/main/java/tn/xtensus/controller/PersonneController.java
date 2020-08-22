package tn.xtensus.controller;


import com.sun.syndication.feed.atom.Person;
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
import org.aspectj.weaver.patterns.PerObject;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.xtensus.entities.*;
import tn.xtensus.repository.*;
import tn.xtensus.service.IDocService;
import tn.xtensus.service.IPersonneService;
import org.ocpsoft.rewrite.el.ELBeanName;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static java.lang.Integer.getInteger;
import static java.lang.Integer.valueOf;

@Scope(value = "session")
@Component(value = "personneController")
@ELBeanName(value = "personneController")
@Join(path = "/", to = "/WEB-INF/jsp/login.jsf")
@SessionScoped
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
    private List<String> selectedRights;
    private Doc docDetails;
    private List<SelectItem> rights;
    private List<Personne> recievers;
    private List<Doc> selectedDocs;
    private Doc selectedDoc;
    private Boolean loggedin = false;
    private Set<Doc_Person> inbox = new HashSet<Doc_Person>();
    private Set<Doc> docInbox = new HashSet<Doc>();
    private List<Doc> selectedInbox;
    private Document document;
    private String checkRights;
    private String siteName;
    private String siteDescription;
    private String siteId;
    private String siteVisibility;
    private List<Site> allSites;
    private List<Site> filteredSites;
    private Site selectedSite;
    private Set<Member> siteMembers;
    private Personne personToAdd;
    private Set<Personne> peopleToAdd = new HashSet<>();
    private String chosenRole;
    private Set<Personne> membersBySite = new HashSet<>();
    private Personne personToDelete;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    InboxRepository inboxRepository;
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
    @Transactional
    public String doLogin(){
        System.out.println("######################### doLogin Function triggered #########################");
        System.out.println("User signed up: "+nom);
        String navigateTo = "null";
        personne=iPersonneService.getPersonneByNomAndPassword(nom,password);
        if(personne != null ){
            session = prem.getCmisSession(nom,password);
            session.getDefaultContext().setCacheEnabled(false);
            System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));
            System.out.println("brought person: "+personne.getNom());
            if(personne.getDocs().size()!=0){
                docs = personne.getDocs();
                for(Doc dc: docs){
                    System.out.println("Document de "+personne.getNom()+" Est: "+dc.getNom());
                }
            }
            docInbox.removeAll(docInbox);
            inbox = inboxRepository.getInboxByUser(personne.getId());

            Iterator<Doc_Person> it = inbox.iterator();
            while(it.hasNext()){

                docInbox.add(it.next().getDoc());
            }
            navigateTo = "/docs-list.xhtml?faces-redirect=true";
            loggedIn=true;
        } else {

            FacesMessage facesMessage =
                    new FacesMessage("Login  Failed: please check your  username/password and try again.");
        FacesContext.getCurrentInstance().addMessage("form:btn",facesMessage);

    }

        return navigateTo;}


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
                    "application/octet-stream", new ByteArrayInputStream(file.getContents()));


            //newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            newFileProps.put(PropertyIds.NAME, personne.getNom());
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
           dbDoc.setAuthor(testDoc.getCreatedBy());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            dbDoc.setCreationDate(dtf.format(now));
            dbDoc.setIsImmutable(testDoc.getPropertyValue("cmis:isImmutable").toString());
            dbDoc.setMimeType(testDoc.getPropertyValue("cmis:contentStreamMimeType").toString());
           dbDoc.setModificationDate(dtf.format(now));
            dbDoc.setVersion(testDoc.getPropertyValue("cmis:versionLabel").toString());
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
            System.out.println("Deleting file: "+selectedDoc.getNom());
        docRepository.delete(selectedDoc);
        personne.getDocs().remove(selectedDoc);

        Document doc =(Document) session.getObject(selectedDoc.getAlfrescoId());
        doc.delete();
        docs.remove(selectedDoc);
        System.out.println("Deleted file: "+selectedDoc.getNom());

        System.out.println("Updating "+personne.getNom());
        personneRepository.save(personne);
        return "/docs-list.xhtml?faces-redirect=true";
    }

    @Transactional
    public void sendTo(){
        System.out.println("######################### Send function started #########################");
        System.out.println("reciever name: "+recievers.get(0).getNom());
        System.out.println("Doc name: "+selectedDoc.getNom());
        System.out.println("Selected rights: "+selectedRights.get(0));
        List<Ace> aceListIn = new ArrayList<Ace>();
        System.out.println("Created Ace");
        System.out.println("Alfresco Id: "+ selectedDoc.getAlfrescoId());
        String alfrescoId = selectedDoc.getAlfrescoId();
        System.out.println("CMIS version:"+session.getRepositoryInfo().getCmisVersion());
        document = (Document) session.getObject(alfrescoId);
        System.out.println("Treating document: "+document.getName());
        Doc_Person inbox = new Doc_Person();
        inbox.setDoc(selectedDoc);
        inbox.setUser(recievers.get(0));
        inboxRepository.save(inbox);
        System.out.println("Added document to inbox");
        String aspectName = "P:cm:titled";
                        for(String rght: selectedRights) {
                            if(rght.equals("middle man"))
                            {

                                System.out.println("Treating middle man!");
                                List<Object> aspects = document.getProperty("cmis:secondaryObjectTypeIds").getValues();

                                aspects.add(aspectName);

                                List<String> permissions = new ArrayList<String>();
                                permissions.add("cmis:all");
                                String principal = recievers.get(0).getNom();
                                Ace aceIn = session.getObjectFactory().createAce(principal, permissions);
                                aceListIn.add(aceIn);

                                document.applyAcl(aceListIn,null, AclPropagation.OBJECTONLY);
                                Map<String, Object> properties = new HashMap<String, Object>();
                                properties.put("cmis:secondaryObjectTypeIds", aspects);
                                properties.put("cm:description", recievers.get(0).getNom());
                                document.updateProperties(properties);



                                System.out.println("Gave "+recievers.get(0).getNom()+" all the rights!");
                                document.applyAcl(aceListIn, null,AclPropagation.REPOSITORYDETERMINED);
                                System.out.println("user Id is: "+recievers.get(0).getId());
                                System.out.println("Document id: "+selectedDoc.getId());
                                selectedDoc.getDestinations().add(recievers.get(0));

                                System.out.println("Added to inbox");
                            } else {
                                System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));
                                System.out.println("right in progress: " + rght);
                                List<String> permissions = new ArrayList<String>();
                                permissions.add("cmis:" + rght);
                                String principal = recievers.get(0).getNom();
                                Ace aceIn = session.getObjectFactory().createAce(principal, permissions);
                                aceListIn.add(aceIn);
                                document.applyAcl(aceListIn, null, AclPropagation.OBJECTONLY);
                                document.refresh();
                                System.out.println("Treated right: " + rght);
                            }
                        }



    }
    public void downloadDocument()
    {
        System.out.println("######################### Download Function #########################");
            Document newDocument = (Document) session.getObject(selectedDoc.getAlfrescoId());
            System.out.println(newDocument.getId());
            System.out.println(newDocument.getContentStreamMimeType());
            try {
                ContentStream cs = newDocument.getContentStream(null);
                BufferedInputStream in = new BufferedInputStream(cs.getStream());
                String home = System.getProperty("user.home");
              //  File file = new File(home+"/Downloads/" + fileName + ".txt");
                FileOutputStream fos = new FileOutputStream(home+"/Downloads/" + selectedDoc.getNom() );
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

    public String doLogout()
    {  System.out.println("######################### Logout Function #########################");
    docs = null;
        //this.session.clear();
        loggedIn = false;
        System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();


        return "/login.xhtml?faces-redirect=true";


    }

    public String inboxPage()
    {
        System.out.println("######################### inbox Function #########################");
        return "/docs-details.xhtml?faces-redirect=true";
    }

    public void checkRights()
    {
        System.out.println("######################### Check rights Function #########################");
        checkRights = "";

        session = prem.getCmisSession("admin","admin");
        OperationContext oc = session.createOperationContext();
        oc.setIncludeAcls(true);
        System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));
        Document dc = (Document)session.getObject(selectedDoc.getAlfrescoId(),oc);
        Acl acl = dc.getAcl();
        List<Ace> entries = acl.getAces();
        for (Ace entry : entries) {
            if (entry.getPrincipalId().equals(nom)) {
               List<String> pr = entry.getPermissions();
             for(String permission:pr)
             {
                 String[] parts = permission.split(":");
                 checkRights+=" "+ parts[1];

             }
                System.out.println("document permissions: "+entry.getPermissions().toString());
            }
        }

        session = prem.getCmisSession(nom,password);
        System.out.println("Session properties: "+ session.getSessionParameters().get(SessionParameter.USER));
    }

    public String createSite()
    {
        System.out.println("######################### Create site Function #########################");
        Site site = new Site();
        site.setNom(siteName);
        site.setSiteId(siteId);
        site.setVisibility(siteVisibility);
        site.setManager(personne);
        Map<String,Personne> members = new HashMap<String,Personne>();
       members.put("manager",personne);
        System.out.println("Person id: "+personne.getId());
        //personne.getSites().add(site);
        //site.setMembers(members);
       // personneRepository.save(personne);
        siteRepository.save(site);
        allSites = (List<Site>)siteRepository.findAll();
        System.out.println("Site Created");

        return "all-sites?faces-redirect=true";


    }

    public String goToSiteCreation()
    {
        return "site-form?faces-redirect=true";
    }
    public String goToAllSites()
    {
        allSites = (List<Site>)siteRepository.findAll();
        System.out.println("Sites size: "+allSites.size());
        return "all-sites?faces-redirect=true";
    }
    public Set<Personne> retrieveMembers()
    {
        membersBySite.removeAll(membersBySite);
        siteMembers = memberRepository.getMembersBySite(selectedSite.getId());
        for (Member mm: siteMembers)
        {
            if(mm.getSite().getId()== selectedSite.getId())
                System.out.println("Retrieving member: "+mm.getUser().getNom());
            membersBySite.add(mm.getUser());
        }
        return membersBySite;
    }
    public String goToSelectedSite()
    {
        System.out.println("######################### Navigation Function #########################");
       retrieveMembers();
        if(selectedSite.getManager() != null && selectedSite.getManager().getId()==personne.getId())
        {
            return "site-details?faces-redirect=true";
        }else
            return "site-visitor?faces-redirect=true";


    }

    public String goToAddMembers()
    {

        return "site-addmembers?faces-redirect=true";
    }

    public void addMembers()
    {
        System.out.println("######################### Add member Function #########################");
        System.out.println("selected person: "+personToAdd.getNom());
        peopleToAdd.add(personToAdd);
        System.out.println("People to add size: " +peopleToAdd.size());
    }

    public String confirmAdd()
    {
        System.out.println("######################### Confirm add Function #########################");
        membersBySite=retrieveMembers();
        for(Personne per:peopleToAdd)
        {
            System.out.println("Testing on: "+per.getNom());
            if(membersBySite.contains(per)){
                System.out.println("User is already a member !!!");

            }else {
                System.out.println("Confirming adding user: " + per.getNom() + " to Site: " + selectedSite.getNom());
                Member member = new Member();
                member.setUser(per);
                System.out.println("Chosen Role: "+chosenRole);
                member.setRole(chosenRole);
                member.setSite(selectedSite);
                memberRepository.save(member);
                System.out.println("Conifrmation complete !");
            }

        }
     membersBySite=retrieveMembers();
        for (Personne m:membersBySite)
        {
            System.out.println("Member name: "+m.getNom());
        }
        peopleToAdd.removeAll(peopleToAdd);
        return "site-addmembers?faces-redirect=true";
    }
    public void deleteMember()
    {
        for (Member m:siteMembers)
        {
            if(m.getUser().getId() == personToDelete.getId())
            {
                memberRepository.delete(m);
                System.out.println("Removing user: "+personToDelete.getNom());

            }
        }
        retrieveMembers();
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

    public List<String> getSelectedRights() {
        return selectedRights;
    }

    public void setSelectedRights(List<String> selectedRights) {
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

    public Set<Doc_Person> getInbox() {
        return inbox;
    }

    public void setInbox(Set<Doc_Person> inbox) {
        this.inbox = inbox;
    }

    public List<Doc> getSelectedInbox() {
        return selectedInbox;
    }

    public void setSelectedInbox(List<Doc> selectedInbox) {
        this.selectedInbox = selectedInbox;
    }

    public InboxRepository getInboxRepository() {
        return inboxRepository;
    }

    public void setInboxRepository(InboxRepository inboxRepository) {
        this.inboxRepository = inboxRepository;
    }

    public Set<Doc> getDocInbox() {
        return docInbox;
    }

    public void setDocInbox(Set<Doc> docInbox) {
        this.docInbox = docInbox;
    }

    public Doc getDocDetails() {
        return docDetails;
    }

    public void setDocDetails(Doc docDetails) {
        this.docDetails = docDetails;
    }

    public Doc getSelectedDoc() {
        return selectedDoc;
    }

    public void setSelectedDoc(Doc selectedDoc) {
        this.selectedDoc = selectedDoc;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getCheckRights() {
        return checkRights;
    }

    public void setCheckRights(String checkRights) {
        this.checkRights = checkRights;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public void setSiteDescription(String siteDescription) {
        this.siteDescription = siteDescription;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteVisibility() {
        return siteVisibility;
    }

    public void setSiteVisibility(String siteVisibility) {
        this.siteVisibility = siteVisibility;
    }

    public List<Site> getAllSites() {
        return allSites;
    }

    public void setAllSites(List<Site> allSites) {
        this.allSites = allSites;
    }

    public List<Site> getFilteredSites() {
        return filteredSites;
    }

    public void setFilteredSites(List<Site> filteredSites) {
        this.filteredSites = filteredSites;
    }

    public Site getSelectedSite() {
        return selectedSite;
    }

    public void setSelectedSite(Site selectedSite) {
        this.selectedSite = selectedSite;
    }

    public Set<Member> getSiteMembers() {
        return siteMembers;
    }

    public void setSiteMembers(Set<Member> siteMembers) {
        this.siteMembers = siteMembers;
    }

    public Personne getPersonToAdd() {
        return personToAdd;
    }

    public void setPersonToAdd(Personne personToAdd) {
        this.personToAdd = personToAdd;
    }

    public Set<Personne> getPeopleToAdd() {
        return peopleToAdd;
    }

    public void setPeopleToAdd(Set<Personne> peopleToAdd) {
        this.peopleToAdd = peopleToAdd;
    }

    public String getChosenRole() {
        return chosenRole;
    }

    public void setChosenRole(String chosenRole) {
        this.chosenRole = chosenRole;
    }

    public Set<Personne> getMembersBySite() {
        return membersBySite;
    }

    public void setMembersBySite(Set<Personne> membersBySite) {
        this.membersBySite = membersBySite;
    }

    public Personne getPersonToDelete() {
        return personToDelete;
    }

    public void setPersonToDelete(Personne personToDelete) {
        this.personToDelete = personToDelete;
    }
}
