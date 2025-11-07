package es.upm.miw.sparrow.data.datasource;

import java.util.ArrayList;
import java.util.List;

import es.upm.miw.sparrow.data.local.AvatarUrlBuilder;
import es.upm.miw.sparrow.data.remote.dicebear.AvatarResponse;

public class FunEmojiRemoteDataSource {

    public List<AvatarResponse> list(int count) {
        int safe = Math.max(1, Math.min(count, 64));
        List<AvatarResponse> out = new ArrayList<>(safe);
        for (int i = 1; i <= safe; i++) {
            String seed = "emoji_" + i;
            String url = AvatarUrlBuilder.buildAutoBg(seed, 128);
            out.add(new AvatarResponse(seed, url));
        }
        return out;
    }
}
