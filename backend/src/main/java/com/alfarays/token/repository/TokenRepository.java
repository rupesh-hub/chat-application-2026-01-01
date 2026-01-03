package com.alfarays.token.repository;

import com.alfarays.token.entity.Token;
import com.alfarays.token.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT T FROM Token T WHERE T.username = :username AND T.token = :token AND T.type = :type")
    Optional<Token> findToken(@Param("username") String username, @Param("token") String token, @Param("type") TokenType type);

}
