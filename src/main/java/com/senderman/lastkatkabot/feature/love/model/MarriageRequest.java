package com.senderman.lastkatkabot.feature.love.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.*;

import java.sql.Timestamp;
import java.util.Objects;

@MappedEntity("MARRIAGE_REQUEST")
public class MarriageRequest {

    @MappedProperty("proposer_id")
    private final long proposerId;
    @MappedProperty("proposer_name")
    private final String proposerName;
    @MappedProperty("proposee_id")
    private final long proposeeId;
    @MappedProperty("proposee_name")
    private final String proposeeName;
    @Id
    @GeneratedValue
    private int id;
    @MappedProperty("created_at")
    @DateCreated
    private Timestamp createdAt;

    @Creator
    public MarriageRequest(long proposerId, String proposerName, long proposeeId, String proposeeName) {
        this.proposerId = proposerId;
        this.proposerName = proposerName;
        this.proposeeId = proposeeId;
        this.proposeeName = proposeeName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getProposerId() {
        return proposerId;
    }

    public String getProposerName() {
        return proposerName;
    }

    public long getProposeeId() {
        return proposeeId;
    }

    public String getProposeeName() {
        return proposeeName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarriageRequest that = (MarriageRequest) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Builder {
        private long proposerId;
        private String proposerName;
        private long proposeeId;
        private String proposeeName;

        public Builder setProposerId(long proposerId) {
            this.proposerId = proposerId;
            return this;
        }

        public Builder setProposerName(String proposerName) {
            this.proposerName = proposerName;
            return this;
        }

        public Builder setProposeeId(long proposeeId) {
            this.proposeeId = proposeeId;
            return this;
        }

        public Builder setProposeeName(String proposeeName) {
            this.proposeeName = proposeeName;
            return this;
        }

        public MarriageRequest createMarriageRequest() {
            return new MarriageRequest(proposerId, proposerName, proposeeId, proposeeName);
        }
    }
}
