package ru.yandex.practicum.mainservice.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.yandex.practicum.mainservice.user.model.User;


public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

}
