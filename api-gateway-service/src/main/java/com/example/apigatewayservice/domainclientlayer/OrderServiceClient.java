package com.example.apigatewayservice.domainclientlayer;

import com.example.apigatewayservice.presentationlayer.orderdtos.OrderRequestModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderResponseModel;
import com.example.apigatewayservice.utils.HttpErrorInfo;
import com.example.apigatewayservice.utils.InvalidInputException;
import com.example.apigatewayservice.utils.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
@Slf4j
public class OrderServiceClient {

        private final RestTemplate restTemplate;
        private final ObjectMapper objectMapper;
        private final String ORDER_SERVICE_BASE_URL;

        public OrderServiceClient(RestTemplate restTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${app.order-services.host}") String orderServicesHost,
                                  @Value("${app.order-services.port}") String orderServicesPort) {
            this.restTemplate  = restTemplate;
            this.objectMapper  = objectMapper;
            this.ORDER_SERVICE_BASE_URL = "http://" +
                    orderServicesHost + ":" + orderServicesPort + "/api/v1/orders";
        }

        public List<OrderResponseModel> getAllOrders() {
            try {
                return restTemplate.exchange(
                        ORDER_SERVICE_BASE_URL,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<OrderResponseModel>>() {}
                ).getBody();
            } catch (HttpClientErrorException ex) {
                throw handleHttpClientException(ex);
            }
        }

    public OrderResponseModel getOrderById(String orderId) {
        try {
            return restTemplate.getForObject(
                    ORDER_SERVICE_BASE_URL + "/" + orderId,
                    OrderResponseModel.class
            );
        }
        catch (HttpClientErrorException.NotFound nf) {
            log.debug("Order {} not found → returning null", orderId);
            return null;
        }
        catch (HttpClientErrorException ce) {
            throw handleHttpClientException(ce);
        }
    }

        public OrderResponseModel createOrder(OrderRequestModel newOrder) {
            try {
                return restTemplate.postForObject(
                        ORDER_SERVICE_BASE_URL,
                        newOrder,
                        OrderResponseModel.class
                );
            } catch (HttpClientErrorException ex) {
                throw handleHttpClientException(ex);
            }
        }

        public OrderResponseModel updateOrder(String orderId, OrderRequestModel updateOrder) {
            try {
                restTemplate.put(
                        ORDER_SERVICE_BASE_URL + "/" + orderId,
                        updateOrder
                );
                return getOrderById(orderId);
            } catch (HttpClientErrorException ex) {
                throw handleHttpClientException(ex);
            }
        }

        public String deleteOrder(String orderId) {
            try {
                restTemplate.delete(ORDER_SERVICE_BASE_URL + "/" + orderId);
                return "Order deleted successfully.";
            } catch (HttpClientErrorException ex) {
                throw handleHttpClientException(ex);
            }
        }

        private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
            HttpStatus status = (HttpStatus) ex.getStatusCode();
            String message = extractErrorMessage(ex);

            if (status == UNPROCESSABLE_ENTITY) {
                return new InvalidInputException(message);
            } else if (status == NOT_FOUND) {
                return new NotFoundException(message);
            } else {
                log.warn("Unexpected HTTP error: {} - {}", status, message);
                return new InvalidInputException(message);
            }
        }

        private String extractErrorMessage(HttpClientErrorException ex) {
            try {
                return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class)
                        .getMessage();
            } catch (IOException io) {
                return io.getMessage();
            }
        }

}
