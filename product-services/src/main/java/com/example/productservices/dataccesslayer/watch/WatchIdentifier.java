package com.example.productservices.dataccesslayer.watch;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class WatchIdentifier {

    @Column(name = "watch_id",nullable = false,unique = true)
    private String watchId;

    public  WatchIdentifier() {
        this.watchId = UUID.randomUUID().toString();
    }


}
