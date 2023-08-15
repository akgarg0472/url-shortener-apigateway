package com.akgarg.us.apigw.urlshortnerapigateway.service;

import org.springframework.stereotype.Service;

@Service
public class HttpAuthService implements AuthService {

    @Override
    public boolean isTokenValidated() {
        // todo: implement this method
        return false;
    }

    @Override
    public boolean isAuthorized() {
        // todo: implement this method
        return false;
    }

}
