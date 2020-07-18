package tn.xtensus.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tn.xtensus.entities.Personne;

public interface PersonneRepository extends CrudRepository<Personne, Integer> {
    @Query("select e from Personne e where e.nom=:nom and e.password=:password")
    public Personne getPersonneByNomAndPassword(@Param("nom")String nom,
                                                @Param("password")String password);
}
