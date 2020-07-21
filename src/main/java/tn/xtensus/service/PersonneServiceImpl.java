package tn.xtensus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Personne;
import tn.xtensus.repository.PersonneRepository;

import java.util.List;

@Service
public class PersonneServiceImpl implements IPersonneService{
    @Autowired
    PersonneRepository personneRepository;
    private List<Personne> people ;
    public Personne getPersonneByNomAndPassword(String nom, String password){
        return personneRepository.getPersonneByNomAndPassword(nom,password);
    }

    public List<Personne> loadData()
    {
        System.out.println("Loadind people in PersonneServiceImpl!!!");
        return people = (List<Personne>)personneRepository.findAll();
    }

    public List<Personne> getPersonnes() {
        return people;
    }


}
