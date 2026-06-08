package com.wefit.activityService.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.wefit.activityService.entities.Activity;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, Long> {

}
