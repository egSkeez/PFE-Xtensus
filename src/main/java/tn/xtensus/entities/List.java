package tn.xtensus.entities;

import java.util.ArrayList;

import com.google.api.client.util.Key;


public class List<T extends Entry> {
    @Key
    public ArrayList<T> entries;

    @Key
    public Pagination pagination;
}
