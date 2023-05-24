package com.example.dbproc.model;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SomeRepository extends ReactiveCrudRepository<SomeDoc, String> {
}
