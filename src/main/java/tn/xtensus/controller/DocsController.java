package tn.xtensus.controller;

import org.ocpsoft.rewrite.annotation.Join;
import org.ocpsoft.rewrite.annotation.RequestAction;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.ocpsoft.rewrite.faces.annotation.Deferred;
import org.ocpsoft.rewrite.faces.annotation.IgnorePostback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tn.xtensus.entities.Doc;
import tn.xtensus.service.IDocService;

import java.util.List;

@Scope(value = "session")
@Component(value = "docsList")
@ELBeanName(value = "docsList")
//@Join(path = "/", to = "/docsList.jsf")
public class DocsController implements IDocsController {
    private List<Doc> docs;
    @Autowired
    IDocService iDocService;
    @Deferred
    @RequestAction
    @IgnorePostback
    public void loadData() {
        System.out.println("Loading data");
        iDocService.loadData();

    }

    public List<Doc> getDocs() {
        System.out.println("Fonction getDocs()");
        return iDocService.getDocs();
    }
}
