package foodprint.backend.model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepo extends JpaRepository<Token, Long> {

    Token findByToken(String token);
    List<Token> findByRequestor(User user);
    Page<Token> findByRequestor(Pageable page, User user);

    void deleteByToken(String token);
}

