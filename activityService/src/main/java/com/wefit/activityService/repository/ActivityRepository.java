package com.wefit.activityService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.wefit.activityService.entities.Activity;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

}
