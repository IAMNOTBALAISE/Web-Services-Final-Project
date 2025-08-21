package com.example.productservices.businesslayer;


import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.catalog.CatalogRepository;
import com.example.productservices.dataccesslayer.watch.Watch;
import com.example.productservices.dataccesslayer.watch.WatchRepository;
import com.example.productservices.datamapperlayer.CatalogMapper.CatalogRequestMapper;
import com.example.productservices.datamapperlayer.CatalogMapper.CatalogResponseMapper;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogRequestModel;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogResponseModel;
import com.example.productservices.utils.exceptions.DuplicateCatalogTypeException;
import com.example.productservices.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogRequestMapper catalogRequestMapper;
    private final CatalogResponseMapper catalogResponseMapper;
    private final WatchRepository watchRepository;

    @Autowired
    public CatalogServiceImpl(CatalogRepository catalogRepository, CatalogRequestMapper catalogRequestMapper, CatalogResponseMapper catalogResponseMapper, WatchRepository watchRepository) {
        this.catalogRepository = catalogRepository;
        this.catalogRequestMapper = catalogRequestMapper;
        this.catalogResponseMapper = catalogResponseMapper;
        this.watchRepository = watchRepository;
    }


    @Override
    public List<CatalogResponseModel> getCatalogs() {

        List<Catalog> catalogs = catalogRepository.findAll();
        return this.catalogResponseMapper.entityListToResponseModelList(catalogs);
    }

    @Override
    public CatalogResponseModel getCatalogById(String catalogId) {

        Catalog foundCatalog = this.catalogRepository.findByCatalogIdentifier_CatalogId(catalogId);
        if(foundCatalog == null) {

            throw new RuntimeException("Catalog not found");
        }else {
            return this.catalogResponseMapper.entityToResponseModel(foundCatalog);
        }
    }


    @Override
    public CatalogResponseModel addCatalog(CatalogRequestModel catalogRequestModel) {

        String type = catalogRequestModel.getType();

        if (catalogRepository.existsByType(type)) {
            throw new DuplicateCatalogTypeException(type);
        }
        else {

            Catalog catalog = this.catalogRequestMapper.requestModelToEntity(catalogRequestModel);

            catalog.setCatalogIdentifier(new CatalogIdentifier());
            catalog.setType(type);
            catalog.setDescription(catalogRequestModel.getDescription());
            this.catalogRepository.save(catalog);
            return this.catalogResponseMapper.entityToResponseModel(catalog);
        }
    }


    @Override
    public CatalogResponseModel updateCatalog(CatalogRequestModel catalogRequestModel, String catalogId){


        Catalog foundCatalog = this.catalogRepository.findByCatalogIdentifier_CatalogId(catalogId);
        if(foundCatalog == null) {

            throw new RuntimeException("Catalog not found");
        } else if (catalogId == null || catalogId.isEmpty()) {

            throw new IllegalArgumentException("Catalog id cannot be empty in the request bar");
        }

        String newType = catalogRequestModel.getType();
        // if you’re changing type, make sure no other catalog already has it
        if (! foundCatalog.getType().equals(newType)
                && catalogRepository.existsByType(newType)) {
            throw new DuplicateCatalogTypeException(newType);
        }




        foundCatalog.setType(catalogRequestModel.getType());
        foundCatalog.setDescription(catalogRequestModel.getDescription());

        Catalog updatedCatalog = this.catalogRepository.save(foundCatalog);

        return this.catalogResponseMapper.entityToResponseModel(updatedCatalog);

    }
    @Override
     public String deleteCatalog(String catalogId){

        Catalog existingCatalog = catalogRepository.findByCatalogIdentifier_CatalogId(catalogId);
        if(existingCatalog == null) {

            throw new NotFoundException("This catalog does not exist");
        }

        List<Watch> watches = watchRepository.findAllByCatalogIdentifier_CatalogId(catalogId);
        watches.forEach(watch -> watchRepository.delete(watch));

        catalogRepository.delete(existingCatalog);

        return "Catalog with id: " + catalogId + " was deleted";


    }

}
