package com.psiclinic.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "psicologos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Psicologo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String crp;

    @Column(length = 100)
    private String especialidade;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Builder.Default
    @OneToMany(mappedBy = "psicologo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sessao> sessoes = new ArrayList<>();
}
