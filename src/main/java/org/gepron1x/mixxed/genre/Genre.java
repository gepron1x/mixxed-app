package org.gepron1x.mixxed.genre;

import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.List;

public enum Genre {

    HOUSE,
    TECHNO,
    TRANCE,
    DRUM_AND_BASS("Drum and Bass"),
    DUBSTEP,
    HIPHOP("Hip-Hop"),
    AMBIENT,
    OTHER("Другое");

    private final String fullName;

    Genre(String fullName) {
        this.fullName = fullName;
    }
    Genre() {
        this.fullName = WordUtils.capitalize(this.name().toLowerCase());
    }

    public String getFullName() {
        return fullName;
    }

    public static List<Genre> genres() {
        return Arrays.asList(Genre.values());
    }






}
