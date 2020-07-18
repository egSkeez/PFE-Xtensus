package tn.xtensus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.xtensus.entities.Personne;
import tn.xtensus.repository.PersonneRepository;
@Service
public class PersonneServiceImpl implements IPersonneService{
    @Autowired
    PersonneRepository personneRepository;

    public Personne getPersonneByNomAndPassword(String nom, String password){
        return personneRepository.getPersonneByNomAndPassword(nom,password);
    }
}
