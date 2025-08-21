package com.example.serviceplanservices.presentationlayer;


import com.example.serviceplanservices.businesslayer.ServicePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class ServicePlanController {

    private final ServicePlanService servicePlanService;

    @Autowired
    public ServicePlanController(ServicePlanService servicePlanService) {
        this.servicePlanService = servicePlanService;
    }

    @GetMapping()
    public ResponseEntity<List<ServicePlanResponseModel>> getServicePlans() {

        return ResponseEntity.ok(this.servicePlanService.getServicePlans());
    }

    @GetMapping("/{plan_id}")
    public ResponseEntity<ServicePlanResponseModel> getServicePlanById(@PathVariable String plan_id) {

        return ResponseEntity.ok(this.servicePlanService.getServicePlansById(plan_id));

    }

    @PostMapping()
    public ResponseEntity<ServicePlanResponseModel> addServicePlan(@RequestBody ServicePlanRequestModel servicePlanRequestModel) {

        ServicePlanResponseModel created = servicePlanService.addServicePlan(servicePlanRequestModel);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PutMapping("/{plan_id}")
    public ResponseEntity<ServicePlanResponseModel> updateServicePlan(@PathVariable String plan_id, @RequestBody ServicePlanRequestModel servicePlanRequestModel) {

        return ResponseEntity.ok(this.servicePlanService.updateServicePlan(plan_id,servicePlanRequestModel));

    }

    @DeleteMapping("/{plan_id}")
    public ResponseEntity<String> deleteServicePlanById(@PathVariable String plan_id) {

        servicePlanService.deleteServicePlanById(plan_id);
        return ResponseEntity.noContent().build();
    }
}
