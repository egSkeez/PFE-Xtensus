package tn.xtensus.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tn.xtensus.entities.Doc;

import java.util.Set;

public interface DocRepository extends CrudRepository<Doc, Integer > {
    @Query("select e from Doc e where e.nom=:alfrescoId")
    public Doc getDocByName(@Param("alfrescoId")String alfrescoId);
}
