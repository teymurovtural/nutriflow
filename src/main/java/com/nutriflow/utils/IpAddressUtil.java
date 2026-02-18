package com.nutriflow.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for determining the real IP address of a client.
 * Takes into account Proxy, Load Balancer and firewalls.
 *
 * Usage:
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
     * Resolves the real IP address of the client,
     * taking into account various Proxy and Load Balancer configurations.
     *
     * @return Client IP address or "unknown" if not found
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
     * Extracts the IP address from an HttpServletRequest.
     * Checks headers first, then falls back to the request's remote address.
     *
     * @param request HTTP request
     * @return IP address
     */
    private String getClientIpFromRequest(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);

            if (isValidIp(ipList)) {
                // X-Forwarded-For may return multiple IPs (comma-separated)
                // We use the first one which represents the original client IP
                return ipList.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Checks whether an IP list is valid.
     *
     * @param ipList IP list (can be null, empty or "unknown")
     * @return true if valid
     */
    private boolean isValidIp(String ipList) {
        return ipList != null
                && !ipList.isEmpty()
                && !"unknown".equalsIgnoreCase(ipList);
    }
}