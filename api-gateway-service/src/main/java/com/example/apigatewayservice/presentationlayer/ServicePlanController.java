package com.example.apigatewayservice.presentationlayer;



import com.example.apigatewayservice.businesslayer.servicePlanservicesBusinessLayer.ServicePlanService;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanRequestModel;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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

        List<ServicePlanResponseModel> list = servicePlanService.getServicePlans();
        list.forEach(p -> {
            p.add(linkTo(methodOn(ServicePlanController.class)
                    .getServicePlanById(p.getPlanId())).withSelfRel());
            p.add(linkTo(methodOn(ServicePlanController.class)
                    .getServicePlans()).withRel("all-plans"));
        });
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{plan_id}")
    public ResponseEntity<ServicePlanResponseModel> getServicePlanById(@PathVariable String plan_id) {

        ServicePlanResponseModel p = servicePlanService.getServicePlansById(plan_id);
        p.add(linkTo(methodOn(ServicePlanController.class)
                .getServicePlanById(plan_id)).withSelfRel());
        p.add(linkTo(methodOn(ServicePlanController.class)
                .getServicePlans()).withRel("all-plans"));
        return ResponseEntity.ok(p);

    }

    @PostMapping()
    public ResponseEntity<ServicePlanResponseModel> addServicePlan(@RequestBody ServicePlanRequestModel servicePlanRequestModel) {

        ServicePlanResponseModel p = servicePlanService.addServicePlan(servicePlanRequestModel);
        p.add(linkTo(methodOn(ServicePlanController.class)
                .getServicePlanById(p.getPlanId())).withSelfRel());
        p.add(linkTo(methodOn(ServicePlanController.class)
                .getServicePlans()).withRel("all-plans"));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(p);
    }

    @PutMapping("/{plan_id}")
    public ResponseEntity<ServicePlanResponseModel> updateServicePlan(@PathVariable String plan_id, @RequestBody ServicePlanRequestModel servicePlanRequestModel) {

        ServicePlanResponseModel p = servicePlanService.updateServicePlan(plan_id, servicePlanRequestModel);
        p.add(linkTo(methodOn(ServicePlanController.class)
                .getServicePlanById(plan_id)).withSelfRel());
        p.add(linkTo(methodOn(ServicePlanController.class)
                .getServicePlans()).withRel("all-plans"));
        return ResponseEntity.ok(p);

    }

    @DeleteMapping("/{plan_id}")
    public ResponseEntity<String> deleteServicePlanById(@PathVariable String plan_id) {

        servicePlanService.deleteServicePlanById(plan_id);
        String msg = "Deleted " + plan_id;
        Link all = linkTo(methodOn(ServicePlanController.class)
                .getServicePlans()).withRel("all-plans");
        return ResponseEntity
                .noContent()
                .header(HttpHeaders.LINK, all.toUri().toString())
                .build();
    }
}
