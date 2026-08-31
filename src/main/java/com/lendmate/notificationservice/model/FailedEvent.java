package com.lendmate.notificationservice.model;

import com.lendmate.notificationservice.model.Enum.FailedEventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "failed_events")
public class FailedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID eventId;
    private Long orderId;
    private String originalTopic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String exceptionMessage;

    @Builder.Default
    private Instant failedAt = Instant.now();

    private Instant retriedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FailedEventStatus status = FailedEventStatus.PENDING;

}
