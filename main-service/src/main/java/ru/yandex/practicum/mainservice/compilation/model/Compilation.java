package ru.yandex.practicum.mainservice.compilation.model;

import lombok.*;
import ru.yandex.practicum.mainservice.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Setter
@Getter
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

    private Boolean pinned;

    private String title;

}
