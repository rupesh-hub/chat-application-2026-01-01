package com.alfarays.user.repository;

import com.alfarays.user.entity.User;
import com.alfarays.user.model.UserFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withFilter(UserFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(StringUtils.hasText(filter.getQuery())) {
                String term = String.format("%%%s%%", filter.getQuery().toLowerCase()); /* %query% */
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term)
                ));
            }

            // filter by individual fields
            if(StringUtils.hasText(filter.getFirstname())) {
                var term = "%" + filter.getFirstname().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstname")), term));
            }

            if(StringUtils.hasText(filter.getLastname())) {
                var term = "%" + filter.getLastname().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastname")), term));
            }

            if(StringUtils.hasText(filter.getEmail())) {
                var term = "%" + filter.getEmail().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }

}
