package com.psiclinic.repository;

import com.psiclinic.model.Psicologo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsicologoRepository extends JpaRepository<Psicologo, Long> {

    Optional<Psicologo> findByCrp(String crp);

    boolean existsByCrp(String crp);

    boolean existsByCrpAndIdNot(String crp, Long id);
}
