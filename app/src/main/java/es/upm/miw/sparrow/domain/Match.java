package es.upm.miw.sparrow.domain;

import com.google.firebase.Timestamp;

public class Match {
    public final String id;
    public final String category;
    public final String email;
    public final int numPoints;
    public final int totalPoints;
    public final Timestamp timestamp;

    public Match(String id, String category, String email, int numPoints, int totalPoints, Timestamp timestamp) {
        this.id = id;
        this.category = category;
        this.email = email;
        this.numPoints = numPoints;
        this.totalPoints = totalPoints;
        this.timestamp = timestamp;
    }

    public boolean isPassed() {
        return numPoints * 2 >= totalPoints;
    }

    public String pointsPretty() {
        return numPoints + "/" + totalPoints;
    }
}
