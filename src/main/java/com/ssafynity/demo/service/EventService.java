package com.ssafynity.demo.service;

import com.ssafynity.demo.common.exception.BusinessException;
import com.ssafynity.demo.common.exception.ErrorCode;
import com.ssafynity.demo.domain.Event;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.dto.request.EventRequest;
import com.ssafynity.demo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    private static final String[] EVENT_TYPES = {"스터디", "해커톤", "세미나", "워크숍", "기타"};
    private static final String[] LOCATIONS = {"ONLINE", "OFFLINE", "HYBRID"};

    public String[] getEventTypes() { return EVENT_TYPES; }
    public String[] getLocations()  { return LOCATIONS; }

    @Transactional
    public Event create(EventRequest req, Member organizer) {
        Event event = Event.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .location(req.getLocation())
                .eventType(req.getEventType())
                .maxParticipants(req.getMaxParticipants())
                .organizer(organizer)
                .status("UPCOMING")
                .build();
        return eventRepository.save(event);
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findUpcoming() {
        return eventRepository.findByStartDateAfterOrderByStartDateAsc(LocalDateTime.now());
    }

    public List<Event> findUpcomingTop4() {
        return eventRepository.findTop4ByStartDateAfterOrderByStartDateAsc(LocalDateTime.now());
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByStartDateAsc();
    }

    public List<Event> findOngoing() {
        return eventRepository.findByStatusOrderByStartDateAsc("ONGOING");
    }

    @Transactional
    public void update(Long id, EventRequest req, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        if (!event.getOrganizer().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.EVENT_ACCESS_DENIED);
        }
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setStartDate(req.getStartDate());
        event.setEndDate(req.getEndDate());
        event.setLocation(req.getLocation());
        event.setEventType(req.getEventType());
        event.setMaxParticipants(req.getMaxParticipants());
    }

    @Transactional
    public void join(Long id) {
        System.out.println("이벤트 참가 시도 id=" + id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        if (event.getMaxParticipants() > 0
                && event.getCurrentParticipants() >= event.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.EVENT_FULL);
        }
        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
    }

    @Transactional
    public void delete(Long id, Long requesterId, String requesterRole) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        boolean isOwner = event.getOrganizer().getId().equals(requesterId);
        boolean isAdmin = "ADMIN".equals(requesterRole);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.EVENT_ACCESS_DENIED);
        }
        eventRepository.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        event.setStatus(status);
    }
}
