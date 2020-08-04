package tn.xtensus.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Doc_Person;
import tn.xtensus.entities.Personne;

import java.util.Set;

public interface InboxRepository extends CrudRepository<Doc_Person, Integer > {
    @Query("select e from Doc_Person e where e.user.id=:userId")
    public Set<Doc_Person> getInboxByUser(@Param("userId")int userId);
}
