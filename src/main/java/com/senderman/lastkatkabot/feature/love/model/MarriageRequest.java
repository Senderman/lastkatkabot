package com.senderman.lastkatkabot.feature.love.model;


import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.sql.Timestamp;
import java.util.Objects;

@MappedEntity("marriage_request")
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
    private int id;
    @MappedProperty("created_at")
    @DateCreated
    private Timestamp createdAt;

    @Creator
    public MarriageRequest(int id, long proposerId, String proposerName, long proposeeId, String proposeeName) {
        this.id = id;
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

}
