package com.example.apigatewayservice.presentationlayer;



import com.example.apigatewayservice.businesslayer.productservicesBusinessLayer.CatalogWatchService;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchRequestModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.WatchResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("api/v1/catalogs/{catalog_id}/watches")
public class CatalogWatchController {

    private final CatalogWatchService catalogWatchService;

    public CatalogWatchController(CatalogWatchService catalogWatchService) {
        this.catalogWatchService = catalogWatchService;
    }

    @GetMapping()
    public ResponseEntity<List<WatchResponseModel>> getWatchesInCatalogWithFiltering(@PathVariable("catalog_id") String catalogId, @RequestParam Map<String,String> queryParams) {

        List<WatchResponseModel> list = catalogWatchService.getWatchesInCatalogWithFiltering(catalogId, queryParams);

        list.forEach(w -> {
            // self link
            w.add(linkTo(methodOn(CatalogWatchController.class)
                    .getWatchInCatalogByWatchId(w.getWatchId()))
                    .withSelfRel());
            // link back to this collection (no filters)
            w.add(linkTo(methodOn(CatalogWatchController.class)
                    .getWatchesInCatalogWithFiltering(catalogId, Collections.emptyMap()))
                    .withRel("all-watches"));
        });

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{watchId}")
    public ResponseEntity<WatchResponseModel> getWatchInCatalogByWatchId(@PathVariable("watchId") String watchId) {

        return ResponseEntity.ok().body(catalogWatchService.getCatalogWatchByID(watchId));
    }

    @PostMapping
    public ResponseEntity<WatchResponseModel> addWatches(
            @PathVariable("catalog_id") String catalog_id,
            @RequestBody WatchRequestModel watchRequestModel
    ){

        return ResponseEntity.status(HttpStatus.CREATED).body(catalogWatchService.addWatches(watchRequestModel,catalog_id));
    }

    @PutMapping("/{watchId}")
    public ResponseEntity<WatchResponseModel> updateWatchInCatalog(
            @PathVariable("catalog_id") String catalogId, @PathVariable("watchId") String watchId, @RequestBody WatchRequestModel watchRequestModel
    ){
        return ResponseEntity.ok().body(catalogWatchService.updateWatchInInventory(catalogId,watchId,watchRequestModel));
    }

    @DeleteMapping("/{watchId}")
    ResponseEntity<String> removeWatchInCatalog(@PathVariable("catalog_id") String catalogId, @PathVariable("watchId") String watchId) {

        catalogWatchService.removeWatchInCatalog(catalogId,watchId);

        return ResponseEntity.ok("Watch with Id: " + watchId + " deleted successfully.");
    }







}
