package com.example.productservices.businesslayer;



import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.dataccesslayer.catalog.CatalogRepository;
import com.example.productservices.dataccesslayer.watch.*;
import com.example.productservices.datamapperlayer.WatchMapper.WatchRequestMapper;
import com.example.productservices.datamapperlayer.WatchMapper.WatchResponseMapper;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchRequestModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchResponseModel;
import com.example.productservices.utils.exceptions.DuplicateWatchModelException;
import com.example.productservices.utils.exceptions.InvalidInputException;
import com.example.productservices.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogWatchServiceImpl implements CatalogWatchService {

    private final CatalogRepository catalogRepository;

    private final WatchRepository watchRepository;

    private final WatchResponseMapper watchResponseMapper;

    private final WatchRequestMapper watchRequestMapper;

   // public final OrderRepository orderRepository;


    @Autowired
    public CatalogWatchServiceImpl(CatalogRepository catalogRepository, WatchRepository watchRepository, WatchResponseMapper watchResponseMapper, WatchRequestMapper watchRequestMapper /*, OrderRepository orderRepository*/) {
        this.catalogRepository = catalogRepository;
        this.watchRepository = watchRepository;
        this.watchResponseMapper = watchResponseMapper;
        this.watchRequestMapper = watchRequestMapper;
//        this.orderRepository = orderRepository;
    }

    @Override
    public List<WatchResponseModel> getWatchesInCatalogWithFiltering(String catalogId, Map<String, String> queryParams) {


//        String watchStatus = queryParams.get("status");
        String usageType = queryParams.get("usage");

        if (!catalogRepository.existsByCatalogIdentifier_CatalogId(catalogId)) {
            throw new InvalidInputException("Catalog does not exist");
        }

//        Map<String, WatchStatus> statusMap = new HashMap<String, WatchStatus>();
//        statusMap.put("available", WatchStatus.AVAILABLE);
//        statusMap.put("sale_pending", WatchStatus.SALE_PENDING);
//        statusMap.put("sold_out", WatchStatus.SOLD_OUT);


        Map<String, UsageType> usageTypeMap = new HashMap<String, UsageType>();
        usageTypeMap.put("new", UsageType.NEW);
        usageTypeMap.put("used", UsageType.USED);

//        WatchStatus watchStatusEnum = null;
//        if (watchStatus != null) {
//            watchStatusEnum = statusMap.get(watchStatus.toLowerCase());
//        }

        UsageType usageTypeEnum = null;
        if (usageType != null) {
            usageTypeEnum = usageTypeMap.get(usageType.toLowerCase());
        }

//        if (watchStatusEnum != null && usageTypeEnum != null) {
//            return watchResponseMapper.entityListToResponseModelList(
//                    watchRepository.findAllByCatalogIdentifier_CatalogIdAndWatchStatusEqualsAndUsageTypeEquals(
//                            catalogId, watchStatusEnum, usageTypeEnum
//                    )
//            );
//        }
//
//        if (watchStatusEnum != null) {
//            return watchResponseMapper.entityListToResponseModelList(
//                    watchRepository.findAllByCatalogIdentifier_CatalogIdAndWatchStatusEquals(
//                            catalogId, watchStatusEnum
//                    )
//            );
//        }

        if (usageTypeEnum != null) {
            return watchResponseMapper.entityListToResponseModelList(
                    watchRepository.findAllByCatalogIdentifier_CatalogIdAndUsageTypeEquals(
                            catalogId, usageTypeEnum
                    )
            );
        }


        return watchResponseMapper.entityListToResponseModelList(
                watchRepository.findAllByCatalogIdentifier_CatalogId(catalogId));
    }

    @Override
    public List<WatchResponseModel> getWatchesWithFilter(Map<String, String> queryParams) {

        return watchResponseMapper.entityListToResponseModelList(watchRepository.findAll());
    }

    @Override
    public WatchResponseModel getCatalogWatchByID(String watchId) {

        Watch watch = watchRepository.findByWatchIdentifier_WatchId(watchId);

        if (watch == null) {
            throw new InvalidInputException("Watch does not exist");
        }
        return watchResponseMapper.entityToResponseModel(watch);
    }

    @Override
    public WatchResponseModel addWatches(WatchRequestModel watchRequestModel, String catalogId) {

        Catalog existingCatalog = catalogRepository.findByCatalogIdentifier_CatalogId(catalogId);
        if (existingCatalog == null) {
            throw new InvalidInputException("Catalog does not exist");
        }

        String model = watchRequestModel.getModel();

        if (watchRepository.existsByModelAndCatalogIdentifier_CatalogId(model, catalogId)) {
            throw new DuplicateWatchModelException(model);
        }

        BigDecimal totalAccessoryCost = BigDecimal.ZERO;
        if (watchRequestModel.getAccessories() != null) {
            totalAccessoryCost = watchRequestModel.getAccessories().stream()
                    .map(Accessory::getAccessoryCost)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Price price = new Price(
                watchRequestModel.getPrice().getMsrp(),
                watchRequestModel.getPrice().getCost(),
                totalAccessoryCost
        );

        Watch w = watchRequestMapper.requestModelToEntity(watchRequestModel);
        w.setWatchIdentifier(new WatchIdentifier());
        w.setCatalogIdentifier(existingCatalog.getCatalogIdentifier());
        w.setPrice(price);
        w.setWatchBrand(watchRequestModel.getWatchBrand());

        // Set quantity explicitly from request
        w.setQuantity(watchRequestModel.getQuantity());

        watchRepository.save(w);

        return watchResponseMapper.entityToResponseModel(w);
    }


    @Override
    public WatchResponseModel updateWatchInInventory(String catalogId, String watchId, WatchRequestModel watchRequestModel) {


        Catalog existingCatalog = catalogRepository.findByCatalogIdentifier_CatalogId(catalogId);
        if (existingCatalog == null) {
            throw new InvalidInputException("Catalog does not exist");
        }

        Watch existingWatch = watchRepository.findByWatchIdentifier_WatchId(watchId);
        if (existingWatch == null) {
            throw new NotFoundException("Unknown watch Id provided : " + watchId);
        }

        BigDecimal totalAccessoryCost = watchRequestModel.getAccessories().stream()
                .map(Accessory::getAccessoryCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Price price = new Price(
                watchRequestModel.getPrice().getMsrp(),
                watchRequestModel.getPrice().getCost(),
                totalAccessoryCost
        );

        String newModel = watchRequestModel.getModel();

        if (!existingWatch.getModel().equals(newModel)
                && watchRepository.existsByModelAndCatalogIdentifier_CatalogId(newModel, catalogId)) {
            throw new DuplicateWatchModelException(newModel);
        }

        Watch updated = watchRequestMapper.requestModelToEntity(watchRequestModel);

        // preserve identity & embeddables
        updated.setId(existingWatch.getId());
        updated.setWatchIdentifier(existingWatch.getWatchIdentifier());
        updated.setCatalogIdentifier(existingWatch.getCatalogIdentifier());
        updated.setPrice(price);
        updated.setWatchBrand(watchRequestModel.getWatchBrand());

        // explicitly handle quantity
        if (watchRequestModel.getQuantity() != null) {
            updated.setQuantity(watchRequestModel.getQuantity());
        } else {
            updated.setQuantity(existingWatch.getQuantity());
        }

        Watch saved = watchRepository.save(updated);
        return watchResponseMapper.entityToResponseModel(saved);

        }





    @Override
    public  String removeWatchInCatalog(String catalogId, String watchId){

        Catalog existingCatalog = catalogRepository.findByCatalogIdentifier_CatalogId(catalogId);
        if(existingCatalog == null){
            throw new InvalidInputException("Catalog does not exist");
        }
        Watch existingWatch = watchRepository.findByWatchIdentifier_WatchId(watchId);
        if(existingWatch == null){
            throw new NotFoundException("Unknown watch Id provided : " + existingWatch);

        }
        watchRepository.delete(existingWatch);
        return "Watch with ID" + watchId + " was successfully removed";
    }

}
