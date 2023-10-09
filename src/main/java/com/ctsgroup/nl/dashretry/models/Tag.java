package com.ctsgroup.nl.dashretry.models;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Entity
@Builder
@Table(name = "tags")
public class Tag {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "tag_name")
    private String tagName;

}
