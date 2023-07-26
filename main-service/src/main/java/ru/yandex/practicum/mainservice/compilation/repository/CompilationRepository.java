package ru.yandex.practicum.mainservice.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.yandex.practicum.mainservice.compilation.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long>, QuerydslPredicateExecutor<Compilation> {

}
