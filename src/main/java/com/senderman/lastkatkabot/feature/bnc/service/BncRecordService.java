package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.model.BncRecord;

import java.util.List;
import java.util.Optional;

public interface BncRecordService {

    List<BncRecord> findAll();

    Optional<BncRecord> findByLengthAndHexadecimal(int length, boolean hexadecimal);

    BncRecord save(BncRecord bncRecord);

}
