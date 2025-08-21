package com.example.productservices.datamapperlayer.WatchMapper;


import com.example.productservices.dataccesslayer.watch.Watch;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WatchResponseMapper {

    @Mappings({
            @Mapping(expression = "java(watch.getWatchIdentifier().getWatchId())", target = "watchId"),
            @Mapping(expression = "java(watch.getCatalogIdentifier().getCatalogId())", target = "catalogId"),
            @Mapping(target = "accessories", source = "accessories"),
            @Mapping(target = "price", source = "price"),
            @Mapping(target = "watchBrand", source = "watchBrand")
    })
    WatchResponseModel entityToResponseModel(Watch watch);

    List<WatchResponseModel> entityListToResponseModelList(List<Watch> watches);


}
