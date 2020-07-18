package tn.xtensus.controller;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import tn.xtensus.entities.NetworkEntry;
import tn.xtensus.entities.NetworkList;
import tn.xtensus.util.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author motaz souid
 *
 */
abstract public class RemoteConfig {
	//parametre de l'exple de l'impl�mentation du l'api alfresco ReST API
    public static final String SITES_URL = "/public/alfresco/versions/1/sites/";
    public static final String NODES_URL = "/public/alfresco/versions/1/nodes/";
    public static final String PEOPLE_URL = "/public/alfresco/versions/1/people/";

    private String homeNetwork;

    /**
     *
     * @param cmisSession
     * @param parentFolderId
     * @param folderName
     * @return Folder
     *
     */
    public Folder createFolder(String parentFolderId, String folderName) {
        Session cmisSession = getCmisSession();
        Folder rootFolder = (Folder) cmisSession.getObject(parentFolderId);

        Folder subFolder = null;
        try {
            // Making an assumption here that you probably wouldn't normally do
            subFolder = (Folder) cmisSession.getObjectByPath(rootFolder.getPath() + "/" + folderName);
            System.out.println("Folder already existed!");
        } catch (CmisObjectNotFoundException onfe) {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("cmis:objectTypeId",  "cmis:folder");
            props.put("cmis:name", folderName);
            subFolder = rootFolder.createFolder(props);
            String subFolderId = subFolder.getId();
            System.out.println("Created new folder: " + subFolderId);
        }

        return subFolder;
    }

    public String getHomeNetwork() throws IOException {
        if (this.homeNetwork == null) {
            GenericUrl url = new GenericUrl(getAlfrescoAPIUrl());

            HttpRequest request = getRequestFactory().buildGetRequest(url);

            NetworkList networkList = request.execute().parseAs(NetworkList.class);
            System.out.println("Found " + networkList.list.pagination.totalItems + " networks.");
            for (NetworkEntry networkEntry : networkList.list.entries) {
                if (networkEntry.entry.homeNetwork) {
                    this.homeNetwork = networkEntry.entry.id;
                }
            }

            if (this.homeNetwork == null) {
                this.homeNetwork = "-default-";
            }

            System.out.println("Your home network appears to be: " + homeNetwork);
        }
        return this.homeNetwork;
    }

    public Document createDocument(Folder parentFolder,
                                   File file,
                                   String fileType)
            throws FileNotFoundException {
        return createDocument(parentFolder, file, fileType, null);
    }

    /**
     *
     * @param parentFolder
     * @param file
     * @param fileType
     * @param props
     * @return
     * @throws FileNotFoundException
     *
     */
    public Document createDocument(Folder parentFolder,
                                   File file,
                                   String fileType,
                                   Map<String, Object> props)
            throws FileNotFoundException {

        Session cmisSession = getCmisSession();

        String fileName = file.getName();

        // cr�er une map de parametres
        if (props == null) {
            props = new HashMap<String, Object>();
        }

        // ajouter l'id de l'objet
        if (props.get("cmis:objectTypeId") == null) {
            props.put("cmis:objectTypeId",  "cmis:document");
        }

        // ajouter le nom
        if (props.get("cmis:name") == null) {
            props.put("cmis:name", fileName);
        }

        ContentStream contentStream = cmisSession.getObjectFactory().
                  createContentStream(
                    fileName,
                    file.length(),
                    fileType,
                    new FileInputStream(file)
                  );

        Document document = null;
        try {
            document = parentFolder.createDocument(props, contentStream, null);
            System.out.println("Created new document: " + document.getId());
        } catch (CmisContentAlreadyExistsException ccaee) {
            document = (Document) cmisSession.getObjectByPath(parentFolder.getPath() + "/" + fileName);
            System.out.println("Document already exists: " + fileName);
        }

        return document;
    }

  
    public String getSite() {
        return Config.getConfig().getProperty("site");
    }

    public String getFolderName() {
        return Config.getConfig().getProperty("folder_name");
    }

    public File getLocalFile() {
        String filePath = Config.getConfig().getProperty("local_file_path");
        return new File(filePath);
    }

    public String getLocalFileType() {
        return Config.getConfig().getProperty("local_file_type");
    }

    abstract public String getAlfrescoAPIUrl();
    abstract public Session getCmisSession();
    abstract public HttpRequestFactory getRequestFactory();
}
