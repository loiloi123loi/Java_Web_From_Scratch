package com.polime.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(formatter));
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        String dateStr = in.nextString();
        try {
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr, formatter);
            }
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            throw new IOException(
                    String.format("Invalid date format: '%s'. Expected 'yyyy-MM-dd'.", dateStr));
        }
    }
}
