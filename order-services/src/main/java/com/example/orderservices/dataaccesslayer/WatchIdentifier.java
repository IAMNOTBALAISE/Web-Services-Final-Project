package com.example.orderservices.dataaccesslayer;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class WatchIdentifier {


    private String watchId;

    public  WatchIdentifier() {
        this.watchId = UUID.randomUUID().toString();
    }


}
