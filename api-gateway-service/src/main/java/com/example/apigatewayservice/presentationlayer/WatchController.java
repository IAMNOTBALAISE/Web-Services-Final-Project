package com.example.apigatewayservice.presentationlayer;



import com.example.apigatewayservice.businesslayer.productservicesBusinessLayer.CatalogWatchService;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("api/v1/watches")
public class WatchController {

    private final CatalogWatchService catalogWatchService;

    public WatchController(CatalogWatchService catalogWatchService) {
        this.catalogWatchService = catalogWatchService;
    }

    @GetMapping()
    public ResponseEntity<List<WatchResponseModel>> getWatchesWithFilter(@RequestParam Map <String,String> queryParams){

        List<WatchResponseModel> list = catalogWatchService.getWatchesWithFilter(queryParams);
        list.forEach(w -> {
            w.add(linkTo(methodOn(WatchController.class)
                    .getWatchInCatalogByID(w.getWatchId())).withSelfRel());
            w.add(linkTo(methodOn(WatchController.class)
                    .getWatchesWithFilter(null)).withRel("all-watches"));
        });
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{watchId}")
    public ResponseEntity<WatchResponseModel> getWatchInCatalogByID(@PathVariable String watchId){

        WatchResponseModel w = catalogWatchService.getCatalogWatchByID(watchId);
        w.add(linkTo(methodOn(WatchController.class)
                .getWatchInCatalogByID(watchId)).withSelfRel());
        w.add(linkTo(methodOn(WatchController.class)
                .getWatchesWithFilter(null)).withRel("all-watches"));
        return ResponseEntity.ok(w);
    }
}



