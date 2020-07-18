package tn.xtensus.entities;

import com.google.api.client.util.Key;


public class Network {
    @Key
    public String id;

    @Key
    public boolean homeNetwork;

    //@Key
    //DateTime createdAt;

    @Key
    public boolean paidNetwork;

    @Key
    public boolean isEnabled;

    @Key
    public String subscriptionLevel;
}
