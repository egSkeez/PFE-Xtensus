package tn.xtensus.service;

import org.springframework.beans.factory.annotation.Autowired;
import tn.xtensus.entities.Doc;
import tn.xtensus.repository.DocRepository;

import java.util.List;

public class DocServiceImpl implements IDocService {
    private Doc doc = new Doc();
    private List<Doc> docs ;
    @Autowired
    DocRepository docRepository;

    public String save() {
        docRepository.save(doc);
        doc = new Doc();
        return "/doc-list.xhtml?faces-redirect=true";
    }

    @Override
    public Doc getDoc() {
        return doc;
    }


    public Doc getProduct() {
        return doc;
    }


    public String delete(int id) {
        Doc d = docRepository.findById(id).get();
        docRepository.delete(d);
        return "/doc-list.xhtml?faces-redirect=true";

    }


    public void loadData() {
        docs = (List<Doc>)docRepository.findAll();

    }


    public List<Doc> getDocs() {
        return docs;
    }


}
