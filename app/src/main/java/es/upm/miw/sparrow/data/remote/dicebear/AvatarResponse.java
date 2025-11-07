package es.upm.miw.sparrow.data.remote.dicebear;

public class AvatarResponse {
    private final String seed;
    private final String url;

    public AvatarResponse(String seed, String url) {
        this.seed = seed;
        this.url = url;
    }
    public String getSeed() { return seed; }
    public String getUrl() { return url; }
}
