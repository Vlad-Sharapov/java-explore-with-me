package ru.yandex.practicum.mainservice.compilation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "compilation")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> events;

    @Column
    private Boolean pinned;

    @Column
    private String title;

}
