package com.example.auth0springbootstarter.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Entity
public class Role extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth0_role_id", unique = true, nullable = false)
    private String auth0Id;

    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

}