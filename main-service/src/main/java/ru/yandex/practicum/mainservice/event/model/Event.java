package ru.yandex.practicum.mainservice.event.model;

import lombok.*;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "events")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    private String annotation;

    private String title;

    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "create_on")
    private LocalDateTime createOn = LocalDateTime.now().withNano(0);

    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(columnDefinition = "integer default false")
    private boolean paid = false;

    @Column(name = "participant_limit", columnDefinition = "integer default 0")
    private int participantLimit = 0;

    @Column(name = "request_moderation", columnDefinition = "boolean default true")
    private boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    private EventState state = EventState.PENDING;

}
