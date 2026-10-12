package com.ballastlane.pokedex.application.port;

import com.ballastlane.pokedex.domain.model.AuthToken;
import com.ballastlane.pokedex.domain.model.User;

public interface TokenIssuer {

    AuthToken issue(User user);
}
