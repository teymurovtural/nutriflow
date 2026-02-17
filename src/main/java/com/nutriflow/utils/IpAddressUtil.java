package com.nutriflow.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Client-in real IP ünvanını müəyyən etmək üçün utility.
 * Proxy, Load Balancer və firewalls-u nəzərə alır.
 *
 * İstifadə:
 * @Autowired
 * private IpAddressUtil ipAddressUtil;
 *
 * String clientIp = ipAddressUtil.getClientIp();
 */
@Component
public class IpAddressUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private static final String DEFAULT_IP = "unknown";

    /**
     * Müxtəlif Proxy və Load Balancer-ləri nəzərə alaraq client-in real IP ünvanını tapır.
     *
     * @return Client IP ünvanı və ya "unknown" əgər tapılamazsa
     */
    public String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            return DEFAULT_IP;
        }

        HttpServletRequest request = attrs.getRequest();
        return getClientIpFromRequest(request);
    }

    /**
     * HttpServletRequest-dən IP ünvanını çıxarır.
     * Əvvəl header-ləri yoxlayır, sonra request-in remote address-ini istifadə edir.
     *
     * @param request HTTP request
     * @return IP ünvanı
     */
    private String getClientIpFromRequest(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);

            if (isValidIp(ipList)) {
                // X-Forwarded-For bir neçə IP qaytara bilər (vergüllə ayrılmış)
                // Biz birincisini (client-in IP) istifadə edirik
                return ipList.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * IP siyahısının valid olub olmadığını yoxlayır.
     *
     * @param ipList IP siyahısı (null, boş və ya "unknown" ola bilər)
     * @return Valid-dirsə true
     */
    private boolean isValidIp(String ipList) {
        return ipList != null
                && !ipList.isEmpty()
                && !"unknown".equalsIgnoreCase(ipList);
    }
}