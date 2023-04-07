package com.goit;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;
    String timezone;

    @Override
    public void init() {
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setSuffix(".html");
        resolver.setPrefix(getPrefix());
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    private String getPrefix() {
        try {
            URI uri = getClass().getClassLoader().getResource("templates").toURI();
            return Paths.get(uri).toFile().getAbsolutePath() + "/";
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=utf-8");
        timezone = req.getParameter("timezone");

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("timeString", getCurrentUtcTime(checkingLastTimezoneCookie(timezone, req, resp)));
        Context timeContext = new Context(
                req.getLocale(),
                Map.of("queryParams", params)
        );
        engine.process("timePage", timeContext, resp.getWriter());
        resp.getWriter().close();
    }

    public static String getCurrentUtcTime(String timezone) {
        ZoneId zone = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zone);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss '" + timezone + "'"));
    }

    public static String checkingLastTimezoneCookie(String tz, HttpServletRequest req, HttpServletResponse resp) {
        String cookieValue = null;
        if (tz != null && tz.contains(" ")) {
            tz = tz.replace(" ", "+");
        }

        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            if (tz == null) {
                return "UTC";
            } else {
                resp.addCookie(new Cookie("lastTimezone", tz));
            }
        } else {
            cookieValue = Arrays.stream(cookies)
                    .filter(item -> item.getName().equals("lastTimezone"))
                    .collect(Collectors.toList())
                    .get(0).getValue();
        }
        if (cookieValue == null) {
            if (tz == null) {
                return "UTC";
            } else {
                resp.addCookie(new Cookie("lastTimezone", tz));
            }
        } else if (tz == null) {
            tz = cookieValue;
        } else {
            resp.addCookie(new Cookie("lastTimezone", tz));
        }
        return tz;
    }
}
