package tn.xtensus.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tn.xtensus.entities.Doc_Person;
import tn.xtensus.entities.Member;
import tn.xtensus.entities.Site;

import java.util.Set;

public interface MemberRepository extends CrudRepository<Member, Integer > {
    @Query("select e from Member e where e.site.id=:siteId")
    public Set<Member> getMembersBySite(@Param("siteId")int userId);
}
