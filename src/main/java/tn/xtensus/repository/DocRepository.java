package tn.xtensus.repository;

import org.springframework.data.repository.CrudRepository;
import tn.xtensus.entities.Doc;

public interface DocRepository extends CrudRepository<Doc, Integer > {
}
