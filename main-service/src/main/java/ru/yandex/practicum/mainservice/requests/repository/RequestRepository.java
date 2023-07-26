package ru.yandex.practicum.mainservice.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.Request;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(long requesterId);

    Request findByRequesterIdAndEventId(long requesterId, long eventId);

    List<Request> findAllByEventId(long eventId);

    List<Request> findAllByStatusAndEventIn(RequestStatus status, Collection<Event> event);

    long countAllByStatusAndEventId(RequestStatus status, long eventId);

}
