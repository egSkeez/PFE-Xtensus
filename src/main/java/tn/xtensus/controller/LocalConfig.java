package tn.xtensus.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import tn.xtensus.util.Config;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 *
 * @author motaz souid
 */
public class LocalConfig extends RemoteConfig {

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
    public Session getCmisSession() {
        if (cmisSession == null) {
            // default factory implementation
            SessionFactory factory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();

            // connection settings
            parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/alfresco/api/-default-/public/cmis/versions/1.1/atom");
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
            parameter.put(SessionParameter.USER, "admin");
            parameter.put(SessionParameter.PASSWORD, "admin");
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
