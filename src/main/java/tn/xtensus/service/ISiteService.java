package tn.xtensus.service;

import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Site;

import java.util.List;

public interface ISiteService {
    public String save();
    public Site getSite();
    public String delete(int id);
    public void loadData();
    public List<Doc> getSites();


}
