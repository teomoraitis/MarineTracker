package com.di.marinetracker.backendspringboot.specifications;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import org.springframework.data.jpa.domain.Specification;

// Specifications are used for ORM like query building
public class VesselSpecifications {

    // Specification to filter vessels by type
    public static Specification<Vessel> hasType(String type) {
        return (root, query, builder) ->
                type == null ? null : builder.equal(root.get("type"), type);
    }

    // Specification to filter vessels by name
    public static Specification<Vessel> hasName(String name) {
        return (root, query, builder) ->
                name == null ? null : builder.like(root.get("name"), "%" + name + "%");
    }

}
