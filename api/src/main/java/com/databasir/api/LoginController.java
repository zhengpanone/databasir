package com.databasir.api;

import com.databasir.common.DatabasirException;
import com.databasir.common.JsonData;
import com.databasir.common.exception.InvalidTokenException;
import com.databasir.core.domain.DomainErrors;
import com.databasir.core.domain.log.annotation.Operation;
import com.databasir.core.domain.login.data.AccessTokenRefreshRequest;
import com.databasir.core.domain.login.data.AccessTokenRefreshResponse;
import com.databasir.core.domain.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class LoginController {

    private final AuthenticationManager authenticationManager;

    private final LoginService loginService;

    @GetMapping(Routes.Login.LOGOUT)
    @Operation(module = Operation.Modules.USER, name = "注销登录")
    public JsonData<Void> logout() {
        SecurityContextHolder.clearContext();
        return JsonData.ok();
    }

    @PostMapping(Routes.Login.REFRESH_ACCESS_TOKEN)
    public JsonData<AccessTokenRefreshResponse> refreshAccessTokens(@RequestBody @Valid AccessTokenRefreshRequest request,
                                                                    HttpServletResponse response) {
        try {
            return JsonData.ok(loginService.refreshAccessTokens(request));
        } catch (DatabasirException e) {
            if (Objects.equals(e.getErrCode(), DomainErrors.INVALID_REFRESH_TOKEN_OPERATION.getErrCode())) {
                throw new InvalidTokenException(DomainErrors.INVALID_REFRESH_TOKEN_OPERATION);
            }
            if (Objects.equals(e.getErrCode(), DomainErrors.REFRESH_TOKEN_EXPIRED.getErrCode())) {
                throw new InvalidTokenException(DomainErrors.REFRESH_TOKEN_EXPIRED);
            }
            throw e;
        }
    }
}
