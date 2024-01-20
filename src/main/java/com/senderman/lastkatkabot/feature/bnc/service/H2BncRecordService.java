package com.senderman.lastkatkabot.feature.bnc.service;

import com.senderman.lastkatkabot.feature.bnc.model.BncRecord;
import com.senderman.lastkatkabot.feature.bnc.repository.BncRecordRepository;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class H2BncRecordService implements BncRecordService {

    private final BncRecordRepository repo;

    public H2BncRecordService(BncRecordRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<BncRecord> findAllOrderByHexadecimalAndLength() {
        return repo.findAllOrderByHexadecimalAndLength();
    }


    @Override
    public Optional<BncRecord> findByLengthAndHexadecimal(int length, boolean hexadecimal) {
        return repo.findById(new BncRecord.PrimaryKey(length, hexadecimal));
    }

    @Override
    public BncRecord save(BncRecord bncRecord) {
        return repo.existsById(bncRecord.getPrimaryKey()) ? repo.update(bncRecord) : repo.save(bncRecord);
    }
}
