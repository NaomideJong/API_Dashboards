package com.ctsgroup.nl.dashretry.models;

import jakarta.persistence.Entity;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Builder
public class EverhourUser {

    private Long everhourUserId;
    private String email;

}
