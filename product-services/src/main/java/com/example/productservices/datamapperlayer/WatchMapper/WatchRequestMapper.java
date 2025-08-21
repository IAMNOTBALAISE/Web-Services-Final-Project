package com.example.productservices.datamapperlayer.WatchMapper;



import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.watch.Price;
import com.example.productservices.dataccesslayer.watch.Watch;
import com.example.productservices.dataccesslayer.watch.WatchBrand;
import com.example.productservices.dataccesslayer.watch.WatchIdentifier;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface WatchRequestMapper {

    @Mapping(target="id",            ignore=true)
    @Mapping(target="watchIdentifier", ignore=true) // set in service
    @Mapping(target="catalogIdentifier",ignore=true) // set in service
    @Mapping(target="price",            ignore=true) // set in service
    @Mapping(target="watchBrand",      ignore=true) // set in service
        // everything else (model, material, status, usageType, accessories) is auto‐mapped
    Watch requestModelToEntity(WatchRequestModel req);
}
