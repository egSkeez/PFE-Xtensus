package tn.xtensus.repository;

import org.springframework.data.repository.CrudRepository;
import tn.xtensus.entities.Doc;
import tn.xtensus.entities.Site;

public interface SiteRepository extends CrudRepository<Site, Integer > {
}
