package com.alfarays.image.entity;

import com.alfarays.shared.AbstractEntity;
import com.alfarays.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Image extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "_image_id_seq_generator")
    @SequenceGenerator(name = "_image_id_seq_generator", sequenceName = "_image_id_seq", allocationSize = 1, initialValue = 1)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private long size;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

}
