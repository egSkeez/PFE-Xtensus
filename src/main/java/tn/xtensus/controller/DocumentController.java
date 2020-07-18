package tn.xtensus.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tn.xtensus.entities.Doc;
import tn.xtensus.repository.DocRepository;
import tn.xtensus.service.IDocService;

@Scope(value = "session")
@Component(value = "documentController")
@ELBeanName(value = "documentController")
//@Join(path = "/", to = "/welcome.jsf")
public class DocumentController extends LocalConfig implements IDocumentController{
    @Autowired
    IDocService iDocService;
    private UploadedFile file;
    public void uploadFile() throws IOException {
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
    	    Folder folderAssociations = ((Folder) session.getObjectByPath("/Espaces Utilisateurs/motaz"))
    	    .createFolder(newFolderProps); */

            HashMap<String, Object> newFileProps = new HashMap<String, Object>();
            ContentStream contentStream = new ContentStreamImpl(file.getFileName(), null,
                    file.getContentType(), file.getInputstream());


            //newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            newFileProps.put(PropertyIds.NAME, file.getFileName());
            newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            List<String> permissions = new ArrayList<String>();
            permissions.add("cmis:all");
            String principal = "admin";
            Ace aceIn = ((Session) session).getObjectFactory().createAce(principal, permissions);
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


    public String save() {
        return iDocService.save();
    }


    public Doc getDocument() {
       return iDocService.getDoc();
    }


    public String delete(int id) {

        return iDocService.delete(id);
    }

    public void loadData() {
        iDocService.loadData();

    }


    public List<Doc> getDocs() {
        return iDocService.getDocs();
    }


    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }
}
