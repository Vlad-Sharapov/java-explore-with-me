package ru.yandex.practicum.mainservice.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mainservice.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
