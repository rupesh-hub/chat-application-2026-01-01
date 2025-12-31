package com.alfarays.user.entity;

import com.alfarays.image.entity.Image;
import com.alfarays.shared.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "_users")
@NamedQueries(
        {
                @NamedQuery(name = "User.findByEmail", query = "SELECT U FROM User U WHERE lower(U.email) = lower(:email)"),
                @NamedQuery(name = "User.emailExists", query = "SELECT CASE WHEN COUNT(U) > 0 THEN TRUE ELSE FALSE END FROM User U WHERE lower(U.email) = lower(:email)"),
        }
)
public class User extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "_user_id_seq_generator")
    @SequenceGenerator(name = "_user_id_seq_generator", sequenceName = "_user_id_seq", allocationSize = 1, initialValue = 1)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    private String firstname;
    private String lastname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;
    private String password;
    private LocalDateTime lastLogin;
    private String role;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Image profile;

}
