package tn.xtensus.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.http.*;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import tn.xtensus.entities.NetworkEntry;
import tn.xtensus.entities.NetworkList;
import tn.xtensus.util.Config;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 *
 * @author motaz souid
 */
public class LocalConfig {
    //parametre de l'exple de l'impl�mentation du l'api alfresco ReST API
    public static final String SITES_URL = "/public/alfresco/versions/1/sites/";
    public static final String NODES_URL = "/public/alfresco/versions/1/nodes/";
    public static final String PEOPLE_URL = "/public/alfresco/versions/1/people/";

    private String homeNetwork;

    /**
     *
     * @param parentFolderId
     * @param folderName
     * @return Folder
     *
     */
    public Folder createFolder(String parentFolderId, String folderName) {
        Session cmisSession = getCmisSession("test","test");
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
/*
    public Document createDocument(Folder parentFolder,
                                   File file,
                                   String fileType)
            throws FileNotFoundException {
        return createDocument(parentFolder, file, fileType, null);
    } */

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
    public Document createDocument( Session cmisSession,Folder parentFolder,
                                   File file,
                                   String fileType,
                                   Map<String, Object> props)
            throws FileNotFoundException {


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

    /**
     * parametres de configuration
     */
    //public static final String CMIS_URL = "/public/cmis/versions/1.0/atom";
    public static final String CMIS_URL = "/public/cmis/versions/1.1/atom";

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private HttpRequestFactory requestFactory;
    private Session cmisSession;

    public String getAtomPubURL(HttpRequestFactory requestFactory) {
        String alfrescoAPIUrl = getAlfrescoAPIUrl();
        String atomPubURL = null;

        try {
            atomPubURL = alfrescoAPIUrl + getHomeNetwork() + CMIS_URL;
        } catch (IOException ioe) {
            System.out.println("Warning: Couldn't determine home network, defaulting to -default-");
            atomPubURL = alfrescoAPIUrl + "-default-" + CMIS_URL;
        }

        return atomPubURL;
    }

    /**
     * connexion a une serveur alfresco local
     *
     * @return Session
     */
    public Session getCmisSession(String username,String password) {
        if (cmisSession == null || !cmisSession.getSessionParameters().get(SessionParameter.USER).equals(username)) {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom");
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
            parameter.put(SessionParameter.USER, username);
            parameter.put(SessionParameter.PASSWORD, password);
            //parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

            List<Repository> repositories = factory.getRepositories(parameter);

            cmisSession = repositories.get(0).createSession();
        }
        return this.cmisSession;
    }

    /**
     *
     * @return HttpRequestFactory
     */
    public HttpRequestFactory getRequestFactory() {
        if (this.requestFactory == null) {
            this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    request.setParser(new JsonObjectParser(new JacksonFactory()));
                    request.getHeaders().setBasicAuthentication("admin","admin");
                }
            });
        }
        return this.requestFactory;
    }

    public String getAlfrescoAPIUrl() {
        String host = "http://localhost:8080/alfresco";
        return host + "/api/";
    }



}
