package com.vishal.campsite.repository;

import com.vishal.campsite.model.Campsite;
import org.springframework.data.repository.CrudRepository;

public interface CampsiteRepository extends CrudRepository<Campsite, Long> {
}
