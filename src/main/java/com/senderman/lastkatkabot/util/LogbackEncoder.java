package com.senderman.lastkatkabot.util;

import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.Status;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LogbackEncoder implements Encoder<ILoggingEvent> {

    private final Encoder<ILoggingEvent> encoder;
    private final Supplier<String> getPattern;
    private final Consumer<String> setPattern;


    public LogbackEncoder() {
        if (System.getProperties().containsKey("jsonLogs")) {
            var jsonEncoder = new JsonEncoder();
            jsonEncoder.setWithFormattedMessage(true);
            this.encoder = jsonEncoder;
            // json encoder doesn't have pattern field, so we make these methods do nothing
            this.getPattern = () -> null;
            this.setPattern = s -> {
            };
        } else {
            var enc = new PatternLayoutEncoder();
            this.encoder = enc;
            this.getPattern = enc::getPattern;
            this.setPattern = enc::setPattern;
        }
    }

    @Override
    public byte[] headerBytes() {
        return encoder.headerBytes();
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        return encoder.encode(event);
    }

    @Override
    public byte[] footerBytes() {
        return encoder.footerBytes();
    }

    @Override
    public Context getContext() {
        return encoder.getContext();
    }

    @Override
    public void setContext(Context context) {
        encoder.setContext(context);
    }

    @Override
    public void addStatus(Status status) {
        encoder.addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        encoder.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        encoder.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        encoder.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        encoder.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        encoder.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        encoder.addError(msg, ex);
    }

    @Override
    public void start() {
        encoder.start();
    }

    @Override
    public void stop() {
        encoder.stop();
    }

    @Override
    public boolean isStarted() {
        return encoder.isStarted();
    }

    public String getPattern() {
        return getPattern.get();
    }

    public void setPattern(String pattern) {
        setPattern.accept(pattern);
    }
}

