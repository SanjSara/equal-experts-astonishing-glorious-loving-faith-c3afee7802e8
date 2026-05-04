package com.gist;

import com.gist.domain.Gist;

import java.util.List;

public interface GistClient {

    List<Gist> fetchPublicGists(String username);
}
