package com.example.mindarkproxy.common.constant;

public class CacheConstant {
    /**
     * redis user login key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * redis admin login key
     */
    public static final String LOGIN_BOOST_TOKEN_KEY = "login_boost_tokens:";

    /**
     * redis multiple submit prevention key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * redis limit rate key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * redis user password error count key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * redis admin password error count key
     */
    public static final String BOOST_PWD_ERR_CNT_KEY = "boost_pwd_err_cnt:";
}
