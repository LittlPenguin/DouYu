package cn.edu.app.douyu.server.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * TraceId 过滤器：为每个 HTTP 请求生成或传递 traceId。
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requested = request.getHeader("X-Request-Id");
        String traceId = requested == null || requested.isBlank()
                ? "trace_" + LocalDate.now().format(DATE) + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                : requested;
        TraceContext.set(traceId);
        response.setHeader("X-Trace-Id", traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceContext.clear();
        }
    }
}
