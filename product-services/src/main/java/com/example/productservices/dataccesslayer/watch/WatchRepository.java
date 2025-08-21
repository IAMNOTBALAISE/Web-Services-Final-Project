package com.example.productservices.dataccesslayer.watch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WatchRepository extends JpaRepository<Watch, Integer> {

    List<Watch> findAllByCatalogIdentifier_CatalogId(String catalogId);

//    List<Watch> findAllByCatalogIdentifier_CatalogIdAndWatchStatusEqualsAndUsageTypeEquals(String catalogId, WatchStatus status, UsageType usageType);
//
//    List<Watch> findAllByCatalogIdentifier_CatalogIdAndWatchStatusEquals(String catalogId, WatchStatus status);

    List<Watch> findAllByCatalogIdentifier_CatalogIdAndUsageTypeEquals(String catalogId, UsageType usageType);

    Watch findByWatchIdentifier_WatchId(String watchId);

    boolean existsByModelAndCatalogIdentifier_CatalogId(String model, String catalogId);
}
