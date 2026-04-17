package com.example.demo.base.shared.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event IdempotentLog 的複合主鍵
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventIdempotentLogId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType;

    private String uniqueKey;

    public int hashCode() {
        int result = 1;
        result = 31 * result + (this.eventType == null ? 0 : this.eventType.hashCode());
        result = 31 * result + (this.uniqueKey == null ? 0 : this.uniqueKey.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            EventIdempotentLogId other = (EventIdempotentLogId) obj;
            if (this.eventType == null) {
                if (other.eventType != null) {
                    return false;
                }
            } else if (!this.eventType.equals(other.eventType)) {
                return false;
            }

            if (this.uniqueKey == null) {
                if (other.uniqueKey != null) {
                    return false;
                }
            } else if (!this.uniqueKey.equals(other.uniqueKey)) {
                return false;
            }

            return true;
        }
    }

    public String toString() {
        return "EventIdempotentLogId(eventType=" + this.eventType + ", uniqueKey=" + this.uniqueKey
                + ")";
    }

}
